package de.gitlab.kaisoenmalex.api.network;

import de.gitlab.kaisoenmalex.api.game.BattleshipClient;
import de.gitlab.kaisoenmalex.api.game.BattleshipGame;

import java.io.PrintWriter;

/**
 * @author Alexander Ley
 * @version 1.0
 *
 * This class defines all server reactions.
 * Note every Method have (PrintWriter writer, BattleshipGame game, BattleshipClient client, Object... parameters) Parameters.
 */
public class ActionHandler {

    /**
     * Set the actual client ready and try starting the game.
     */
    private static void setReady(PrintWriter writer, BattleshipGame game, BattleshipClient client){
        client.setReady();
        game.startGame();
    }
}
