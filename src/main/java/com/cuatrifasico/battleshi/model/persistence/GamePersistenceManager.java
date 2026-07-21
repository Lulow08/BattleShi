package com.cuatrifasico.battleshi.model.persistence;

import com.cuatrifasico.battleshi.model.entities.GameSession;
import com.cuatrifasico.battleshi.model.exceptions.PersistenceException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles all read and write operations for Battle Shi's two persistence
 * files:
 * <ol>
 *     <li><b>Binary save file</b> ({@code battleshi_save.dat}) — a Java
 *     serialized {@link GameSession} written to the user's home directory.
 *     One slot only; saving overwrites any previous save.</li>
 *     <li><b>Plain-text player file</b> ({@code battleshi_player.txt}) —
 *     stores the human player's nickname so it can be pre-filled on the
 *     next launch.  One line, UTF-8.</li>
 * </ol>
 * <p>
 * Both files live under {@code System.getProperty("user.home")} so the
 * application does not need special filesystem permissions.
 * <p>
 * All checked {@link IOException} instances are wrapped in
 * {@link PersistenceException} before propagating, keeping callers
 * decoupled from the specific I/O API.
 * <p>
 * This class is stateless; all methods are static so no instantiation
 * is needed (callers reference it directly from the controller layer).
 */
public final class GamePersistenceManager {

    /** File name for the serialized game session. */
    private static final String SAVE_FILE_NAME = "battleshi_save.dat";

    /** File name for the plain-text player record. */
    private static final String PLAYER_FILE_NAME = "battleshi_player.txt";

    private GamePersistenceManager() {
        // Utility class — not instantiable.
    }

    // ------------------------------------------------------------------ //
    //  Binary save (GameSession serialization)                             //
    // ------------------------------------------------------------------ //

    /**
     * Serializes {@code session} and writes it to the save file,
     * overwriting any existing save.
     *
     * @param session The session to persist.
     * @throws PersistenceException If the file cannot be written.
     */
    public static void saveSession(GameSession session) throws PersistenceException {
        Path target = resolveSavePath();
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(target.toFile()))) {
            oos.writeObject(session);
        } catch (IOException e) {
            throw new PersistenceException("Failed to write save file: " + target, e);
        }
    }

    /**
     * Deserializes and returns the saved {@link GameSession}, or
     * {@code null} if no save file exists.
     *
     * @return The loaded session, or {@code null} if there is no save.
     * @throws PersistenceException If the file exists but cannot be read
     *                               or parsed (e.g. corrupted or incompatible
     *                               serialized class).
     */
    public static GameSession loadSession() throws PersistenceException {
        Path source = resolveSavePath();
        if (!Files.exists(source)) {
            return null;
        }
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(source.toFile()))) {
            return (GameSession) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenceException("Failed to read save file: " + source, e);
        }
    }

    /**
     * Deletes the save file if it exists.  Called when a match ends
     * ({@link com.cuatrifasico.battleshi.model.enums.GameState#PLAYER_WON}
     * or {@link com.cuatrifasico.battleshi.model.enums.GameState#MACHINE_WON})
     * so the "Continue Game" button in the menu is disabled on the
     * next launch.
     *
     * @throws PersistenceException If the file exists but cannot be deleted.
     */
    public static void deleteSave() throws PersistenceException {
        Path target = resolveSavePath();
        if (Files.exists(target)) {
            try {
                Files.delete(target);
            } catch (IOException e) {
                throw new PersistenceException("Failed to delete save file: " + target, e);
            }
        }
    }

    /**
     * @return {@code true} if a save file exists on disk, meaning the
     *         "Continue Game" button should be enabled in the main menu.
     */
    public static boolean hasSave() {
        return Files.exists(resolveSavePath());
    }

    // ------------------------------------------------------------------ //
    //  Plain-text player file (nickname)                                  //
    // ------------------------------------------------------------------ //

    /**
     * Writes the player's nickname to the plain-text player file,
     * overwriting any previous value.
     *
     * @param nickname The nickname to store; must not be {@code null}.
     * @throws PersistenceException If the file cannot be written.
     */
    public static void saveNickname(String nickname) throws PersistenceException {
        Path target = resolvePlayerPath();
        try (BufferedWriter writer =
                     Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            writer.write(nickname);
        } catch (IOException e) {
            throw new PersistenceException("Failed to write player file: " + target, e);
        }
    }

    /**
     * Reads and returns the stored nickname, or {@code null} if the
     * player file does not exist yet.
     *
     * @return The stored nickname, or {@code null}.
     * @throws PersistenceException If the file exists but cannot be read.
     */
    public static String loadNickname() throws PersistenceException {
        Path source = resolvePlayerPath();
        if (!Files.exists(source)) {
            return null;
        }
        try {
            String content = Files.readString(source, StandardCharsets.UTF_8).trim();
            return content.isEmpty() ? null : content;
        } catch (IOException e) {
            throw new PersistenceException("Failed to read player file: " + source, e);
        }
    }

    // ------------------------------------------------------------------ //
    //  Path helpers                                                        //
    // ------------------------------------------------------------------ //

    /**
     * @return The absolute {@link Path} of the binary save file.
     */
    private static Path resolveSavePath() {
        return Paths.get(System.getProperty("user.home"), SAVE_FILE_NAME);
    }

    /**
     * @return The absolute {@link Path} of the plain-text player file.
     */
    private static Path resolvePlayerPath() {
        return Paths.get(System.getProperty("user.home"), PLAYER_FILE_NAME);
    }
}