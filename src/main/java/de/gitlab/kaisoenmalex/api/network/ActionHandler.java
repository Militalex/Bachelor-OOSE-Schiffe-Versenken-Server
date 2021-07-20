package de.gitlab.kaisoenmalex.api.network;

import de.gitlab.kaisoenmalex.api.game.BattleshipClient;
import de.gitlab.kaisoenmalex.api.game.BattleshipGame;
import de.gitlab.kaisoenmalex.api.network.packets.ActionPacket;
import de.gitlab.kaisoenmalex.api.network.packets.GameStatusPacket;

import java.io.ObjectOutputStream;

/**
 * @author Alexander Ley
 * @version 1.1.1
 *
 * This class defines all server reactions.
 * Note every Method have (PrintWriter writer, BattleshipGame game, BattleshipClient client, Object... parameters) Parameters.
 */
public class ActionHandler {

    /**
     * Set the actual client ready and try starting the game.
     */
    private static void setReady(ObjectOutputStream writer, BattleshipGame game, BattleshipClient client){
        client.setReady();
        game.startGame();
    }

    /**
     * Refreshes client timeout when client say he is alive.
     */
    private static void refreshClient(ObjectOutputStream writer, BattleshipGame game, BattleshipClient client){
        if (client.isHost()) game.getNetwork().getHostListener().refresh();
        else game.getNetwork().getGuestListener().refresh();
    }

    /**
     * [Without function] Sends GameStatus via tcp to client.
     */
    private static void sendStatus(ObjectOutputStream writer, BattleshipGame game, BattleshipClient client){
        final ActionPacket statusPacket = new ActionPacket("receiveStatus",
                new GameStatusPacket(client.getName(), ((client.isHost()) ? game.getHostClient() : game.getGuestClient()).getName(), game.isNoob(),
                        (client.isHost()) ? game.canHostAttack() : game.canGuestAttack(), "empty",
                                client.getArea(), ((client.isHost() ? game.getGuestClient() : game.getHostClient())).getArea()));

        game.getNetwork().sendPacket(statusPacket, client.isHost() ? 0 : 1);
    }
}
