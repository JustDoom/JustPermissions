package com.imjustdoom.justpermissions.commands.subcommands;

import com.imjustdoom.justpermissions.JustPermissions;
import com.imjustdoom.justpermissions.PermissionHandler;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class PlayerSubcommand extends Command {

    public PlayerSubcommand() {
        super("player");

        ArgumentEntity players = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);
        ArgumentWord action = Word("action").from("add", "remove");
        ArgumentWord groupAction = Word("action").from("add", "remove", "set");
        ArgumentWord permission = Word("permission");

        addSyntax(this::executeInfo, players, Literal("info"));

        addSyntax(this::executePerm, players, Literal("permission"), action, permission);
        addSyntax(this::executeClear, players, Literal("permission"), Literal("clear"));

        addSyntax(this::executeGroup, players, Literal("group"), groupAction, Word("group"));
    }

    private void executeGroup(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final String action = context.get("action");
        final EntityFinder target = context.get("player");
        final String group = context.get("group");

        Player player = target.findFirstPlayer(sender);

        if (player == null) {
            sender.sendMessage("The player " + target + " was unable to be found");
            return;
        }

        /**
         * Checks if input group is valid
         */
        try {
            if(!JustPermissions.getInstance().getSqLite().doesContain("'" + group + "'", "name", "groups")){
                sender.sendMessage("The group " + group + " doesn't exist");
                return;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        switch (action) {
            case "add" -> {
                if (player.hasPermission("group." + group)) {
                    sender.sendMessage(player.getUsername() + " already is in the group " + group);
                    return;
                }

                PermissionHandler.addPermission(player, "group." + group);
                JustPermissions.getInstance().getPlayers().put(player, group);

                /**
                 * Gets all permissions from group and adds them
                 */
                try {
                    ResultSet rs = JustPermissions.getInstance().getSqLite().stmt.executeQuery("SELECT * FROM group_permissions WHERE name = '" + group + "'");
                    while (rs.next()) {
                        player.addPermission(new Permission(rs.getString("permission")));
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                sender.sendMessage(player.getUsername() + " now is in the group " + group);
            }
            case "remove" -> {
                if (!player.hasPermission("group." + group)) {
                    sender.sendMessage(player.getUsername() + " isn't in the group " + group);
                    return;
                }

                PermissionHandler.removePermission(player, "group." + group);
                JustPermissions.getInstance().getPlayers().remove(player, group);

                /**
                 * Gets all permissions from group and removes them
                 */
                try {
                    ResultSet rs = JustPermissions.getInstance().getSqLite().stmt.executeQuery("SELECT * FROM group_permissions WHERE name = '" + group + "'");
                    while (rs.next()) {
                        player.removePermission(new Permission(rs.getString("permission")));
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

                sender.sendMessage(player.getUsername() + " no longer is in the group " + group);
            }
        }
    }

    private void executePerm(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final String action = context.get("action");
        final EntityFinder target = context.get("player");
        final String permission = context.get("permission");

        Player player = target.findFirstPlayer(sender);

        if (player == null) {
            sender.sendMessage("The player " + target + " was unable to be found");
            return;
        }

        switch (action) {
            case "add" -> {
                if (player.hasPermission(permission)) {
                    sender.sendMessage(player.getUsername() + " already has the permission " + permission);
                    return;
                }

                PermissionHandler.addPermission(player, permission);
                sender.sendMessage("Added the permission " + permission + " to " + player.getUsername());
            }
            case "remove" -> {
                if (!player.hasPermission(permission)) {
                    sender.sendMessage(player.getUsername() + " doesn't have the permission " + permission);
                    return;
                }

                PermissionHandler.removePermission(player, permission);
                sender.sendMessage("Removed the permission " + permission + " from " + player.getUsername());
            }
        }
    }

    private void executeClear(@NotNull CommandSender sender, @NotNull CommandContext context){
        final EntityFinder target = context.get("player");
        Player player = target.findFirstPlayer(sender);

        /**
         * Clears all permissions
         */
        try {
            ResultSet rs = JustPermissions.getInstance().getSqLite().stmt.executeQuery("SELECT * FROM player_permissions WHERE uuid = '" + player.getUuid() + "'");
            while (rs.next()){
                System.out.println(rs.getString("permission"));
                player.removePermission(rs.getString("permission"));
            }

            rs.close();

            JustPermissions.getInstance().getSqLite().stmt.executeUpdate("DELETE FROM player_permissions WHERE uuid = '" + player.getUuid() + "'");

            sender.sendMessage("Cleared all permissions from " + player.getUsername());
        } catch (SQLException throwables) {
            sender.sendMessage("Error while trying to remove all permissions from " + player.getUsername());
            throwables.printStackTrace();
        }
    }

    private void executeInfo(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final EntityFinder target = context.get("player");
        Player player = target.findFirstPlayer(sender);

        List<String> perms = new ArrayList<>();
        for(Permission perm:player.getAllPermissions()){
            perms.add(perm.getPermissionName());
        }

        sender.sendMessage(player.getUsername() + " has the permissions " + perms);
    }

    /**
     * @param permission - The permission to be added
     * @param player     - Player to add the permission to
     * @param sender     - Command Sender
     */
    private void addPerm(String permission, Player player, CommandSender sender) {
        if (player.hasPermission(permission)) {
            sender.sendMessage(player.getUsername() + " already has the permission " + permission);
            return;
        }

        player.addPermission(new Permission(permission));
        sender.sendMessage("Added the permission " + permission + " to " + player.getUsername());
    }

    /**
     * @param permission - The permission to be removed
     * @param player     - Player to remove the permission from
     * @param sender     - Command sender
     */
    private void removePerm(String permission, Player player, CommandSender sender) {
        if (!player.hasPermission(permission)) {
            sender.sendMessage(player.getUsername() + " doesn't have the permission " + permission);
            return;
        }

        player.removePermission(permission);
        sender.sendMessage("Removed the permission " + permission + " from " + player.getUsername());
    }
}
