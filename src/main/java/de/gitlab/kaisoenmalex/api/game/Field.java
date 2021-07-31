package de.gitlab.kaisoenmalex.api.game;

import de.gitlab.kaisoenmalex.api.util.ILogger;
import de.gitlab.kaisoenmalex.api.util.ImageManager;
import de.gitlab.kaisoenmalex.api.util.Rotation;
import javafx.scene.image.Image;

import java.io.Serializable;

/**
 * This class represents one field in GameArea and manage all field properties including textures.
 *
 * @author Alexander Ley
 * @version 1.2
 */
public class Field implements ILogger, Serializable {

    private final int xCord, yCord;
    private boolean isShoot;
    private boolean isShip;
    /**
     * Marks a field to say that a ship is not placeable or to visualize that water field was shoot.
     */
    private boolean isMarked;
    private final ImageManager manager = new ImageManager("water.png");

    public Field(int xCord, int yCord){
        this.xCord = xCord;
        this.yCord = yCord;
    }

    public int getXCord() {
        return xCord;
    }

    public int getYCord() {
        return yCord;
    }

    public Image getImage() {
        return manager.getImage();
    }

    public boolean isShoot() {
        return isShoot;
    }

    public boolean isShip() {
        return isShip;
    }

    public boolean isWater(){
        return !isShip;
    }

    public boolean isMarked() {
        return isMarked;
    }

    /**
     * Clear field and place a ship in field.
     * @param part Part of the Ship.
     */
    public void setShip(ShipPart part){
        this.isShip = true;
        this.isShoot = false;
        manager.addKey(part.imageKey, part.rotation);
    }

    /**
     * Clear field and place water in field.
     */
    public void setWater() {
        this.isShip = false;
        this.isShoot = false;
        try {
            manager.removeKey("ship_middle.png");
        }
        catch (IndexOutOfBoundsException ignored) { }
        try {
            manager.removeKey("ship_top.png");
        }
        catch (IndexOutOfBoundsException ignored) { }
    }

    /**
     * Marks a field to say that a ship is not placeable or to visualize that water field was shoot.
     */
    public void markField(){
        if (this.isMarked) return;

        this.isMarked = true;

        if (manager.size() > 1) manager.insertKey("shot_water.png", 1);
        else manager.addKey("shot_water.png");
    }

    /**
     * Un-marks field.
     */
    public void unmarkField(){
        if (!this.isMarked) return;

        this.isMarked = false;
        manager.removeKey("shot_water.png");
    }

    /**
     * Unmark Field and set remove ships from field.
     */
    public void clear(){
        unmarkField();
        setWater();
    }

    /**
     * Shoot field.
     * @return Returns null if field is not shoot-able, true if shoot was a hit und false if not.
     */
    public Boolean shoot(){
        if (isShoot) return null;

        this.isShoot = true;

        if (isWater()) manager.addKey("shot_water.png");
        else manager.addKey("shot_ship.png");

        return isShip();
    }

    @Override
    public String toString() {
        return (isShoot) ? "X" : (isShip) ? "S" : (isMarked) ? "C" : "O";
    }

    /**
     * Part of the Ship. Contains image and rotation.
     */
    public enum ShipPart{
        MIDDLE("ship_middle.png", Rotation.NORTH),
        TOP_NORTH("ship_top.png", Rotation.NORTH),
        TOP_WEST("ship_top.png", Rotation.WEST),
        TOP_SOUTH("ship_top.png", Rotation.SOUTH),
        TOP_EAST("ship_top.png", Rotation.EAST);

        private final String imageKey;
        private final Rotation rotation;

        ShipPart(String imageKey, Rotation rotation){
            this.imageKey = imageKey;
            this.rotation = rotation;
        }
    }
}
