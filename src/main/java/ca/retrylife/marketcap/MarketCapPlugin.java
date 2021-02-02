package ca.retrylife.marketcap;

import kr.entree.spigradle.annotations.PluginMain;

import org.bukkit.plugin.java.JavaPlugin;

import ca.retrylife.marketcap.commands.MarketCapCommand;
import ca.retrylife.marketcap.database.DatabaseAPI;
import ca.retrylife.marketcap.events.InventoryInteraction;
import ca.retrylife.marketcap.events.PlayerConnection;
import ca.retrylife.marketcap.util.SentryUtil;
import co.aikar.commands.PaperCommandManager;


// import dev.jorel.commandapi.CommandAPICommand;
// import dev.jorel.commandapi.CommandPermission;
// import dev.jorel.commandapi.arguments.ItemStackArgument;

@PluginMain
public class MarketCapPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        SentryUtil.configureSentry("https://326a28c0d4544344b05469612ee9a29a@o398481.ingest.sentry.io/5619922", getServer()); 

        // Start the database server
        DatabaseAPI.getInstance().startServer();

        // Add event listeners
        getServer().getPluginManager().registerEvents(new PlayerConnection(), this);
        getServer().getPluginManager().registerEvents(new InventoryInteraction(), this);

        // Plugin management
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new MarketCapCommand());

        // Set up item add and remove commands
        // CommandAPICommand itemAddCommand = new CommandAPICommand("add").withPermission(CommandPermission.OP)
        //         .withArguments(new ItemStackArgument("type"));
        // CommandAPICommand itemRemoveCommand = new CommandAPICommand("remove").withPermission(CommandPermission.OP)
        //         .withArguments(new ItemStackArgument("type"));
        // CommandAPICommand dbStatCommand = new CommandAPICommand("dbstat").withPermission(CommandPermission.NONE);

        // // Add command listeners
        // new CommandAPICommand("mcap").withPermission(CommandPermission.NONE).executes(new MCapCommand())
        //         .withSubcommand(itemAddCommand).withSubcommand(itemRemoveCommand).withSubcommand(dbStatCommand).register();

    }

}