package fun.fishysparadise.mc.commands;

import fun.fishysparadise.mc.FishysParadisePlugin;
import fun.fishysparadise.mc.tables.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClearWeatherCommand implements CommandExecutor {

    private FishysParadisePlugin main;

    public ClearWeatherCommand(FishysParadisePlugin plugin) {
        setMain(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player player) {
            User user = getMain().getUser(player);
            if (user != null) {
                if (player.getWorld().hasStorm()) {
                    player.getWorld().setStorm(false);
                    getMain().broadcastFormattedMessage("Cleared the weather, %s", player.getDisplayName());
                } else {
                    getMain().broadcastFormattedMessage("You are not verified, %s and hence cannot use this command. Please use /verify to verify yourself.", player.getDisplayName());
                }
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
