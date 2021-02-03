package ca.retrylife.marketcap.events;

import java.util.Collection;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import ca.retrylife.marketcap.events.utils.ChunkySearcher;

public class TimerSearch implements Runnable {

    private final Server server;

    public TimerSearch(Server server) {
        this.server = server;
    }

    @Override
    public void run() {

        // Get all players
        Collection<? extends Player> players = this.server.getOnlinePlayers();

        // Run a chunkysearcher at each player
        players.forEach((player) -> {

            // Build the player's searcher
            ChunkySearcher searcher = new ChunkySearcher(player.getLocation());

            // Search
            searcher.search(null, 2);

        });

    }

}