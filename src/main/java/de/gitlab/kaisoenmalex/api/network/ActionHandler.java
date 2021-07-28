package de.gitlab.kaisoenmalex.api.network;

import de.gitlab.kaisoenmalex.api.game.BattleshipClient;
import de.gitlab.kaisoenmalex.api.game.BattleshipGame;
import de.gitlab.kaisoenmalex.api.game.GameArea;
import de.gitlab.kaisoenmalex.api.network.packets.ActionPacket;
import de.gitlab.kaisoenmalex.api.network.packets.GameStatusPacket;
import de.gitlab.kaisoenmalex.api.util.RunLater;

import java.io.ObjectOutputStream;

/**
 * @author Alexander Ley
 * @version 1.3
 * latest author: Kai Sturm
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
                        (client.isHost()) ? game.canHostAttack() : game.canGuestAttack(),
                                client.getArea(), ((client.isHost() ? game.getGuestClient() : game.getHostClient())).getArea()));

        game.getNetwork().sendPacket(statusPacket, client.isHost() ? 0 : 1);
    }

    /**
     * Server Response if client is shooting a field.
     */
    private static void shoot(ObjectOutputStream writer, BattleshipGame game, BattleshipClient client, Double x, Double y){
        BattleshipClient enemyClient = (client.isHost()) ? game.getGuestClient() : game.getHostClient();
        Boolean result = enemyClient.getArea().shootField(x.intValue(), y.intValue());
        assert result != null;

        if (!result) {
            game.setCanHostAttack(!client.isHost());
        }

        if (enemyClient.getArea().allShipsShot()){
            final ActionPacket gameWonPacket = new ActionPacket("stopGame", true, false);
            final ActionPacket gameLoosePacket = new ActionPacket("stopGame", false, false);

            game.getNetwork().sendPacket(gameWonPacket, (client.isHost()) ? 0 : 1);
            game.getNetwork().sendPacket(gameLoosePacket, (enemyClient.isHost()) ? 0 : 1);
            game.setRunning(false);
            game.close();
            return;
        }

        final ActionPacket packet = new ActionPacket("refreshStatus");
        game.getNetwork().sendPacket(packet, 0);
        game.getNetwork().sendPacket(packet, 1);
    }

    /**
     * Server Response if one client is suddenly aborting game.
     */
    private static void abortGame(ObjectOutputStream writer, BattleshipGame game, BattleshipClient client){
        (client.isHost() ? game.getGuestClient() : game.getHostClient()).abort("Der Gegner hat das Spiel verlassen!");

        new RunLater(10, unused -> game.close());
    }

    /**
     * Server Response if one client surrenders.
     */
    private static void giveUp(ObjectOutputStream writer, BattleshipGame game, BattleshipClient client) {
        final ActionPacket gameWonPacket = new ActionPacket("stopGame", true, true);
        final ActionPacket gameLoosePacket = new ActionPacket("stopGame", false, false);

        game.getNetwork().sendPacket(gameWonPacket, (client.isHost()) ? 1 : 0);
        game.getNetwork().sendPacket(gameLoosePacket, (client.isHost()) ? 0 : 1);
        game.setRunning(false);
        game.close();
    }
}
