package de.gitlab.kaisoenmalex.api.util;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import de.gitlab.kaisoenmalex.api.network.packets.ActionPacket;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

/**
 * This class offers' tcp methods.
 *
 * @author Alexander Ley
 * @version 1.3.5
 */
public class TCPUtil implements ILogger{

    public static final String SERVER_ADDRESS = "http://schiffe.milinphostor.de:4567/";
    public static final String HOST_ADDRESS = "schiffe.milinphostor.de";

    public static final Gson gson = new Gson();

    /**
     * @return Returns an object writer (is used to send objects) from socket.
     * @throws IOException when an I/O error occurs when creating the output stream or if the socket is not connected.
     */
    public static ObjectOutputStream getObjectWriter(Socket socket) throws IOException {
        return new ObjectOutputStream(socket.getOutputStream());
    }

    /**
     * @return Returns a writer (is used to send things) from socket.
     * @throws IOException when an I/O error occurs when creating the output stream or if the socket is not connected.
     */
    public static PrintWriter getWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * @return Returns a BufferedReader to read from tcp connection.
     * @throws IOException when there are no bytes buffered on the socket, or all buffered bytes have been consumed by read.
     */
    public static BufferedReader getReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * @return Returns an object reader to read objects from tcp connection.
     * @throws IOException when there are no bytes buffered on the socket, or all buffered bytes have been consumed by read.
     */
    public static ObjectInputStream getObjectReader(Socket socket) throws IOException {
        return new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Sends an object via outputStream.
     * @return Returns id object was successful send. Note: All exceptions are fatal to the OutputStream,
     * which is left in an indeterminate state.
     */
    public static <T> boolean sendObject(ObjectOutputStream stream, T obj) {
        ILogger.STATIC_LOGGER.info("Send: " + (((obj instanceof ActionPacket)) ? ((ActionPacket) obj).getMethodName() + ": ": "") + obj);

        try {
            stream.writeObject(obj);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * This method converts an object via gson to json and send it with (PrintWriter) writer.
     * @param obj Object which is send.
     * @param <T> Object type that is saved to json.
     */
    public static <T> void sendGsonObject(PrintWriter writer, T obj){
        final String json = gson.toJson(obj, new TypeToken<T>(){}.getType());
        System.out.println(json);
        writer.println(json);
    }

    public static Object receiveObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        final Object obj = stream.readObject();
        ILogger.STATIC_LOGGER.info("Received: " + (((obj instanceof ActionPacket)) ? ((ActionPacket) obj).getMethodName() + ": ": "") + obj);
        return obj;
    }

    /**
     * Tries to read an object from BufferedReader.
     * @param <T> Object type that was saved to json.
     * @return Returns the json decrypted object.
     * @throws IllegalStateException when BufferedReader is not readable.
     * @throws JsonParseException when json cannot decrypted to object.
     */
    public static <T> T receiveGsonObject(BufferedReader reader, Class<T> clazz){
        try {
            if (!reader.ready()) {
                STATIC_LOGGER.warn("BufferedReader is not readable.");
                throw new IllegalStateException("BufferedReader is not readable.");
            }

            StringBuilder json = new StringBuilder();

            while (reader.ready()){
                json.append(reader.readLine());
            }

            // String json = reader.readLine();
            System.out.println(json);
            return gson.fromJson(json.toString(), clazz);
        }
        catch (JsonParseException e){
            STATIC_LOGGER.warn("Cannot get Object from json.", e);
            throw e;
        }
        catch (IOException e) {
            STATIC_LOGGER.error("An I/O error occurs.", e);
            throw new IllegalStateException("BufferedReader is not readable.", e);
        }
    }

    /**
     * Tries to read an object from BufferedReader.
     * @param typeToken have to be new TypeToken<T>(){}
     * @param <T> Object type that was saved to json.
     * @return Returns the json decrypted object.
     * @throws IllegalStateException when BufferedReader is not readable.
     * @throws JsonParseException when json cannot decrypted to object.
     */
    public static <T> T receiveGsonObject(BufferedReader reader, TypeToken<T> typeToken){
        try {
            if (!reader.ready()) {
                STATIC_LOGGER.warn("BufferedReader is not readable.");
                throw new IllegalStateException("BufferedReader is not readable.");
            }
            final String json = reader.readLine();
            return gson.fromJson(json, typeToken.getType());
        }
        catch (JsonParseException e){
            STATIC_LOGGER.warn("Cannot get Object from json.", e);
            throw e;
        }
        catch (IOException e) {
            STATIC_LOGGER.error("An I/O error occurs.", e);
            throw new IllegalStateException("BufferedReader is not readable.", e);
        }
    }

    /**
     * Tests if port is connectable.
     * @return Returns true if port is connectable.
     */
    public static boolean isSocketAlive(int port) {
        boolean isAlive = false;

        // Creates a socket address from a hostname and a port number
        SocketAddress socketAddress = new InetSocketAddress(HOST_ADDRESS, port);
        Socket socket = new Socket();

        // Timeout required - it's in milliseconds
        int timeout = 2000;

        try {
            socket.connect(socketAddress, timeout);
            socket.close();
            isAlive = true;
        }
        catch (SocketTimeoutException exception) {
            System.out.println("SocketTimeoutException " + HOST_ADDRESS + ":" + port + ". " + exception.getMessage());
        }
        catch (IOException exception) {
            System.out.println(
                    "IOException - Unable to connect to " + HOST_ADDRESS + ":" + port + ". " + exception.getMessage());
        }
        return isAlive;
    }
}
