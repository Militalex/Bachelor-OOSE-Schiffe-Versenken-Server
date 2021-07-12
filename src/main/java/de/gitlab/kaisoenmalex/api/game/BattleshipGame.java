package de.gitlab.kaisoenmalex.api.game;

import de.gitlab.kaisoenmalex.api.network.ServerNetwork;
import de.gitlab.kaisoenmalex.api.network.packets.ActionPacket;

import java.io.Closeable;

/**
 * @author Alexander Ley
 * @version 0.0
 *
 * This Class is not complete yet.
 */
public class BattleshipGame implements Closeable {

    private String code;
    private final boolean isNoob;

    private ServerNetwork serverNetwork;

    private BattleshipClient hostClient, guestClient;

    public BattleshipGame(String code, boolean isNoob, BattleshipClient hostClient, BattleshipClient guestClient) {
        this.code = code;
        this.isNoob = isNoob;
        this.serverNetwork = new ServerNetwork(this, hostClient.getServerSocket(), guestClient.getServerSocket());
        this.hostClient = hostClient;
        this.guestClient = guestClient;
    }

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

    public boolean isNoob() {
        return isNoob;
    }

    public BattleshipClient getHostClient() {
        return hostClient;
    }

    public BattleshipClient getGuestClient() {
        return guestClient;
    }

    public void startGame(){
        if (hostClient.isReady() && guestClient.isReady()) {
            final ActionPacket packet = new ActionPacket("startGame");
            serverNetwork.sendObject(packet, 0);
            serverNetwork.sendObject(packet, 1);
        }
    }

    @Override
    public void close() {
        if (serverNetwork != null){
            serverNetwork.close();
        }
    }
}
