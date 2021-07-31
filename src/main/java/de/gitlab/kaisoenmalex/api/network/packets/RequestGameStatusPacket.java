package de.gitlab.kaisoenmalex.api.network.packets;

/**
 * This class is sent to server to request via POST Request GameStatus.
 *
 * @author Alexander Ley
 * @version 1.0
 */
public class RequestGameStatusPacket {

    private final String code;
    private final boolean isHost;

    public RequestGameStatusPacket(String code, boolean isHost) {
        this.code = code;
        this.isHost = isHost;
    }

    public String getCode() {
        return code;
    }

    public boolean isHost() {
        return isHost;
    }
}
