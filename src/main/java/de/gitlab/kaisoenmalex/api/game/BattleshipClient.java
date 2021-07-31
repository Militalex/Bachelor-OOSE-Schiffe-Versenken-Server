package de.gitlab.kaisoenmalex.api.game;

import de.gitlab.kaisoenmalex.api.network.packets.ActionPacket;
import de.gitlab.kaisoenmalex.api.util.ILogger;
import org.jetbrains.annotations.Nullable;

import java.net.ServerSocket;

/**
 * This class represents a battlesipClient, stores its data and can do client stuff.
 *
 * @author Alexander Ley
 * @version 1.0
 */
public class BattleshipClient implements ILogger {

    private boolean ready = false;
    private boolean isHost;

    private final String name;
    private final ServerSocket serverSocket;

    private final GameArea area;

    @Nullable private BattleshipGame game;

    /**
     * @param name Name of client.
     * @param serverSocket ServerSocket to connect to.
     * @param area Own GameArea
     */
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

    public void setGame(@Nullable BattleshipGame game) {
        this.game = game;
    }

    public void setReady(){
        this.ready = true;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    /**
     * Abort client and send abortGame to enemy.
     * @param msg Individual message shown to enemy.
     */
    public void abort(String msg){
        if (game == null){
            getLogger().info("Client cannot aborted because game has not started yet.");
            return;
        }
        final ActionPacket packet = new ActionPacket("abortGame", msg);
        game.getNetwork().sendPacket(packet, (isHost) ? 0 : 1);
    }
}
