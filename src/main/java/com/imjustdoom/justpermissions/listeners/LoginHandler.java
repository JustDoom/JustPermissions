package com.imjustdoom.justpermissions.listeners;

import com.imjustdoom.justpermissions.JustPermissions;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.permission.Permission;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginHandler {

    public LoginHandler(){
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(AsyncPlayerPreLoginEvent.class, event -> {
            Player player = event.getPlayer();

            try {
                if(JustPermissions.getInstance().getSqLite().doesContain("'" + player.getUuid() + "'", "uuid", "players")){
                    ResultSet rs = JustPermissions.getInstance().getSqLite().stmt.executeQuery("SELECT * FROM player_permissions WHERE uuid = '" + player.getUuid() + "'");
                    while (rs.next()){
                        System.out.println(rs.getString("permission"));
                        player.addPermission(new Permission(rs.getString("permission")));
                    }
                    return;
                }

                JustPermissions.getInstance().getSqLite().insertRecord("players", "'" + player.getUuid()
                        + "', '" + player.getUsername() + "', 'default'");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }
}