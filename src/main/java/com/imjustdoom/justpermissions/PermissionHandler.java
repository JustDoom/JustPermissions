package com.imjustdoom.justpermissions;

import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PermissionHandler {

    public static void addPermission(Player player, String permission){
        try {
            JustPermissions.getInstance().getSqLite().insertRecord("player_permissions", "0, '" + player.getUuid() + "', '" + permission + "'");
            player.addPermission(new Permission(permission));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void addPermission(String group, String permission){
        try {
            JustPermissions.getInstance().getSqLite().insertRecord("group_permissions", "0, '" + group + "', '" + permission + "'");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void removePermission(Player player, String permission){
        try {
            JustPermissions.getInstance().getSqLite().runSql("DELETE FROM player_permissions WHERE uuid = '" + player.getUuid() + "' AND permission = '" + permission + "'");
            player.removePermission(permission);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void removePermission(String group, String permission){
        try {
            JustPermissions.getInstance().getSqLite().runSql("DELETE FROM group_permissions WHERE name = '" + group + "' AND permission = '" + permission + "'");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static boolean hasPermission(String group, String permission){

        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}