package de.gitlab.kaisoenmalex.api.game;

import java.net.ServerSocket;

/**
 * @author Alexander Ley
 * @version 0.0
 *
 * This Class is not complete yet.
 */
public class BattleshipClient {

    private boolean ready = false;
    private boolean isHost;

    private final String name;
    private final ServerSocket serverSocket;

    private final GameArea area;

    public BattleshipClient(String name, ServerSocket serverSocket, GameArea area) {
        this.name = name;
        this.serverSocket = serverSocket;
        this.area = area;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isHost() {
        return isHost;
    }

    public String getName() {
        return name;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public GameArea getArea() {
        return area;
    }

    public void setReady(){
        this.ready = true;
    }

    public void setHost(boolean host) {
        isHost = host;
    }
}
