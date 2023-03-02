package net.fishysparadise.mc;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import net.fishysparadise.mc.commands.HomeCommand;
import net.fishysparadise.mc.commands.SetHomeCommand;
import net.fishysparadise.mc.entities.Home;
import net.fishysparadise.mc.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class Core extends JavaPlugin implements Listener {

    private Bot discordBot;
    private String databaseUrl;
    private ConnectionSource connectionSource;
    private Dao<User, Integer> userDao;
    private Dao<Home, Integer> homeDao;

    public Core() throws SQLException {
        // Saving config / updating config
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        // Setting up database connection
        FileConfiguration config = getConfig();
        databaseUrl = String.format(
                "jdbc:mysql://%s:%s@%s/%s",
                config.getString("DATABASE.USERNAME"),
                config.getString("DATABASE.PASSWORD"),
                config.getString("DATABASE.ADDRESS"),
                config.getString("DATABASE.DATABASE")
        );
        this.connectionSource = new JdbcConnectionSource(databaseUrl);
        // Setting up dao's
        this.userDao = DaoManager.createDao(connectionSource, User.class);
        this.homeDao = DaoManager.createDao(connectionSource, Home.class);
        // Deleting old tables if needed
        if (config.getBoolean("DATABASE.RECREATE")) {
            TableUtils.dropTable(connectionSource, User.class, true);
            TableUtils.dropTable(connectionSource, Home.class, true);
            config.set("DATABASE.RECREATE", false);
            saveConfig();
        }
        // Creating tables
        TableUtils.createTableIfNotExists(connectionSource, User.class);
        TableUtils.createTableIfNotExists(connectionSource, Home.class);

    }

    @Override
    public void onEnable() {
        // Saving config / updating config
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        // Setting up bot
        if (discordBot != null) {
            discordBot.shutdown();
        }
        try {
            discordBot = new Bot(this);
        } catch (Exception e) {
            return;
        }
        // Registering listener and commands
        this.getServer().getPluginManager().registerEvents(this, this);
        getCommand("home").setExecutor(new HomeCommand(this.userDao, this.homeDao));
        getCommand("sethome").setExecutor(new SetHomeCommand(this.userDao, this.homeDao));
        this.getLogger().info("Fishy's Paradise Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        this.getLogger().warning("Fishy's Paradise Plugin has been disabled!");
        // Shutting down bot
        try {
            discordBot.shutdown();
            discordBot = null;
        } catch (Exception ignored) {
        }
    }


    public String getOnlinePlayersMessage(Server server, boolean leaving) {
        int count = server.getOnlinePlayers().size();
        if (leaving) {
            count--;
        }
        if (count < 0) {
            count = 0;
        }
        switch (count) {
            case 0: {
                return "There are no players online.";
            }
            case 1: {
                return "There is 1 player online.";
            }
            default: {
                return "There are " + String.valueOf(count) + " players online.";
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        discordBot.sendMessageToDiscord(event.getPlayer(),
                "*has joined the server! "
                        + getOnlinePlayersMessage(event.getPlayer().getServer(), false)
                        + "*"
        );
        // Custom message and registration
        QueryBuilder<User, Integer> queryBuilder = this.userDao.queryBuilder();
        String uniqueId = event.getPlayer().getUniqueId().toString();
        String username = event.getPlayer().getDisplayName();
        try {
            queryBuilder.where().eq("uniqueId", uniqueId);
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            User user = this.userDao.queryForFirst(preparedQuery);
            String formattedMessage;
            if (user != null) {
                formattedMessage = String.format("Welcome back to Fishy's Paradise, %s!", username);
            } else {
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setUniqueId(uniqueId);
                this.userDao.create(newUser);
                formattedMessage = String.format("Welcome to Fishy's Paradise, this is your first time, take a look at the rules, %s!", username);
            }
            Bukkit.broadcastMessage(formattedMessage);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        discordBot.sendMessageToDiscord(event.getPlayer(),
                "*has left the server! "
                        + getOnlinePlayersMessage(event.getPlayer().getServer(), true)
                        + "*"
        );
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        discordBot.sendSanitisedMessageToDiscord(event.getPlayer(), event.getMessage());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        discordBot.sendSanitisedMessageToDiscord(event.getEntity().getPlayer(), "*" + event.getDeathMessage().toString() + "*");

    }





}
