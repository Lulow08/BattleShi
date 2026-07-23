package com.cuatrifasico.battleshi.view.shapes;

import com.cuatrifasico.battleshi.model.enums.Orientation;
import com.cuatrifasico.battleshi.model.enums.ShipType;
import com.cuatrifasico.battleshi.view.board.BoardTheme;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads PNG sprites from the classpath and creates {@link ImageView} overlays
 * for ships and shot markers. These overlays sit on top of the existing
 * {@link javafx.scene.shape.Shape}-based nodes, which remain responsible for
 * hit-testing, highlight feedback and CSS-driven state colors.
 *
 * <p>Sprites are cached on first use so no image is read more than once per
 * JVM run. All PNGs live directly under {@code /images/} on the classpath.</p>
 *
 * <p>Ship sprite naming convention:
 * {@code <type>_<h|v>.png} for the normal state and
 * {@code <type>_sunk_<h|v>.png} for the revealed-sunk state, where
 * {@code <type>} is derived from the lower-snake-case enum name
 * (e.g. {@code aircraft_carrier}, {@code submarine}, {@code destroyer},
 * {@code frigate}).</p>
 *
 * <p>Marker sprites: {@code hit.png} and {@code miss.png}, both 42×42 px.</p>
 */
public final class SpriteOverlayFactory {

    /** Base classpath directory for all sprites. */
    private static final String BASE_PATH = "/images/";

    /** Image cache: sprite key → loaded Image. */
    private static final Map<String, Image> CACHE = new HashMap<>();

    private SpriteOverlayFactory() {
        // Utility class, not meant to be instantiated.
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    /**
     * Creates an {@link ImageView} overlay for a ship body sprite.
     * The returned node is sized to exactly match the ship's footprint
     * ({@code size * CELL_SIZE} along its length, {@code CELL_SIZE} across).
     *
     * @param shipType    The ship category (determines which sprite file is used).
     * @param orientation The ship's current orientation.
     * @param sunk        {@code true} to use the revealed/sunk variant of the sprite.
     * @return A sized, mouse-transparent {@link ImageView} ready to add to the scene.
     */
    public static ImageView createShipOverlay(ShipType shipType, Orientation orientation, boolean sunk) {
        String key = buildShipKey(shipType, orientation, sunk);
        Image image = loadCached(key);

        double width  = orientation == Orientation.HORIZONTAL
                ? shipType.getSize() * BoardTheme.CELL_SIZE
                : BoardTheme.CELL_SIZE;
        double height = orientation == Orientation.HORIZONTAL
                ? BoardTheme.CELL_SIZE
                : shipType.getSize() * BoardTheme.CELL_SIZE;

        return buildImageView(image, width, height);
    }

    /**
     * Convenience overload for a live (not sunk) ship overlay.
     *
     * @param shipType    The ship category.
     * @param orientation The ship's current orientation.
     * @return A sized {@link ImageView} for the normal ship sprite.
     */
    public static ImageView createShipOverlay(ShipType shipType, Orientation orientation) {
        return createShipOverlay(shipType, orientation, false);
    }

    /**
     * Creates an {@link ImageView} overlay for a hit marker ({@code hit.png}).
     * The returned node is {@code CELL_SIZE × CELL_SIZE}.
     *
     * @return A sized {@link ImageView} for the hit marker sprite.
     */
    public static ImageView createHitOverlay() {
        return buildImageView(loadCached("hit"), BoardTheme.CELL_SIZE, BoardTheme.CELL_SIZE);
    }

    /**
     * Creates an {@link ImageView} overlay for a miss marker ({@code miss.png}).
     * The returned node is {@code CELL_SIZE × CELL_SIZE}.
     *
     * @return A sized {@link ImageView} for the miss marker sprite.
     */
    public static ImageView createMissOverlay() {
        return buildImageView(loadCached("miss"), BoardTheme.CELL_SIZE, BoardTheme.CELL_SIZE);
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    /**
     * Builds the cache key for a ship sprite.
     * Format: {@code <snake_type>_[sunk_]<h|v>}
     */
    private static String buildShipKey(ShipType shipType, Orientation orientation, boolean sunk) {
        String typePart   = shipType.name().toLowerCase();          // e.g. "aircraft_carrier"
        String sunkPart   = sunk ? "_sunk" : "";
        String orientPart = orientation == Orientation.HORIZONTAL ? "_h" : "_v";
        return typePart + sunkPart + orientPart;
    }

    /**
     * Returns the cached {@link Image} for the given key, loading it from
     * the classpath on first access.
     *
     * @param key The sprite key (without the .png extension).
     * @return The loaded image.
     * @throws IllegalStateException If the sprite file cannot be found on the classpath.
     */
    private static Image loadCached(String key) {
        return CACHE.computeIfAbsent(key, k -> {
            String path = BASE_PATH + k + ".png";
            var stream = SpriteOverlayFactory.class.getResourceAsStream(path);
            if (stream == null) {
                throw new IllegalStateException("Sprite not found on classpath: " + path);
            }
            return new Image(stream);
        });
    }

    /**
     * Builds a non-resizable, pixel-perfect {@link ImageView} of the given dimensions.
     * Mouse events are disabled so the Shape layer underneath handles all interaction.
     *
     * @param image  The source image.
     * @param width  Desired display width in scene units.
     * @param height Desired display height in scene units.
     * @return The configured {@link ImageView}.
     */
    private static ImageView buildImageView(Image image, double width, double height) {
        ImageView view = new ImageView(image);
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(false);
        view.setSmooth(true);
        view.setMouseTransparent(true);
        return view;
    }
}