package de.gitlab.kaisoenmalex.api.util;

/**
 * This enum offers several rotations with degrees. This class is e.g. used in ImageManager.
 *
 * @author Alexander Ley
 * @version 1.0
 */
public enum Rotation {
    NORTH(0),
    WEST(270),
    SOUTH(180),
    EAST(90);

    private final int degrees;

    Rotation(int degrees){
        this.degrees = degrees;
    }

    public int getDegrees() {
        return degrees;
    }
}
