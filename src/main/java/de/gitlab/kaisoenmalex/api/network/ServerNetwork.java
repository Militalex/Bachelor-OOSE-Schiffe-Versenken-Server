package de.gitlab.kaisoenmalex.api.network;

import de.gitlab.kaisoenmalex.api.game.BattleshipGame;
import de.gitlab.kaisoenmalex.api.util.ILogger;
import de.gitlab.kaisoenmalex.api.util.TCPUtil;
import de.gitlab.kaisoenmalex.api.util.Tickable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * @author Alexander Ley
 * @version 1.0
 *
 * This Class helps to communicate with Clients. It can send things to client and handle client requests.
 */
public class ServerNetwork implements ILogger, Closeable {

    private final Logger LOGGER = getLogger();

    /**
     * Listener for host (listener0) and guest (listener1).
     */
    private final Listener listener0, listener1;
    private final BattleshipGame game;

    /**
     * PrintWriter for host and guest. Are only assigned after the first request handle.
     */
    private PrintWriter writer0, writer1;

    public ServerNetwork(BattleshipGame game, ServerSocket socket1, ServerSocket socket2) {
        this.game = game;

        //host -> 0; guest -> 1

        listener0 = new Listener(socket1, 0);
        listener1 = new Listener(socket2, 1);
    }

    /**
     * This methods handles all client requests.
     */
    public void handleClientRequest(PrintWriter writer, BufferedReader reader, int id){
        //TODO: Define Reactions of server for client requests.
    }

    //Methods to send things to client

    /**
     * @param id host -> 0; guest -> 1
     * @throws IllegalStateException when writer is not assigned.
     */
    public void sendToClient(String msg, int id){
        PrintWriter writer = ((id == 0) ? writer0 : writer1);

        if (writer == null) throw new IllegalStateException("Writer is not assigned. Wait until first client request handle.");

        writer.println(msg);
    }

    /**
     * @param id host -> 0; guest -> 1
     * @throws IllegalStateException when writer is not assigned.
     */
    public void sendToClient(List<String> msgList, int id){
        msgList.forEach(s -> sendToClient(s, id));
    }

    /**
     * @param id host -> 0; guest -> 1
     * @throws IllegalStateException when writer is not assigned.
     */
    public <T> void sendObject(T obj, int id){
        PrintWriter writer = ((id == 0) ? writer0 : writer1);

        if (writer == null) throw new IllegalStateException("Writer is not assigned. Wait until first client request handle.");

        TCPUtil.sendObject(writer, obj);
    }

    /**
     * Close network -> all sockets will be closed.
     */
    @Override
    public void close() {
        listener0.close();
        listener1.close();
    }

    /**
     * This inner class listen for client requests and fires the handleClientRequest method.
     */
    private class Listener extends Tickable implements Closeable {

        private final ServerSocket serverSocket;
        private final int id;

        /**
         * @param serverSocket Socket to listen on.
         * @param id id host -> 0; guest -> 1
         */
        public Listener(ServerSocket serverSocket, int id) {
            this.serverSocket = serverSocket;
            this.id = id;

            start(2000, 5000);
        }

        /**
         * Checks periodically for requests.
         */
        @Override
        public void tick() {
            try {
                //Wait for request.
                final Socket socket = serverSocket.accept();
                final PrintWriter writer = TCPUtil.getWriter(socket);

                if (id == 0) writer0 = writer;
                else  writer1 = writer;

                try {
                    handleClientRequest(writer, TCPUtil.getReader(socket), id);
                }
                catch (IOException ignored) { }
            }
            catch (IOException e) {
                LOGGER.error("I/O Error occurs when waiting for a connection. Listener will be closed.", e);
                close();
            }
        }

        /**
         * Stops the timer and close the socket.
         */
        @Override
        public void close() {
            stop();
            try {
                serverSocket.close();
            }
            catch (IOException e) {
                LOGGER.warn("An I/O error occurs when closing the socket.", e);
            }
        }
    }
}
