package de.gitlab.kaisoenmalex.server;

import com.google.gson.Gson;
import de.gitlab.kaisoenmalex.api.game.BattleshipClient;
import de.gitlab.kaisoenmalex.api.game.BattleshipGame;
import de.gitlab.kaisoenmalex.api.network.ActionHandler;
import de.gitlab.kaisoenmalex.api.network.packets.ActionPacket;
import de.gitlab.kaisoenmalex.api.network.packets.ConnectPacket;
import de.gitlab.kaisoenmalex.api.network.packets.GameStatusPacket;
import de.gitlab.kaisoenmalex.api.network.packets.RequestGameStatusPacket;
import de.gitlab.kaisoenmalex.api.util.ILogger;
import de.gitlab.kaisoenmalex.api.util.Reflector;
import de.gitlab.kaisoenmalex.api.util.Tuple;
import spark.Spark;

import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author Alexander Ley
 * @version 1.6
 *
 * This Class is the entry Point for the Server Application.
 */
public class BattleshipServer {

    public static final Gson gson = new Gson();

    public static final HashMap<String, BattleshipGame> ACTIVE_GAMES = new HashMap<>();

    public static final HashMap<String, BattleshipGame> WAITING_GAMES_CODE = new HashMap<>();
    public static final HashMap<String, BattleshipGame> WAITING_GAMES_RANDOM = new HashMap<>();

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ACTIVE_GAMES.forEach((s, battleshipGame) -> battleshipGame.close());
            WAITING_GAMES_CODE.forEach((s, battleshipGame) -> battleshipGame.close());
            WAITING_GAMES_RANDOM.forEach((s, game) -> game.close());
        }));

        /*
         * Define post connect request.
         * Used for first client registration and its game into database.
         *
         * Client sends information about game and code and server will either start a game if
         * client only wants to join or server will create a new game and adds client into queue.
         *
         * In all cases the server sends code for verification and port to connect via tcp, except
         * something went wrong. If so the server will return null.
         */
        Spark.post("/connect", (request, response) -> {

            final ConnectPacket connectPacket = gson.fromJson(request.body(), ConnectPacket.class);

            //Generate free socket
            ServerSocket serverSocket = new ServerSocket(0);
            System.out.println(serverSocket.getLocalPort());

            //Client is created
            final BattleshipClient client = new BattleshipClient(connectPacket.getName(), serverSocket, connectPacket.getArea());

            //If client is an host or client wants to match with random enemy
            if (connectPacket.isHost()){
                final String code = generateCode();

                //Random Enemy
                if (connectPacket.isRandomEnemy()){
                    //If there is waiting random player match both
                    if (WAITING_GAMES_RANDOM.size() >= 1){
                        client.setHost(false);

                        final BattleshipGame host = WAITING_GAMES_RANDOM.values().stream().findFirst().orElse(null);
                        assert host != null;

                        host.guestClientJoin(client);
                        ACTIVE_GAMES.put(host.getCode(), host);
                        WAITING_GAMES_RANDOM.remove(host.getCode());

                        return gson.toJson(new Tuple<>(host.getCode(), serverSocket.getLocalPort()));
                    }
                    //If there is no waiting player add player into queue
                    else {
                        client.setHost(true);
                        final BattleshipGame game = new BattleshipGame(code, connectPacket.isNoobVariant(), client);
                        WAITING_GAMES_RANDOM.put(code, game);

                        return gson.toJson(new Tuple<>(code, serverSocket.getLocalPort()));
                    }
                }
                //Host Player with code
                else {
                    client.setHost(true);
                    final BattleshipGame game = new BattleshipGame(code, connectPacket.isNoobVariant(), client);
                    WAITING_GAMES_CODE.put(code, game);

                    return gson.toJson(new Tuple<>(code, serverSocket.getLocalPort()));
                }
            }
            //Joining Player with code
            else{
                client.setHost(false);

                final String code = connectPacket.getCode();
                final BattleshipGame host = WAITING_GAMES_CODE.get(code);

                if (host == null) return null;

                host.guestClientJoin(client);
                ACTIVE_GAMES.put(host.getCode(), host);
                WAITING_GAMES_CODE.remove(host.getCode());


                return gson.toJson(new Tuple<>(host.getCode(), serverSocket.getLocalPort()));
            }
        });

        /*
         * Defines gameStatus post request.
         *
         * Client can get every time information about his game by sending verfication code and
         * information if client is host or not. Based on this information server will pack
         * a gameStatusPacket (which can not send via tcp) and send it back to client using gson.
         */
        Spark.post("/gameStatus", (request, response) -> {
            final RequestGameStatusPacket packet = gson.fromJson(request.body(), RequestGameStatusPacket.class);

            final BattleshipGame game = ACTIVE_GAMES.get(packet.getCode());

            if (game == null) return null;

            final boolean isHost = packet.isHost();
            final GameStatusPacket statusPacket = new GameStatusPacket(
                    (isHost) ? game.getHostClient().getName() : game.getGuestClient().getName(),
                    (isHost) ? game.getGuestClient().getName() : game.getHostClient().getName(),
                    game.isNoob(), (isHost) ? game.canHostAttack() : game.canGuestAttack(),
                    (isHost) ? game.getHostClient().getArea() : game.getGuestClient().getArea(),
                    (isHost) ? game.getGuestClient().getArea() : game.getHostClient().getArea()
            );

            return gson.toJson(statusPacket);
        });

        /*
        * Defines waitingAbort Request.
        *
        * Clients can request for game to abort before actual game has started.
         */
        Spark.post("/waitingAbort", (request, response) -> {
            final String code = request.body();

            BattleshipGame game = WAITING_GAMES_CODE.get(code);
            if (game == null) game = WAITING_GAMES_RANDOM.get(code);
            if (game == null) return false;

            game.close();

            return true;
        });

        /*
         * Defines async GameAction post request.
         *
         * This request enables client do server actions asynchronously.
         */
        Spark.post("/asyncAction", (request, response) -> {
            final ActionPacket packet = gson.fromJson(request.body(), ActionPacket.class);
            final List<Object> parameters = packet.getParameters();

            ILogger.STATIC_LOGGER.info("Async Received: " + packet.getMethodName() + ": " + packet);

            if (parameters == null) return null;

            final BattleshipGame game;
            final boolean isHost;

            try {
                game = ACTIVE_GAMES.get((String) packet.getParameters().get(0));
                isHost = (Boolean) packet.getParameters().get(1);
            }
            catch (ClassCastException e){
                ILogger.STATIC_LOGGER.warn("Wrong request to /asyncAction");
                return null;
            }

            final BattleshipClient client = (isHost) ? game.getHostClient() : game.getGuestClient();
            final ObjectOutputStream writer = ((isHost) ? game.getNetwork().getHostListener() : game.getNetwork().getGuestListener()).getObjectWriter();

            try {
                if (packet.getParameters().size() == 2){
                    Reflector.invokeStaticMethod(ActionHandler.class, packet.getMethodName(), writer, game, client);
                }
                else {
                    final Object[] objects = new Object[packet.getParameters().size() + 1];

                    objects[0] = writer;
                    objects[1] = game;
                    objects[2] = client;

                    for (int i = 3; i < packet.getParameters().size() + 1; i++){
                        objects[i] = packet.getParameters().get(i - 1);
                    }

                    Reflector.invokeStaticMethod(ActionHandler.class, packet.getMethodName(), objects);
                }
            }
            catch (InvocationTargetException | IllegalAccessException e) {
                ILogger.STATIC_LOGGER.error("Cannot invoke task: " + packet.getMethodName(), e);
                return null;
            }
            return true;
        });

        /*
         * Enables users from outside to get status server information  (only for debug reasons).
         */
        Spark.get("/info", (request, response) -> "Active Games: " + ACTIVE_GAMES.size() +
                "\n" +
                "Waiting Clients with Code: " + WAITING_GAMES_CODE.size() +
                "\n" +
                "Waiting Clients without Code: " + WAITING_GAMES_RANDOM.size() +
                "\n");

        /*
        * Enables users from outside to stop all games (only for debug reasons).
        */
        Spark.get("/clear", (request, response) -> {
            clearGames();
            return true;
        });

        /*
        * Enables users from outside to shutdown server (only for debug reasons).
        */
        Spark.get("/stop", (request, response) -> {
            clearGames();
            Spark.stop();
            return true;
        });
    }

    /**
     * Closes all games.
     */
    public static void clearGames(){
        boolean flag = true;
        while (flag) {
            try {
                ACTIVE_GAMES.forEach((s, battleshipGame) -> battleshipGame.close());
                WAITING_GAMES_CODE.forEach((s, battleshipGame) -> battleshipGame.close());
                WAITING_GAMES_RANDOM.forEach((s, game) -> game.close());
                flag = false;
            }
            catch (ConcurrentModificationException ignored) { }
        }
    }

    /**
     * Generates a 10 letter random code which is not already in use.
     */
    public static String generateCode(){
        String code;

        do {
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 10;
            Random random = new Random();

            code = random.ints(leftLimit, rightLimit + 1)
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

        } while (ACTIVE_GAMES.containsKey(code) || WAITING_GAMES_CODE.containsKey(code)
                || WAITING_GAMES_RANDOM.values().stream()
                        .map(BattleshipGame::getCode)
                        .collect(Collectors.toList())
                        .contains(code));

        return code;
    }
}
