package net.fishysparadise.mc.commands;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import net.fishysparadise.mc.entities.Home;
import net.fishysparadise.mc.entities.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class SetHomeCommand implements CommandExecutor {

    private final Dao<User, Integer> userDao;
    private final Dao<Home, Integer> homeDao;
    public SetHomeCommand(Dao<User, Integer> userDao, Dao<Home, Integer> userHomeDao) {
        this.userDao = userDao;
        this.homeDao = userHomeDao;
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
                Home home = user.getHome();
                if (home != null) {
                    Home homeRecord = this.homeDao.queryForId(home.getId());
                    if (homeRecord != null) {
                        // If home has been set before
                        home.setCoordinates(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
                        home.setWorld(player.getWorld().getName().toString());
                        home.setUser(user);
                        this.homeDao.update(home);
                        player.sendMessage("Your home coords have been updated.");
                        return true;
                    }
                }
                // If home is being set first time
                Home newHome = new Home(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getWorld().getName().toString());
                newHome.setUser(user);
                this.homeDao.create(newHome);
                user.setHome(newHome);
                this.userDao.update(user);
                player.sendMessage("Your home coords have been set.");
                return true;

            } else {
                player.getServer().getLogger().warning("User not found!");
                player.sendMessage("Rejoin the server and try again!");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return true;
    }
}
