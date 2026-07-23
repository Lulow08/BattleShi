# 🚢 BattleShi

> Juego de batalla naval en JavaFX donde un jugador humano desafía a una máquina en un duelo estratégico de flotas en un tablero de 10×10.

---

## 📖 Tabla de contenidos

- [Descripción](#descripción)
- [Reglas del juego](#reglas-del-juego)
- [Funcionalidades](#funcionalidades)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Estructuras de datos](#estructuras-de-datos)
- [Concurrencia](#concurrencia)
- [Manejo de excepciones](#manejo-de-excepciones)
- [Persistencia](#persistencia)
- [Pruebas unitarias](#pruebas-unitarias)
- [Cómo ejecutar](#cómo-ejecutar)
- [Autores](#autores)

---

## Descripción

**BattleShi** es un juego de batalla naval desarrollado como proyecto para la asignatura *750014C Fundamentos de Programación Orientada a Eventos* de la Universidad del Valle.

El jugador humano coloca su flota en un tablero de 10×10 y luego enfrenta a una máquina que dispara con una estrategia de caza inteligente. Ambos turnos se alternan hasta que uno de los dos jugadores hunde toda la flota del contrario. El juego permite guardar y reanudar partidas en progreso.

---

## Reglas del juego

- **Tablero:** Cuadrícula de 10×10 con coordenadas de columna A–J y fila 1–10.
- **Flota por jugador:**

  | Tipo de barco    | Tamaño | Cantidad |
    |------------------|--------|----------|
  | Aircraft Carrier | 4      | 1        |
  | Submarine        | 3      | 2        |
  | Destroyer        | 2      | 3        |
  | Frigate          | 1      | 4        |

- **Colocación:** Los barcos no pueden superponerse ni tocarse entre sí (ni en diagonal).
- **Turnos:** El humano dispara primero; si acierta, vuelve a disparar. Si falla, la máquina toma su turno. La máquina también dispara de nuevo al acertar.
- **Victoria:** Gana quien hunda la totalidad de la flota contraria.
- **Guardado:** Solo se persisten partidas en estado `IN_PROGRESS`. Las partidas terminadas eliminan el archivo de guardado automáticamente.

---

## Funcionalidades

- ✅ Fase de colocación interactiva con arrastrar, soltar y rotar barcos (tecla **R**)
- ✅ Vista previa en verde/rojo al pasar el cursor sobre el tablero
- ✅ Cancelar selección con **Escape**; recoger barco ya colocado al hacer clic sobre él
- ✅ Colocación aleatoria de la flota de la máquina en hilo de fondo
- ✅ IA con estrategia **random + hunt & target**: disparo aleatorio hasta acertar, luego caza los vecinos ortogonales del impacto
- ✅ Turno de la máquina con pausa simulada de 1.2 segundos sin bloquear la UI
- ✅ Marcadores visuales de impacto (💥), fallo (⚪) y hundimiento con sprite del barco
- ✅ Guardado automático de la sesión tras cada disparo durante la partida
- ✅ Carga de partida guardada desde el menú principal ("Continuar")
- ✅ Nickname del jugador persistido en archivo de texto entre sesiones
- ✅ Overlay de victoria y derrota con redirección al menú principal

---

## Estructura del proyecto

```
src/
├── main/
│   ├── java/com/cuatrifasico/battleshi/
│   │   ├── Main.java
│   │   ├── controller/
│   │   │   ├── GameController.java        ← controlador principal (placement + combate)
│   │   │   ├── MenuController.java        ← controlador del menú de inicio
│   │   │   └── PlacementController.java   ← fase de colocación de barcos
│   │   ├── model/
│   │   │   ├── AppInitializer.java
│   │   │   ├── concurrency/
│   │   │   │   ├── FleetPlacementThread.java  ← hilo para colocación aleatoria de la máquina
│   │   │   │   └── MachineTurnThread.java     ← hilo del turno de la máquina
│   │   │   ├── entities/
│   │   │   │   ├── Board.java             ← tablero 10×10 con reglas de colocación y disparo
│   │   │   │   ├── Cell.java              ← celda individual (estado + barco ocupante)
│   │   │   │   ├── Coordinate.java        ← posición inmutable con parsing A1–J10
│   │   │   │   ├── GameSession.java       ← snapshot serializable de la partida
│   │   │   │   ├── HumanPlayer.java       ← jugador humano
│   │   │   │   ├── MachinePlayer.java     ← jugador IA (Strategy pattern)
│   │   │   │   ├── Player.java            ← clase base abstracta del jugador
│   │   │   │   ├── RandomFleetPlacer.java ← algoritmo de colocación aleatoria válida
│   │   │   │   ├── Ship.java              ← barco con celdas ocupadas y registro de impactos
│   │   │   │   └── Shot.java              ← registro inmutable de un disparo
│   │   │   ├── enums/
│   │   │   │   ├── CellState.java         ← WATER, SHIP, MISS, HIT, SUNK
│   │   │   │   ├── GameState.java         ← PLACEMENT, IN_PROGRESS, PLAYER_WON, MACHINE_WON
│   │   │   │   ├── Orientation.java       ← HORIZONTAL, VERTICAL (con deltas)
│   │   │   │   └── ShipType.java          ← tipos de barco con tamaño y cantidad de flota
│   │   │   ├── exceptions/
│   │   │   │   ├── AlreadyShotException.java
│   │   │   │   ├── BattleShiException.java
│   │   │   │   ├── IllegalGameActionException.java
│   │   │   │   ├── InvalidPlacementException.java
│   │   │   │   └── PersistenceException.java
│   │   │   ├── persistence/
│   │   │   │   └── GamePersistenceManager.java ← guardado/carga binaria y nickname
│   │   │   └── strategy/
│   │   │       ├── IMachineShotStrategy.java   ← interfaz de estrategia de disparo
│   │   │       └── RandomHuntStrategy.java     ← random + hunt & target con ArrayDeque
│   │   └── view/
│   │       ├── SceneManager.java           ← singleton para cambio de escenas
│   │       └── board/
│   │           ├── BoardGridView.java      ← renderizado de la cuadrícula
│   │           ├── BoardSpriteLayer.java   ← capa de sprites de barcos
│   │           ├── BoardTheme.java         ← constantes visuales del tablero
│   │           └── ShipTrayView.java       ← bandeja de selección de barcos
│   │       └── shapes/
│   │           ├── MarkerShapeFactory.java ← fábricas de formas de impacto/fallo
│   │           ├── ShipShapeFactory.java   ← fábricas de formas de barcos
│   │           └── SpriteOverlayFactory.java
│   └── resources/
│       ├── fxml/
│       │   ├── menu-view.fxml
│       │   └── game-view.fxml
│       ├── css/styles.css
│       ├── fonts/
│       │   ├── BDOGrotesk-VF.ttf
│       │   ├── ITC-Machine.otf
│       │   └── SF-Pro.ttf
│       └── images/                         ← sprites de barcos (normal + hundido) y marcadores
└── test/
    └── java/com/cuatrifasico/battleshi/model/
        ├── BoardTest.java
        ├── CoordinateTest.java
        └── ShipTest.java
```

---

## Tecnologías

| Herramienta     | Versión    |
|-----------------|------------|
| Java            | SE 17      |
| JavaFX          | 21.0.6     |
| Maven           | 3.x        |
| JUnit Jupiter   | 5.12.1     |
| IntelliJ IDEA   | —          |
| Scene Builder   | —          |

---

## Arquitectura

El proyecto sigue estrictamente la arquitectura **MVC (Modelo-Vista-Controlador)**:

- **Modelo** — `entities/`, `enums/`, `exceptions/`, `strategy/`, `concurrency/`, `persistence/`. Ninguna clase del modelo importa JavaFX.
- **Vista** — `BoardGridView`, `BoardSpriteLayer`, `BoardTheme`, `ShipTrayView` y las fábricas de formas manejan todo el renderizado. `SceneManager` (singleton) gestiona el cambio de escenas.
- **Controlador** — `GameController` orquesta la fase de colocación (delegando a `PlacementController`) y la fase de combate. Nunca muta el modelo directamente sin pasar por sus métodos definidos.

La máquina sigue el **patrón Strategy** (`IMachineShotStrategy` / `RandomHuntStrategy`), lo que permite intercambiar el algoritmo de disparo sin modificar `MachinePlayer`.

---

## Estructuras de datos

Se usan cuatro estructuras de datos distintas, cada una elegida por su patrón de acceso específico:

| Estructura            | Dónde                | Por qué                                                                 |
|-----------------------|----------------------|-------------------------------------------------------------------------|
| `Cell[][]`            | `Board`              | Acceso O(1) por coordenada (fila, columna) — ideal para cuadrícula 2D  |
| `LinkedHashSet<Ship>` | `Board.fleet`        | Preserva el orden de inserción e impide duplicados — O(1) add/contains |
| `LinkedList<Shot>`    | `GameSession.shotHistory` | O(1) al añadir al final — historial de disparos en orden cronológico |
| `ArrayDeque<Coordinate>` | `RandomHuntStrategy.huntQueue` | O(1) add/poll en ambos extremos — cola de celdas candidatas en modo caza |

---

## Concurrencia

Dos hilos de fondo manejan las operaciones que no deben bloquear la UI:

**`FleetPlacementThread`** — hilo daemon que ejecuta `RandomFleetPlacer.place()` en segundo plano mientras el humano está en la fase de colocación. Al terminar, notifica al controlador vía `Platform.runLater()`. Como la máquina es el único hilo que accede a su propio tablero en ese momento, no se requiere sincronización externa.

**`MachineTurnThread`** — hilo daemon que duerme 1.2 segundos simulando el "pensamiento" de la máquina, luego ejecuta el disparo dentro de un bloque `synchronized(session)` usando la sesión como monitor. El controlador del humano adquiere el mismo monitor antes de leer o escribir el estado, eliminando la condición de carrera entre ambos hilos. La actualización de la UI se despacha después de liberar el bloqueo mediante `Platform.runLater()`.

Ninguno de los dos hilos toca nodos de JavaFX directamente; todas las mutaciones de la escena ocurren en el hilo de la aplicación.

---

## Manejo de excepciones

Jerarquía propia de excepciones:

```
Exception (marcadas)
└── BattleShiException                ← clase base propia
    ├── InvalidPlacementException     ← superposición, adyacencia o fuera de límites al colocar
    └── PersistenceException          ← fallo de lectura/escritura de archivos de guardado

RuntimeException (no marcadas)
├── AlreadyShotException              ← disparo sobre celda ya atacada (error de UI/guard)
└── IllegalGameActionException        ← acción ilegal fuera de turno o con partida terminada
```

Las excepciones marcadas se usan cuando el llamador debe recuperarse explícitamente (mostrar feedback de colocación inválida, o cargar nueva partida ante fallo de persistencia). Las no marcadas señalan condiciones que no deberían ocurrir en un flujo correcto y se propagan sin requerir `throws` en cada método.

---

## Persistencia

El juego persiste dos tipos de datos en el directorio `home` del usuario:

**`battleshi_save.dat`** — archivo binario con la `GameSession` serializada completa (ambos tableros, historial de disparos, estado del juego y turno activo). Solo se guarda cuando el estado es `IN_PROGRESS`; al terminar la partida el archivo se elimina automáticamente para deshabilitar "Continuar" en el menú.

**`battleshi_player.txt`** — archivo de texto plano UTF-8 con el nickname del jugador, precargado en el campo de nombre al iniciar la aplicación.

Ambos archivos son gestionados por `GamePersistenceManager`, una clase utilitaria de métodos estáticos que envuelve toda `IOException` en `PersistenceException`.

---

## Pruebas unitarias

3 clases de prueba, 59 pruebas, todas pasando ✅

| Clase            | Pruebas | Qué cubre                                                                          |
|------------------|---------|------------------------------------------------------------------------------------|
| `BoardTest`      | 24      | Estado inicial, colocación (válida e inválida), eliminación de barcos, disparos, flota destruida |
| `CoordinateTest` | 22      | Construcción, límites, parsing A1–J10, `neighbor()`, `equals`, `hashCode`, `compareTo` |
| `ShipTest`       | 13      | Construcción, `occupies`, `registerHit`, `isSunk`, vista inmutable de celdas       |

Ejecutar todas las pruebas:
```bash
mvn test
```

---

## Cómo ejecutar

**Requisitos:** Java 17+, Maven 3.x

```bash
# Clonar el repositorio
git clone https://github.com/cuatrifasico/battleshi.git
cd battleshi

# Ejecutar la aplicación
mvn clean javafx:run
```

---

## Autores
- **Yostin Ramirez - 2519674**
- **Lesly Zapata - 2516574**
- **Joseph Terreros - 2521011**
- **Wilson Pinto - 2521251** 

Universidad del Valle — 750014C Fundamentos de Programación Orientada a Eventos, 2026