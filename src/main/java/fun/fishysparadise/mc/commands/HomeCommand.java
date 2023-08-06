package fun.fishysparadise.mc.commands;

import fun.fishysparadise.mc.FishysParadisePlugin;
import fun.fishysparadise.mc.tables.User;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HomeCommand implements CommandExecutor {

    private FishysParadisePlugin main;

    public HomeCommand(FishysParadisePlugin plugin) {
        setMain(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player player) {
            User user = getMain().getUser(player);
            if (user != null) {
                if (user.getHome_world() != null) {
                    Location location = new Location(
                            getMain().getServer().getWorld(user.getHome_world()),
                            user.getHome_x(),
                            user.getHome_y(),
                            user.getHome_z());
                    player.teleport(location);
                    String teleportSuccessMessage = String.format(
                            "You have teleported to your home. x: %f y: %f z: %f",
                            user.getHome_x(),
                            user.getHome_y(),
                            user.getHome_z());
                    player.sendMessage(teleportSuccessMessage);
                } else {
                    player.sendMessage("You have not set your home yet. Use /sethome to set your home!");
                }
            } else {
                getMain().getLogger().warning("Home Command - User not found!");
                player.sendMessage("Please verify your account to access this command.");
            }
        }
        return true;
    }

    // Getters and setters
    public FishysParadisePlugin getMain() {
        return main;
    }

    public void setMain(FishysParadisePlugin main) {
        this.main = main;
    }
}