package de.gitlab.kaisoenmalex.api.network;

import de.gitlab.kaisoenmalex.api.game.BattleshipGame;
import de.gitlab.kaisoenmalex.api.network.packets.ActionPacket;
import de.gitlab.kaisoenmalex.api.util.ILogger;
import de.gitlab.kaisoenmalex.api.util.Reflector;
import de.gitlab.kaisoenmalex.api.util.TCPUtil;
import de.gitlab.kaisoenmalex.api.util.Tickable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.List;

/**
 * @author Alexander Ley
 * @version 1.1
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

    public Listener getHostListener() {
        return listener0;
    }

    public Listener getGuestListener() {
        return listener1;
    }

    /**
     * This methods handles all client requests.
     */
    public void handleClientRequest(PrintWriter writer, BufferedReader reader, int id){
        final ActionPacket packet = TCPUtil.receiveObject(reader, ActionPacket.class);
        try {
            if (packet.getParameters().isEmpty()){
                Reflector.invokeStaticMethod(ActionHandler.class, packet.getMethodName(), writer, game, (id == 0) ? game.getHostClient() : game.getGuestClient());
            }
            else {
                Reflector.invokeStaticMethod(ActionHandler.class, packet.getMethodName(), writer, game, (id == 0) ? game.getHostClient() : game.getGuestClient(), packet.getParameters().toArray());
            }
        }
        catch (InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Cannot invoke task: " + packet.getMethodName(), e);
        }
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
    public class Listener extends Tickable implements Closeable {

        private final ServerSocket serverSocket;
        private Socket socket;
        private final int id;

        private final Tickable aliveChecker;
        private boolean hasAnswered = true;

        /**
         * @param serverSocket Socket to listen on.
         * @param id id host -> 0; guest -> 1
         */
        public Listener(ServerSocket serverSocket, int id) {
            this.serverSocket = serverSocket;
            this.id = id;

            this.aliveChecker = new Tickable() {
                @Override
                public void tick() {
                    if (hasAnswered) {
                        System.out.println("Test Alive");
                        final ActionPacket packet = new ActionPacket("isAlive");
                        sendObject(packet, id);
                        hasAnswered = false;
                    }
                    else {
                        System.out.println("Abort Game");
                        final ActionPacket stopPacket = new ActionPacket("abortGame");
                        sendObject(stopPacket, 0);
                        sendObject(stopPacket, 1);

                        game.close();
                    }
                }
            };

            start(2000, 2500);
        }

        /**
         * Checks periodically for requests.
         */
        @Override
        public void tick() {
            try {
                //Wait for client to connect
                if (socket == null) {
                    socket = serverSocket.accept();
                }

                final PrintWriter writer = TCPUtil.getWriter(socket);

                if (id == 0) writer0 = writer;
                else  writer1 = writer;

                final BufferedReader reader = TCPUtil.getReader(socket);

                if (reader.ready()){
                    handleClientRequest(writer, reader, id);
                }
            }
            catch (IOException e) {
                LOGGER.error("I/O Error occurs when waiting for a connection. Listener will be closed.", e);
                close();
            }
        }

        public void refresh(){
            this.hasAnswered = true;
        }

        public void startAliveChecker(){
            if (this.aliveChecker.isReady()) this.aliveChecker.start(10000, 15000);
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
            aliveChecker.stop();
        }
    }
}
