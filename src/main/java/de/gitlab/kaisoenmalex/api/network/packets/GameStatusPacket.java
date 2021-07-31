package de.gitlab.kaisoenmalex.api.network.packets;

import de.gitlab.kaisoenmalex.api.game.GameArea;

import java.io.Serializable;

/**
 * This class holds all information about the actual game status.
 *
 * @author Alexander Ley
 * @version 1.2
 */
public class GameStatusPacket implements Serializable {

    private final String ownName, enemyName;
    private final boolean isNoob;
    private final boolean canAttack;
    private final GameArea ownArea, enemyArea;

    /**
     * @param canAttack Determine if the actual player can attack.
     */
    public GameStatusPacket(String ownName, String enemyName, boolean isNoob, boolean canAttack, GameArea ownArea, GameArea enemyArea) {
        this.ownName = ownName;
        this.enemyName = enemyName;
        this.isNoob = isNoob;
        this.canAttack = canAttack;
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

    public GameArea getOwnArea() {
        return ownArea;
    }

    public GameArea getEnemyArea() {
        return enemyArea;
    }
}