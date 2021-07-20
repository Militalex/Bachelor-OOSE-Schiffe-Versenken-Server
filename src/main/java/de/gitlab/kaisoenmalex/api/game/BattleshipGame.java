package de.gitlab.kaisoenmalex.api.game;

import de.gitlab.kaisoenmalex.api.network.ServerNetwork;
import de.gitlab.kaisoenmalex.api.network.packets.ActionPacket;
import de.gitlab.kaisoenmalex.server.BattleshipServer;

import java.io.Closeable;

/**
 * @author Alexander Ley
 * @version 1.0.1
 *
 * This class represents a BattleshipGame.
 */
public class BattleshipGame implements Closeable {

    private String code;
    /**
     * Text Buffer to say if enemy has shot water or ship.
     */
    private String shootBuffer = "empty";
    private final boolean isNoob;
    private boolean canHostAttack = true;

    private ServerNetwork serverNetwork;

    private BattleshipClient hostClient, guestClient;

    /**
     * @param code Identification code.
     */
    public BattleshipGame(String code, boolean isNoob, BattleshipClient hostClient, BattleshipClient guestClient) {
        this.code = code;
        this.isNoob = isNoob;
        this.hostClient = hostClient;
        this.guestClient = guestClient;
        guestClientJoin(guestClient);
    }

    /**
     * Note in this state the game is not complete. Game is in waiting for enemy state.
     * @param code Identification code.
     */
    public BattleshipGame(String code, boolean isNoob, BattleshipClient hostClient){
        this.code = code;
        this.isNoob = true;
        this.hostClient = hostClient;
    }

    public void guestClientJoin(BattleshipClient guestClient){
        this.guestClient = guestClient;
        this.serverNetwork = new ServerNetwork(this, hostClient.getServerSocket(), guestClient.getServerSocket());
    }

    public String getCode() {
        return code;
    }

    public String getShootBuffer() {
        final String ret = shootBuffer;
        shootBuffer = "empty";
        return shootBuffer;
    }

    public boolean isNoob() {
        return isNoob;
    }

    public ServerNetwork getNetwork() {
        return serverNetwork;
    }

    public BattleshipClient getHostClient() {
        return hostClient;
    }

    public BattleshipClient getGuestClient() {
        return guestClient;
    }

    public boolean canHostAttack() {
        return canHostAttack;
    }

    public boolean canGuestAttack(){
        return !canHostAttack;
    }

    /**
     * Starts game and send startGame packet to clients.
     */
    public void startGame(){
        if (hostClient.isReady() && guestClient.isReady()) {
            final ActionPacket hostPacket = new ActionPacket("startGame", true);
            serverNetwork.sendPacket(hostPacket, 0);

            final ActionPacket guestPacket = new ActionPacket("startGame", false);
            serverNetwork.sendPacket(guestPacket, 1);
        }
    }

    /**
     * Close network and remove game from database.
     */
    @Override
    public void close() {
        if (serverNetwork != null){
            serverNetwork.close();
        }

        BattleshipServer.ACTIVE_GAMES.remove(code);
        BattleshipServer.WAITING_GAMES_CODE.remove(code);
        BattleshipServer.WAITING_GAMES_RANDOM.remove(code);
    }
}
