package de.gitlab.kaisoenmalex.server;

import com.google.gson.Gson;
import de.gitlab.kaisoenmalex.api.game.BattleshipClient;
import de.gitlab.kaisoenmalex.api.game.BattleshipGame;
import de.gitlab.kaisoenmalex.api.network.packets.ConnectPacket;
import de.gitlab.kaisoenmalex.api.util.Tuple;
import spark.Spark;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author Alexander Ley
 * @version 1.1
 *
 * This Class is the entry Point for the Server Application.
 */
public class BattleshipServer {

    public static final Gson gson = new Gson();

    private static final HashMap<String, BattleshipGame> ACTIVE_GAMES = new HashMap<>();

    private static final HashMap<String, BattleshipGame> WAITING_GAMES_CODE = new HashMap<>();
    private static final List<BattleshipGame> WAITING_GAMES_RANDOM = new ArrayList<>();

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ACTIVE_GAMES.forEach((s, battleshipGame) -> battleshipGame.close());
            WAITING_GAMES_CODE.forEach((s, battleshipGame) -> battleshipGame.close());
            WAITING_GAMES_RANDOM.forEach(BattleshipGame::close);
        }));

        Spark.post("/connect", (request, response) -> {
            /*Thread connectThread = new Thread(() -> {
                try {
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);


                    for (int i = 0; i < 20; i++){
                        writer.println(i);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            connectThread.start();*/

            final ConnectPacket connectPacket = gson.fromJson(request.body(), ConnectPacket.class);

            ServerSocket serverSocket = new ServerSocket(0);

            final BattleshipClient client = new BattleshipClient(connectPacket.getName(), serverSocket, connectPacket.getArea());

            if (connectPacket.isHost()){
                final String code = generateCode();

                if (connectPacket.isRandomEnemy()){
                    if (WAITING_GAMES_RANDOM.size() >= 1){

                        final BattleshipGame host = WAITING_GAMES_RANDOM.get(0);
                        host.guestClientJoin(client);
                        ACTIVE_GAMES.put(host.getCode(), WAITING_GAMES_RANDOM.remove(0));

                    }
                    else {
                        final BattleshipGame game = new BattleshipGame(code, connectPacket.isNoobVariant(), client);
                        WAITING_GAMES_RANDOM.add(game);
                    }
                }
                else {
                    final BattleshipGame game = new BattleshipGame(code, connectPacket.isNoobVariant(), client);
                    WAITING_GAMES_CODE.put(code, game);
                }

                return gson.toJson(new Tuple<>(code, serverSocket.getLocalPort()));
            }
            else{
                final String code = connectPacket.getCode();
                final BattleshipGame host = ACTIVE_GAMES.get(code);

                if (host == null) return null;

                host.guestClientJoin(client);

                return gson.toJson(new Tuple<>("", serverSocket.getLocalPort()));
            }
        });
    }

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
                || WAITING_GAMES_RANDOM.stream()
                        .map(BattleshipGame::getCode)
                        .collect(Collectors.toList())
                        .contains(code));

        return code;
    }
}
