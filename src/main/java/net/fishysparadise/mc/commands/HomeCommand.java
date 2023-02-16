package net.fishysparadise.mc.commands;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import net.fishysparadise.mc.entities.Home;
import net.fishysparadise.mc.entities.User;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class HomeCommand implements CommandExecutor {

    private final Dao<User, Integer> userDao;
    private final Dao<Home, Integer> homeDao;
    public HomeCommand(Dao<User, Integer> userDao, Dao<Home, Integer> homeDao) {
        this.userDao = userDao;
        this.homeDao = homeDao;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        QueryBuilder<User, Integer> queryBuilder = this.userDao.queryBuilder();

        try {
            queryBuilder.where().eq("uniqueId", player.getUniqueId().toString());
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            User user = this.userDao.queryForFirst(preparedQuery);

            if (user != null) {
                if (user.getHome() != null) {
                    Home home = this.homeDao.queryForId(user.getHome().getId());
                    if (home != null) {
                        List<World> worlds = player.getServer().getWorlds();
                        System.out.println(home.getWorld().toString());
                        Location location = new Location(
                                player.getServer().getWorld(home.getWorld().toString()),
                                home.getX(),
                                home.getY(),
                                home.getZ());
                        player.teleport(location);
                        String successMessage = String.format(
                                "You have teleported to your home. x: %f y: %f z: %f world: %s",
                                home.getX(),
                                home.getY(),
                                home.getZ(),
                                home.getWorld().toString());
                        player.sendMessage(successMessage);
                        return true;
                    }
                }
                player.sendMessage("You have not set your home yet, set it using /sethome");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return true;
    }
}
