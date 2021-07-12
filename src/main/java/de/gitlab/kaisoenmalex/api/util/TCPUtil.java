package de.gitlab.kaisoenmalex.api.util;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Alexander Ley
 * @version 1.0
 *
 * This class offers basic tcp methods.
 */
public class TCPUtil implements ILogger{

    public static final String SERVER_ADDRESS = "http://localhost:4567/connect";
    public static final String HOST_ADDRESS = "localhost";

    public static final Gson gson = new Gson();

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
     * This method converts an object via gson to json and send it with (PrintWriter) writer.
     * @param obj Object which is send.
     * @param <T> Object type that is saved to json.
     */
    public static <T> void sendObject(PrintWriter writer, T obj){
        final String json = gson.toJson(obj, new TypeToken<T>(){}.getType());
        writer.println(json);
    }

    /**
     * Tries to read an object from BufferedReader.
     * @param <T> Object type that was saved to json.
     * @return Returns the json decrypted object.
     * @throws IllegalStateException when BufferedReader is not readable.
     * @throws JsonParseException when json cannot decrypted to object.
     */
    public static <T> T receiveObject(BufferedReader reader, Class<T> clazz){
        try {
            if (!reader.ready()) {
                STATIC_LOGGER.warn("BufferedReader is not readable.");
                throw new IllegalStateException("BufferedReader is not readable.");
            }

            final String json = reader.readLine();
            return gson.fromJson(json, clazz);
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
     * @param typeToken have to be new TypeToken<>(){}
     * @param <T> Object type that was saved to json.
     * @return Returns the json decrypted object.
     * @throws IllegalStateException when BufferedReader is not readable.
     * @throws JsonParseException when json cannot decrypted to object.
     */
    public static <T> T receiveObject(BufferedReader reader, TypeToken<T> typeToken){
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
}
