package com.imjustdoom.justpermissions.listeners;

import com.imjustdoom.justpermissions.JustPermissions;
import com.imjustdoom.justpermissions.data.PlayerJP;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.permission.Permission;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginHandler {

    public LoginHandler() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        /**
         * Loads permissions
         */
        eventHandler.addListener(AsyncPlayerPreLoginEvent.class, event -> {
            Player player = event.getPlayer();

            List<String> groupPerms = new ArrayList<>();

            JustPermissions.getInstance().getPlayerHandler().addPlayer(player);
            PlayerJP user = JustPermissions.getInstance().getPlayerHandler().getData(player);

            try {
                /**
                 * Checks if the player is in the database, if not
                 * adds them to it
                 */
                if (!JustPermissions.getInstance().getDbCon().doesContain("'" + player.getUuid() + "'", "uuid", "players")) {
                    JustPermissions.getInstance().getDbCon().insertRecord("players", "'" + player.getUuid()
                            + "', '" + player.getUsername() + "', 'default'");

                    JustPermissions.getInstance().getDbCon().insertRecord("player_permissions", "0, '" + player.getUuid() + "', 'group.default'");
                }

                /**
                 * Gets all the players permissions and adds them
                 */
                ResultSet rs = JustPermissions.getInstance().getDbCon().stmt.executeQuery("SELECT * FROM player_permissions WHERE uuid = '" + player.getUuid() + "'");
                while (rs.next()) {
                    String perm = rs.getString("permission");

                    if (JustPermissions.getInstance().getGroups().contains(perm.replace("group.", ""))) {
                        JustPermissions.getInstance().getPlayers().put(player, perm.replace("group.", ""));
                        groupPerms.add(perm);
                    }

                    player.addPermission(new Permission(perm));

                    if(perm.startsWith("prefix.")) {
                        System.out.println(perm.substring("prefix.".length()));
                        user.addPrefix(perm.substring("prefix.".length()));
                    }
                }

                rs.close();

                /**
                 * Gets all permissions of each group the player inherits from
                 */
                for (String perm : groupPerms) {
                    rs = JustPermissions.getInstance().getDbCon().stmt.executeQuery("SELECT * FROM group_permissions WHERE name = '" + perm.replace("group.", "") + "'");
                    while (rs.next()) {
                        player.addPermission(new Permission(rs.getString("permission")));
                    }
                }

                rs.close();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

        /**
         * Handle prefixes/suffixes for the player
         */
        eventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();

            PlayerJP user = JustPermissions.getInstance().getPlayerHandler().getData(player);
            player.setDisplayName(Component.text(user.getPrefix() + player.getUsername()));
            player.sendMessage(player.getName());
        });

        /**
         * Removes the player from players list
         */
        eventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();

            JustPermissions.getInstance().getPlayers().remove(player);
        });
    }
}