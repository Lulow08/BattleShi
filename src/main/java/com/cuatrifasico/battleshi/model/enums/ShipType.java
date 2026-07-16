package com.cuatrifasico.battleshi.model.enums;

/**
 * Defines the four ship categories available in the fleet, along with
 * the number of cells each ship occupies and how many units of that
 * type must be placed on a single board.
 */
public enum ShipType {

    AIRCRAFT_CARRIER("Aircraft Carrier", 4, 1),
    SUBMARINE("Submarine", 3, 2),
    DESTROYER("Destroyer", 2, 3),
    FRIGATE("Frigate", 1, 4);

    private final String displayName;
    private final int size;
    private final int fleetCount;

    ShipType(String displayName, int size, int fleetCount) {
        this.displayName = displayName;
        this.size = size;
        this.fleetCount = fleetCount;
    }

    /**
     * @return The human-readable name of the ship type.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return The number of cells this ship type occupies on the board.
     */
    public int getSize() {
        return size;
    }

    /**
     * @return The number of ships of this type present in a full fleet.
     */
    public int getFleetCount() {
        return fleetCount;
    }
}