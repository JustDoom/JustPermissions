package com.imjustdoom.justpermissions;

import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;

import java.sql.SQLException;

public class PermissionHandler {

    /**
     * Adds a permission to the player and db
     * @param player - target player
     * @param permission - permission to add
     */
    public static void addPermission(Player player, String permission){
        try {
            JustPermissions.getInstance().getSqLite().insertRecord("player_permissions", "0, '" + player.getUuid() + "', '" + permission + "'");
            player.addPermission(new Permission(permission));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Adds a permission to a group and db
     * @param group - target group
     * @param permission - permission to add
     */
    public static void addPermission(String group, String permission){
        try {
            JustPermissions.getInstance().getSqLite().insertRecord("group_permissions", "0, '" + group + "', '" + permission + "'");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Removes a permission from the player and db
     * @param player - target player
     * @param permission - permission to remove
     */
    public static void removePermission(Player player, String permission){
        try {
            JustPermissions.getInstance().getSqLite().runSql("DELETE FROM player_permissions WHERE uuid = '" + player.getUuid() + "' AND permission = '" + permission + "'");
            player.removePermission(permission);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Removes a permission from the player and db
     * @param group - target group
     * @param permission - permission to remove
     */
    public static void removePermission(String group, String permission){
        try {
            JustPermissions.getInstance().getSqLite().runSql("DELETE FROM group_permissions WHERE name = '" + group + "' AND permission = '" + permission + "'");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}