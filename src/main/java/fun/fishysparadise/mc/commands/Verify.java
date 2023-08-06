package fun.fishysparadise.mc.commands;

import fun.fishysparadise.mc.FishysParadisePlugin;
import fun.fishysparadise.mc.tables.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Verify implements CommandExecutor {

    private FishysParadisePlugin main;

    public Verify(FishysParadisePlugin plugin) {
        setMain(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("Only players can execute this command.");
            return true;
        }
        // Piecing together the code from the strings
        String code = String.join(" ", strings);
        // Ensuring code is within expected length
        if (!(code.length() == 12)) {
            return false;
        }
        // Fetching user
        User user = getMain().getUser(code);
        if (user != null) {
            user.setUsername(player.getDisplayName());
            user.setUniqueId(player.getUniqueId().toString());
            getMain().safeUpdateUser(user);
            player.sendMessage("Account verified successfully!");
        } else {
            getMain().getLogger().warning("Verify Command - User not found!");
            player.sendMessage("Please signup on the website to enable account linking and use the access token provided there to verify again.");
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
