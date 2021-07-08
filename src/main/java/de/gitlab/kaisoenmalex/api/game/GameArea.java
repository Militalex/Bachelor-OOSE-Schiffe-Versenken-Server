package de.gitlab.kaisoenmalex.api.game;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * @author Alexander Ley
 * @version 1.1
 *
 * This class controls gameArea and offers Methods to place ships, shoot and other cool staff.
 */
public class GameArea {

    private final Field[][] area = new Field[10][10];

    /**
     * Initialize GameArea with water fields.
     */
    public GameArea(){
        for (int x = 0; x < 10; x++){
            for (int y = 0; y < 10; y++){
                area[x][y] = new Field(y, x);
            }
        }
    }

    /**
     * @return Returns field at (x, y).
     */
    public Field getField(int x, int y){
        if (x < 0 || x >= 10 || y < 0 || y >= 10) throw new IllegalArgumentException("Coordinates: (" + x + ", " + y + ") are illegal.");
        return area[y][x];
    }

    /**
     * @param x xCord of left top corner of ship.
     * @param y yCord of left top corner of ship.
     * @return Returns true if Ship is placeable.
     */
    public boolean isShipPlaceable(int x, int y, int length, boolean vertical){
        if (x < 0 || x >= 10 || y < 0 || y >= 10 || ((vertical) ? y : x) + length > 10) return false;

        int xy = (vertical) ? x : y;

        for (int i = 0; i < length; i++){
            final Field field = getField((vertical) ? xy : x + i, (vertical) ? y + i : xy);

            if (field.isMarked() || field.isShip()) return false;
        }

        return true;
    }

    /**
     * Set mark around (x, y).
     */
    public void markAround(int x, int y){

        for (int ix = x - 1; ix <= x + 1; ix++){
            for (int iy = y - 1; iy <= y + 1; iy++){
                if (ix < 0 || ix >= 10 || iy < 0 || iy >= 10) continue;

                Field field = getField(ix, iy);

                if (field.isMarked()) continue;

                field.markField();
            }
        }
    }

    /**
     * Remove mark around. Marks where ships nearby stay.
     */
    public void unmarkAround(int x, int y){
        for (int ix = x - 1; ix <= x + 1; ix++){
            for (int iy = y - 1; iy <= y + 1; iy++){
                if (ix < 0 || ix >= 10 || iy < 0 || iy >= 10) continue;

                Field field = getField(ix, iy);

                if (field.isShip() || isShipAround(ix, iy)) continue;

                field.setWater();
            }
        }
    }

    /**
     * @return Returns true if ship is around (x, y).
     */
    public boolean isShipAround(int x, int y){
        for (int ix = x - 1; ix <= x + 1; ix++){
            for (int iy = y - 1; iy <= y + 1; iy++){
                if (ix < 0 || ix >= 10 || iy < 0 || iy >= 10) continue;

                Field field = getField(ix, iy);

                if (field.isShip()) return true;
            }
        }
        return false;
    }

    /**
     * Tries to place a ship and marks area around.
     * @param x xCord of left top corner of ship.
     * @param y yCord of left top corner of ship.
     * @return Returns true if ship was placed and false if not.
     */
    public boolean placeShip(int x, int y, int length, boolean vertical){
        if (!isShipPlaceable(x, y, length, vertical)) return false;

        int xy = (vertical) ? x : y;

        for (int i = 0; i < length; i++){
            final Field field = getField((vertical) ? xy : x + i, (vertical) ? y + i : xy);

            Field.ShipPart part = (i == 0) ? (
                    (vertical) ? Field.ShipPart.TOP_NORTH : Field.ShipPart.TOP_WEST
            ) : (i == length - 1) ? (
                    (length == 2) ? Field.ShipPart.MIDDLE :
                    (vertical) ? Field.ShipPart.TOP_SOUTH : Field.ShipPart.TOP_EAST
            ) : Field.ShipPart.MIDDLE;

            field.setShip(part);
            markAround(field.getXCord(), field.getYCord());
        }

        return true;
    }

    /**
     * Removes ship and unmarks area around ship.
     * @param x xCord of any part of ship.
     * @param y yCord of any part of ship.
     * @return Returns length of removed ship and 0 if nothing was removed.
     */
    public int removeShip(int x, int y){
        if (x < 0 || x >= 10 || y < 0 || y >= 10) return 0;

        Field field = getField(x, y);

        if (!field.isShip()) return 0;

        return removeShipRek(x, y);
    }

    /**
     * Recursive removeShip Method.
     * @param x xCord of any part of ship.
     * @param y yCord of any part of ship.
     * @return Returns sub-length of removed ship and 0 if nothing was removed.
     */
    private int removeShipRek(int x, int y){
        if (x < 0 || x >= 10 || y < 0 || y >= 10) return 0;

        Field field = getField(x, y);

        if (!field.isShip()) return 0;

        field.setWater();

        int deep = removeShipRek(x + 1, y) + removeShipRek(x - 1, y)
                + removeShipRek(x, y + 1) + removeShipRek(x, y -1) + 1;

        unmarkAround(x, y);

        return deep;
    }

    /**
     * Shoot field at (x, y).
     * @return Returns null if field is not shot-able, true if hit and false otherwise.
     */
    @Nullable
    public Boolean shootField(int x, int y){
        if (x < 0 || x >= 10 || y < 0 || y >= 10) return null;

        final Field field = getField(x, y);

        if (field.isShoot()) return null;

        return field.shoot();
    }

    /**
     * [Debug] Prints gameArea into console.
     */
    public void printArea(){
        System.out.println("-----------------------------------");
        Arrays.stream(area).forEach(fields -> {

            StringBuilder builder = new StringBuilder();

            Arrays.stream(fields).forEach(field ->
                    builder.append(" ").append(field));

            System.out.println(builder);
        });
    }
}
