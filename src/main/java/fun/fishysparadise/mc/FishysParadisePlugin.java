package fun.fishysparadise.mc;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import fun.fishysparadise.mc.commands.ClearWeatherCommand;
import fun.fishysparadise.mc.commands.HomeCommand;
import fun.fishysparadise.mc.commands.SetHomeCommand;
import fun.fishysparadise.mc.commands.Verify;
import fun.fishysparadise.mc.discord.Bot;
import fun.fishysparadise.mc.tables.User;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FishysParadisePlugin extends JavaPlugin implements Listener {

    // Database variables
    private FileConfiguration config = null;
    private ConnectionSource connectionSource;
    private String databaseUrl = null;
    private Dao<User, Integer> userDao;

    // Database boolean variables
    private boolean RECREATE = false;
    private boolean CHECK = false;

    // Discord variables
    private Long TEXT_CHANNEL_ID = null;
    private String AVATAR_URL = null;
    private String BOT_TOKEN = null;
    private String WEBHOOK_URL = null;
    private Bot discordBot = null;

    // Minecraft settings
    private double DEATHPORTION = 1;

    public FishysParadisePlugin() throws SQLException {
        // Generating configuration files and checking for values
        if (!regenConfig()) {
            return;
        };
        // Setting up connection
        setConnectionSource(new JdbcConnectionSource(getDatabaseUrl()));
        // Data access object for the User table
        setUserDao(DaoManager.createDao(getConnectionSource(), User.class));
        // Delete existing tables if needed
        if (getRECREATE()) {
            TableUtils.dropTable(getConnectionSource(), User.class, true);
            config.set("STORAGE.RECREATE", false);
            saveConfig();
        }
        // Creating tables if they do not exist if needed
        if (getCHECK()) {
            TableUtils.createTableIfNotExists(getConnectionSource(), User.class);
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        // Attempting to generate and check configuration variables
        if (!regenConfig()) {
            return;
        };
        // Checking for shutdown in case of errors during reload
        if (getDiscordBot() != null) {
            getDiscordBot().shutdown();
        }
        // Creating a new instance of the adapter
        try {
           setDiscordBot(new Bot(this));
        } catch (Exception e) {
            this.getLogger().warning("Error occurred while attempting to set up Discord Adapter: " + e.getMessage());
            return;
        }
        // Adding the events and commands to the plugin
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("verify").setExecutor(new Verify(this));
        this.getCommand("sethome").setExecutor(new SetHomeCommand(this));
        this.getCommand("home").setExecutor(new HomeCommand(this));
        this.getCommand("clearweather").setExecutor(new ClearWeatherCommand(this));
        this.getLogger().info("Fishy's Paradise Plugin has been enabled");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        // Attempting to stop the Discord integration
        try {
            getDiscordBot().shutdown();
            setDiscordBot(null);
        } catch (Exception ignored) {
            if (getDiscordBot() != null) {
                this.getLogger().warning("Discord Integration failed to shutdown properly!");
            }
        }
        this.getLogger().info("Fishy's Paradise Plugin has been disabled!");
    }

    public boolean regenConfig() {
        // Reloading and generating config file if not present
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        config = getConfig();
        // Fetching config data
        setAVATAR_URL(config.getString("MINECRAFT.AVATAR"));
        setTEXT_CHANNEL_ID(config.getLong("DISCORD.CHANNEL") != 0 ? config.getLong("DISCORD.CHANNEL") : null);
        setBOT_TOKEN(config.getString("DISCORD.TOKEN"));
        setWEBHOOK_URL(config.getString("DISCORD.WEBHOOK"));
        setRECREATE(config.getBoolean("STORAGE.RECREATE", false));
        setCHECK(config.getBoolean("STORAGE.CHECK", true));
        setDEATHPORTION(config.getDouble("MINECRAFT.DEATH-PORTION", 1));
        // Constructing database url
        setDatabaseUrl(String.format(
                "jdbc:mysql://%s:%s@%s/%s",
                config.getString("STORAGE.USERNAME"),
                config.getString("STORAGE.PASSWORD"),
                config.getString("STORAGE.ADDRESS"),
                config.getString("STORAGE.DATABASE")
        ));
        // Value checks
        boolean no_issues = true;
        // String variables
        List<String> configCheckVars = Arrays.asList("STORAGE.USERNAME", "STORAGE.PASSWORD", "STORAGE.ADDRESS", "STORAGE.DATABASE", "MINECRAFT.AVATAR", "DISCORD.TOKEN", "DISCORD.WEBHOOK");
        for (String Var: configCheckVars) {
            if (config.getString(Var) == null) {
                // Report every missing configuration variable
                this.getLogger().warning(String.format("Configuration variable %s is not set.", Var));
                config.set(Var, null);
                no_issues = false;
            }
        }
        // Long variable
        if (getTEXT_CHANNEL_ID() == null || getTEXT_CHANNEL_ID() == 0) {
            this.getLogger().warning("Configuration variable DISCORD.CHANNEL is not set.");
            config.set("DISCORD.CHANNEL", null);
            no_issues = false;
        }
        // Cancel plugin load if at least one configuration is missing
        if (!no_issues) {
            saveDefaultConfig();
            getConfig().options().copyDefaults(true);
            saveConfig();
            disablePlugin();
        }
        return no_issues;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Passing on the message to the adapter
        getDiscordBot().sendMessageToDiscord(event.getPlayer(),
                "__*has joined the server, "  + getOnlineMessage(event.getPlayer().getServer(), false) + "*__");
        // Fetching player data for custom join message
        Player player = event.getPlayer();
        if (getUser(player) != null) {
            broadcastFormattedMessage("Welcome back to Fishy's Paradise, %s!", player.getDisplayName());
        } else {
            broadcastFormattedMessage("Welcome to Fishy's Paradise, this is your first time, take a look at the rules, %s! Please verify yourself to use the commands, using /verify", player.getDisplayName());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Adding in verified prefix for verified users
        event.setFormat(getUser(event.getPlayer()) != null ? (ChatColor.of("#aaff00") + "[Verified]" + ChatColor.RESET + " %s : %s") : (ChatColor.of("#808080") + "[Unverified]" + ChatColor.RESET + " %s : %s"));
        // Sending player message via Discord webhook
        getDiscordBot().sendSanitisedMessageToDiscord(event.getPlayer(), event.getMessage());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Passing on the message to the adapter
        getDiscordBot().sendMessageToDiscord(event.getPlayer(),
                "__*has left the server, " + getOnlineMessage(event.getPlayer().getServer(), true) + "*__");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Passing on the message to the adapter
        getDiscordBot().sendMessageToDiscord(event.getEntity().getPlayer(),
                "__*" + event.getDeathMessage() + "*__");
        // Retaining a percentage of the exp
        int value = (int) (Objects.requireNonNull(event.getEntity().getPlayer()).getLevel() * getDEATHPORTION());
        int remaining = Objects.requireNonNull(event.getEntity().getPlayer()).getLevel() - value;
        event.setNewLevel(value);
        event.setDroppedExp(levelsToXp(remaining));
    }

    public static int levelsToXp(int levels) {
        // Convertor
        if (levels <= 16) {
            return (int) (Math.pow(levels, 2) + (6 * levels));
        } else if (levels >= 17 && levels <= 31) {
            return (int) ((2.5 * Math.pow(levels, 2)) + (-40.5 * levels) + 360);
        } else if (levels >= 32) {
            return (int) ((4.5 * Math.pow(levels, 2)) + (-162.5 * levels) + 2220);
        }
        return -1;
    }

    public User getUser(Player player) {
        // Fetching player data using minecraft signature
        try {
            QueryBuilder<User, Integer> queryBuilder = getUserDao().queryBuilder();
            queryBuilder.where().eq("uniqueId", player.getUniqueId().toString());
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            User user = getUserDao().queryForFirst(preparedQuery);
            return user;
        } catch (SQLException e) {
            getLogger().warning(e.getMessage());
        }
        return null;
    }

    public User getUser(String code) {
        // Fetching player data using access code
        try {
            QueryBuilder<User, Integer> queryBuilder = getUserDao().queryBuilder();
            queryBuilder.where().eq("access_code", code);
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            User user = getUserDao().queryForFirst(preparedQuery);
            return user;
        } catch (SQLException e) {
            getLogger().warning(e.getMessage());
        }
        return null;
    }

    public User getMember(Member member) {
        // Fetching player data using discord signature - must be verified
        try {
            QueryBuilder<User, Integer> queryBuilder = getUserDao().queryBuilder();
            queryBuilder.where().eq("dc_id", member.getId());
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            User user = getUserDao().queryForFirst(preparedQuery);
            return user.getUniqueId() != null ? user: null;
        } catch (SQLException e) {
            getLogger().warning(e.getMessage());
        }
        return null;
    }

    public void safeUpdateUser(User user) {
        // Ensuring that errors don't prevent the program from halting
        try {
            getUserDao().update(user);
        } catch (SQLException e) {
            this.getLogger().warning("Unable to update user to database! ");
        }
    }

    public void broadcastFormattedMessage(String toBeFormatted, String... strings) {
        // Auto formatting messages to be sent
        String formattedMessage = String.format(toBeFormatted, (Object[]) strings);
        Bukkit.broadcastMessage(formattedMessage);
    }

    private String getOnlineMessage(Server server, boolean leaving) {
        // Getting player count dynamically
        int amount = server.getOnlinePlayers().size();
        // Discounting the player that is about to leave/ has left
        if (leaving) {
            amount--;
        }
        // Count cannot be negative
        if (amount < 0) {
            amount = 0;
        }
        switch (amount) {
            case 0: {
                return "there are now no players online.";
            }
            case 1: {
                return "there is now 1 player online.";
            }
            default: {
                return "there are now " + amount + " players online.";
            }
        }
    }

    public void disablePlugin() {
        // Disable plugin via manager
        this.getServer().getPluginManager().disablePlugin(this);
    }

    // Getters and setters (noob code)
    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    public void setConnectionSource(ConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public Dao<User, Integer> getUserDao() {
        return userDao;
    }

    public void setUserDao(Dao<User, Integer> userDao) {
        this.userDao = userDao;
    }

    public boolean getRECREATE() {
        return RECREATE;
    }

    public void setRECREATE(boolean RECREATE) {
        this.RECREATE = RECREATE;
    }

    public boolean getCHECK() {
        return CHECK;
    }

    public void setCHECK(boolean CHECK) {
        this.CHECK = CHECK;
    }

    public Long getTEXT_CHANNEL_ID() {
        return TEXT_CHANNEL_ID;
    }

    public void setTEXT_CHANNEL_ID(Long id) {
        TEXT_CHANNEL_ID = id;
    }

    public String getAVATAR_URL() {
        return AVATAR_URL;
    }

    public void setAVATAR_URL(String avatarUrl) {
        AVATAR_URL = avatarUrl;
    }

    public String getBOT_TOKEN() {
        return BOT_TOKEN;
    }

    public void setBOT_TOKEN(String botToken) {
        BOT_TOKEN = botToken;
    }

    public String getWEBHOOK_URL() {
        return WEBHOOK_URL;
    }

    public void setWEBHOOK_URL(String WEBHOOK_URL) {
        this.WEBHOOK_URL = WEBHOOK_URL;
    }

    public Bot getDiscordBot() {
        return discordBot;
    }

    public void setDiscordBot(Bot discordBot) {
        this.discordBot = discordBot;
    }

    public double getDEATHPORTION() {
        return DEATHPORTION;
    }

    public void setDEATHPORTION(double DEATHPORTION) {
        this.DEATHPORTION = DEATHPORTION;
    }
}
