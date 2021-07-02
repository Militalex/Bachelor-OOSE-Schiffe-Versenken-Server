package de.gitlab.kaisoenmalex.api.network.packets;

import de.gitlab.kaisoenmalex.api.game.GameArea;

/**
 * @author Alexander Ley
 * @version 1.0
 *
 * This class stores data which is send to server by client to connect the first time.
 */
public class ConnectPacket {
    private final String name;
    private final boolean isHost;
    private final boolean noobVariant;
    private final boolean randomEnemy;
    private final String code;
    private final GameArea area;

    /**
     * @param name Name of client.
     * @param randomEnemy Boolean if the client want to have a code or if the client want a random enemy.
     * @param code if client is not host he send a code to match with host.
     * @param area Ships
     */
    public ConnectPacket(String name, boolean isHost, boolean noobVariant, boolean randomEnemy, String code, GameArea area) {
        this.name = name;
        this.isHost = isHost;
        this.noobVariant = noobVariant;
        this.randomEnemy = randomEnemy;
        this.code = code;
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public boolean isHost() {
        return isHost;
    }

    public boolean isNoobVariant() {
        return noobVariant;
    }

    public boolean isRandomEnemy() {
        return randomEnemy;
    }

    public String getCode() {
        return code;
    }

    public GameArea getArea() {
        return area;
    }
}
