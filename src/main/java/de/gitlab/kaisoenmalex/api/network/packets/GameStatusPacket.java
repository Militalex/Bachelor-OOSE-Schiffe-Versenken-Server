package de.gitlab.kaisoenmalex.api.network.packets;

import de.gitlab.kaisoenmalex.api.game.GameArea;

import java.io.Serializable;

/**
 * @author Alexander Ley
 * @version 1.0
 *
 * This class holds all information about the actual game status.
 */
public class GameStatusPacket implements Serializable {

    private final String ownName, enemyName;
    private final boolean isNoob;
    private final boolean canAttack;
    private final String enemyShootText;
    private final GameArea ownArea, enemyArea;

    /**
     * @param canAttack Determine if the actual player can attack.
     * @param enemyShootText Determine if enemy has shoot water or ship.
     */
    public GameStatusPacket(String ownName, String enemyName, boolean isNoob, boolean canAttack, String enemyShootText, GameArea ownArea, GameArea enemyArea) {
        this.ownName = ownName;
        this.enemyName = enemyName;
        this.isNoob = isNoob;
        this.canAttack = canAttack;
        this.enemyShootText = enemyShootText;
        this.ownArea = ownArea;
        this.enemyArea = enemyArea;
    }

    public String getOwnName() {
        return ownName;
    }

    public String getEnemyName() {
        return enemyName;
    }

    public boolean isNoob() {
        return isNoob;
    }

    public boolean canAttack() {
        return canAttack;
    }

    public String getEnemyShootText() {
        return enemyShootText;
    }

    public GameArea getOwnArea() {
        return ownArea;
    }

    public GameArea getEnemyArea() {
        return enemyArea;
    }
}