package com.imjustdoom.justpermissions.listeners;

import com.imjustdoom.justpermissions.JustPermissions;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.permission.Permission;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginHandler {

    public LoginHandler(){
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(AsyncPlayerPreLoginEvent.class, event -> {
            Player player = event.getPlayer();

            List<String> groupPerms = new ArrayList<>();

            try {
                if(!JustPermissions.getInstance().getSqLite().doesContain("'" + player.getUuid() + "'", "uuid", "players")) {
                    JustPermissions.getInstance().getSqLite().insertRecord("players", "'" + player.getUuid()
                            + "', '" + player.getUsername() + "', 'default'");

                    JustPermissions.getInstance().getSqLite().insertRecord("player_permissions", "0, '" + player.getUuid() + "', 'group.default'");
                }

                if(JustPermissions.getInstance().getSqLite().doesContain("'" + player.getUuid() + "'", "uuid", "players")){
                    ResultSet rs = JustPermissions.getInstance().getSqLite().stmt.executeQuery("SELECT * FROM player_permissions WHERE uuid = '" + player.getUuid() + "'");
                    while (rs.next()){
                        String perm = rs.getString("permission");

                        System.out.println(perm.replace("group.", ""));
                        if(JustPermissions.getInstance().getGroups().contains(perm.replace("group.", ""))){
                            JustPermissions.getInstance().getPlayers().put(player, perm.replace("group.", ""));
                            groupPerms.add(perm);
                        }

                        player.addPermission(new Permission(perm));
                    }

                    rs.close();

                    for(String perm:groupPerms) {
                        rs = JustPermissions.getInstance().getSqLite().stmt.executeQuery("SELECT * FROM group_permissions WHERE name = '" + perm.replace("group.", "") + "'");
                        while (rs.next()) {
                            System.out.println(rs.getString("permission"));
                            player.addPermission(new Permission(rs.getString("permission")));
                        }
                    }

                    rs.close();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

        eventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();

            JustPermissions.getInstance().getPlayers().remove(player);
        });
    }
}