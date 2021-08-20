package com.imjustdoom.justpermissions.commands.subcommands;

import com.imjustdoom.justpermissions.JustPermissions;
import com.imjustdoom.justpermissions.PermissionHandler;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.extensions.DiscoveredExtension;
import net.minestom.server.extensions.Extension;
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

        List<String> permissions = new ArrayList<>();

        /**for (Extension extension : MinecraftServer.getExtensionManager().getExtensions()){
            if(extension.getOrigin().getMeta()("permissions") == null) continue;
            for (int i = 0; i < extension.getOrigin().getMeta().get("permissions").getAsJsonArray().size(); i++) {
                if(extension.getOrigin().getMeta().get("permissions").getAsJsonArray().get(i) == null) continue;
                permissions.add(extension.getOrigin().getMeta().get("permissions").getAsJsonArray().get(i).getAsString());
            }
        }**/

        String[] permissionArray = permissions.toArray(new String[permissions.size()]);
        ArgumentWord permission = Word("permission").from(permissionArray);

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

        if (sender.isPlayer() && !sender.hasPermission("justpermissions.perms")) {
            sender.sendMessage("You need the permission \"justpermissions.perms\" to use this command");
            return;
        }

        if (player == null) {
            sender.sendMessage("The player " + target + " was unable to be found");
            return;
        }

        /**
         * Checks if input group is valid
         */
        try {
            if(!JustPermissions.getInstance().getDbCon().doesContain("'" + group + "'", "name", "groups")){
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
                    ResultSet rs = JustPermissions.getInstance().getDbCon().stmt.executeQuery("SELECT * FROM group_permissions WHERE name = '" + group + "'");
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
                    ResultSet rs = JustPermissions.getInstance().getDbCon().stmt.executeQuery("SELECT * FROM group_permissions WHERE name = '" + group + "'");
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

        if (sender.isPlayer() && !sender.hasPermission("justpermissions.perms")) {
            sender.sendMessage("You need the permission \"justpermissions.perms\" to use this command");
            return;
        }

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

        if (sender.isPlayer() && !sender.hasPermission("justpermissions.perms")) {
            sender.sendMessage("You need the permission \"justpermissions.perms\" to use this command");
            return;
        }

        /**
         * Clears all permissions
         */
        try {
            ResultSet rs = JustPermissions.getInstance().getDbCon().stmt.executeQuery("SELECT * FROM player_permissions WHERE uuid = '" + player.getUuid() + "'");
            while (rs.next()){
                System.out.println(rs.getString("permission"));
                player.removePermission(rs.getString("permission"));
            }

            rs.close();

            JustPermissions.getInstance().getDbCon().stmt.executeUpdate("DELETE FROM player_permissions WHERE uuid = '" + player.getUuid() + "'");

            sender.sendMessage("Cleared all permissions from " + player.getUsername());
        } catch (SQLException throwables) {
            sender.sendMessage("Error while trying to remove all permissions from " + player.getUsername());
            throwables.printStackTrace();
        }
    }

    private void executeInfo(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final EntityFinder target = context.get("player");
        Player player = target.findFirstPlayer(sender);

        if (sender.isPlayer() && !sender.hasPermission("justpermissions.perms")) {
            sender.sendMessage("You need the permission \"justpermissions.perms\" to use this command");
            return;
        }

        List<String> perms = new ArrayList<>();
        for(Permission perm:player.getAllPermissions()){
            perms.add(perm.getPermissionName());
        }

        sender.sendMessage(player.getUsername() + " has the permissions " + perms);
    }
}
