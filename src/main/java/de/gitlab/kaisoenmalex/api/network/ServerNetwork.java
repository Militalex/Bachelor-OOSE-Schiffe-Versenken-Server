package de.gitlab.kaisoenmalex.api.network;

import de.gitlab.kaisoenmalex.api.game.BattleshipGame;
import de.gitlab.kaisoenmalex.api.network.packets.ActionPacket;
import de.gitlab.kaisoenmalex.api.util.ILogger;
import de.gitlab.kaisoenmalex.api.util.Reflector;
import de.gitlab.kaisoenmalex.api.util.TCPUtil;
import de.gitlab.kaisoenmalex.api.util.Tickable;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Alexander Ley
 * @version 1.3
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
    public void handleClientRequest(ObjectOutputStream writer, ObjectInputStream reader, ActionPacket packet, int id) {
        try {
            if (packet.getParameters().isEmpty()){
                Reflector.invokeStaticMethod(ActionHandler.class, packet.getMethodName(), writer, game, (id == 0) ? game.getHostClient() : game.getGuestClient());
            }
            else {
                final Object[] objects = new Object[packet.getParameters().size() + 1];
                objects[0] = writer;

                for (int i = 1; i <= packet.getParameters().size(); i++){
                    objects[i] = packet.getParameters().get(i - 1);
                }

                Reflector.invokeStaticMethod(ActionHandler.class, packet.getMethodName(), writer, game, (id == 0) ? game.getHostClient() : game.getGuestClient(), objects);
            }
        }
        catch (InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Cannot invoke task: " + packet.getMethodName(), e);
        }
    }

    //Methods to send things to client

    /**
     * @param id host -> 0; guest -> 1
     */
    public <T> void sendObject(T obj, int id) {
        ObjectOutputStream writer = ((id == 0) ? listener0.getObjectWriter() : listener1.getObjectWriter());

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

        private ObjectInputStream objectReader;
        private ObjectOutputStream objectWriter;

        private int aliveCheck = 0;
        private boolean hasAnswered = true;

        /**
         * @param serverSocket Socket to listen on.
         * @param id id host -> 0; guest -> 1
         */
        public Listener(ServerSocket serverSocket, int id) {
            this.serverSocket = serverSocket;
            this.id = id;

            Thread connectThread = new Thread(() ->{
                try {
                    socket = serverSocket.accept();
                }
                catch (IOException e) {
                    LOGGER.error("IO Exception on waiting for client.");
                    game.close();
                }
            });
            connectThread.start();

            start(1000, 2500);
        }

        public ObjectInputStream getObjectReader() {
            return objectReader;
        }

        public ObjectOutputStream getObjectWriter() {
            return objectWriter;
        }

        /**
         * Checks periodically for requests.
         */
        @Override
        public void tick() {
            aliveCheck++;

            if (aliveCheck > 5){
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

                    try {
                        Thread.sleep(10000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    game.close();
                }
                aliveCheck = 0;
            }
            else if (socket != null){
                try {
                    if (objectWriter == null) objectWriter = TCPUtil.getObjectWriter(socket);
                    if (objectReader == null) objectReader = TCPUtil.getObjectReader(socket);

                    try {
                        if (TCPUtil.getReader(socket).ready()) {
                            ActionPacket packet = (ActionPacket) TCPUtil.receiveObject(objectReader);
                            handleClientRequest(objectWriter, objectReader, packet, id);
                        }
                    } catch (Exception ignored) { }
                }
                catch (IOException e) {
                    LOGGER.error("I/O Error occurs when waiting for a connection. Listener will be closed.", e);
                    close();
                }
            }
        }

        public void refresh(){
            this.hasAnswered = true;
        }

        /**
         * Stops the timer and close the socket.
         */
        @Override
        public void close() {
            stop();

            try {
                objectWriter.close();
            } catch (IOException e) {
                LOGGER.info("An I/O error occurs when closing the ObjectOutputStream.", e);
            }

            try {
                objectReader.close();
            }
            catch (IOException e) {
                LOGGER.info("An I/O error occurs when closing the ObjectInputReader.", e);
            }

            try {
                serverSocket.close();
            }
            catch (IOException e) {
                LOGGER.info("An I/O error occurs when closing the socket.", e);
            }
        }
    }
}
