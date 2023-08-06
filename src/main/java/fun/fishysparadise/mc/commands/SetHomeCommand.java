package fun.fishysparadise.mc.commands;

import fun.fishysparadise.mc.FishysParadisePlugin;
import fun.fishysparadise.mc.tables.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class SetHomeCommand implements CommandExecutor {

    private FishysParadisePlugin main;

    public SetHomeCommand(FishysParadisePlugin plugin) {
        setMain(plugin);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player player) {
            User user = getMain().getUser(player);
            if (user != null) {
                user.setHome_x(player.getLocation().getX());
                user.setHome_y(player.getLocation().getY());
                user.setHome_z(player.getLocation().getZ());
                user.setHome_world(Objects.requireNonNull(player.getLocation().getWorld()).getName());
                getMain().safeUpdateUser(user);
                player.sendMessage("You have set your new home coordinates!");
            } else {
                getMain().getLogger().warning("Set Home Command - User not found!");
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
