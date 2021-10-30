package com.imjustdoom.justpermissions.commands.subcommands;

import com.imjustdoom.justpermissions.JustPermissions;
import com.imjustdoom.justpermissions.PermissionHandler;
import com.imjustdoom.justpermissions.config.Config;
import com.imjustdoom.justpermissions.util.MessageUtil;
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
        ArgumentWord metaAction = Word("action").from("addprefix", "removeprefix");

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

        addSyntax(this::executeMeta, players, Literal("meta"), metaAction, Word("prefix"));
    }

    private void executeMeta(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final String action = context.get("action");
        final EntityFinder target = context.get("player");
        final String group = context.get("group");
        final Player player = target.findFirstPlayer(sender);
        final String prefix = context.get("prefix");

        final String perm = "justpermissions.perms";

        if (sender.isPlayer() && !sender.hasPermission(perm)) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.MISSING_PERMISSION.replaceAll("%perm%", perm)));
            return;
        }

        if (player == null) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.UNABLE_TO_FIND_PLAYER.replaceAll("%player%", player.getUsername())));
            return;
        }

        switch (action) {
            case "addprefix":
                if (player.hasPermission("prefix." + prefix)) {
                    sender.sendMessage(MessageUtil.translate(Config.Messages.HAS_PERMISSION
                            .replaceAll("%target%", player.getUsername())
                            .replaceAll("%perm%", "prefix." + prefix)));
                    return;
                }

                PermissionHandler.addPermission(player, "prefix." + prefix);
                sender.sendMessage(MessageUtil.translate(Config.Messages.ADDED_PERMISSION
                        .replaceAll("%target%", player.getUsername()).replaceAll("%perm%", "prefix." + prefix)));
                break;
        }
    }

    private void executeGroup(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final String action = context.get("action");
        final EntityFinder target = context.get("player");
        final String group = context.get("group");
        final Player player = target.findFirstPlayer(sender);

        final String perm = "justpermissions.perms";

        if (sender.isPlayer() && !sender.hasPermission(perm)) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.MISSING_PERMISSION.replaceAll("%perm%", perm)));
            return;
        }

        if (player == null) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.UNABLE_TO_FIND_PLAYER.replaceAll("%player%", player.getUsername())));
            return;
        }

        /**
         * Checks if input group is valid
         */
        try {
            if(!JustPermissions.getInstance().getDbCon().doesContain("'" + group + "'", "name", "groups")){
                sender.sendMessage(MessageUtil.translate(Config.Messages.GROUP_DOESNT_EXIST
                        .replaceAll("%group%", group)));
                return;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        switch (action) {
            case "add":
                if (player.hasPermission("group." + group)) {
                    sender.sendMessage(MessageUtil.translate(Config.Messages.ALREADY_IN_GROUP
                            .replaceAll("%player%", player.getUsername())
                            .replaceAll("%group%", group)));
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

                sender.sendMessage(MessageUtil.translate(Config.Messages.ADDED_TO_GROUP
                        .replaceAll("%player%", player.getUsername())
                        .replaceAll("%group%", group)));
                break;
            case "remove":
                if (!player.hasPermission("group." + group)) {
                    sender.sendMessage(MessageUtil.translate(Config.Messages.NOT_IN_GROUP
                            .replaceAll("%player%", player.getUsername())
                            .replaceAll("%group%", group)));
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

                sender.sendMessage(MessageUtil.translate(Config.Messages.NO_LONGER_IN_GROUP
                        .replaceAll("%player%", player.getUsername())
                        .replaceAll("%group%", group)));
                break;
        }
    }

    private void executePerm(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final String action = context.get("action");
        final EntityFinder target = context.get("player");
        final String permission = context.get("permission");

        final Player player = target.findFirstPlayer(sender);

        final String perm = "justpermissions.perms";

        if (sender.isPlayer() && !sender.hasPermission(perm)) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.MISSING_PERMISSION.replaceAll("%perm%", perm)));
            return;
        }

        if (player == null) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.UNABLE_TO_FIND_PLAYER.replaceAll("%player%", player.getUsername())));
            return;
        }

        switch (action) {
            case "add":
                if (player.hasPermission(permission)) {
                    sender.sendMessage(MessageUtil.translate(Config.Messages.HAS_PERMISSION
                            .replaceAll("%target%", player.getUsername())
                            .replaceAll("%perm%", permission)));
                    return;
                }

                PermissionHandler.addPermission(player, permission);
                sender.sendMessage(MessageUtil.translate(Config.Messages.ADDED_PERMISSION
                        .replaceAll("%target%", player.getUsername()).replaceAll("%perm%", permission)));
                break;
            case "remove":
                if (!player.hasPermission(permission)) {
                    sender.sendMessage(MessageUtil.translate(Config.Messages.DOESNT_HAVE_PERMISSION
                            .replaceAll("%target%", player.getUsername()).replaceAll("%perm%", permission)));
                    return;
                }

                PermissionHandler.removePermission(player, permission);
                sender.sendMessage(MessageUtil.translate(Config.Messages.REMOVED_PERMISSION
                        .replaceAll("%target%", player.getUsername()).replaceAll("%perm%", permission)));
                break;
            }
    }

    private void executeClear(@NotNull CommandSender sender, @NotNull CommandContext context){
        final EntityFinder target = context.get("player");
        Player player = target.findFirstPlayer(sender);

        final String perm = "justpermissions.perms";

        if (sender.isPlayer() && !sender.hasPermission(perm)) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.MISSING_PERMISSION.replaceAll("%perm%", perm)));
            return;
        }

        /**
         * Clears all permissions
         */
        try {
            ResultSet rs = JustPermissions.getInstance().getDbCon().stmt.executeQuery("SELECT * FROM player_permissions WHERE uuid = '" + player.getUuid() + "'");
            while (rs.next()) player.removePermission(rs.getString("permission"));

            rs.close();

            JustPermissions.getInstance().getDbCon().stmt.executeUpdate("DELETE FROM player_permissions WHERE uuid = '" + player.getUuid() + "'");

            sender.sendMessage(MessageUtil.translate(Config.Messages.CLEARED_PERMISSIONS.replaceAll("%target%", player.getUsername())));
        } catch (SQLException throwables) {
            sender.sendMessage("Error while trying to remove all permissions from " + player.getUsername());
            throwables.printStackTrace();
        }
    }

    private void executeInfo(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final EntityFinder target = context.get("player");
        Player player = target.findFirstPlayer(sender);

        final String perm = "justpermissions.perms";

        if (sender.isPlayer() && !sender.hasPermission(perm)) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.MISSING_PERMISSION.replaceAll("%perm%", perm)));
            return;
        }

        List<String> perms = new ArrayList<>();
        for(Permission permission:player.getAllPermissions()){
            perms.add(permission.getPermissionName());
        }

        sender.sendMessage(MessageUtil.translate(Config.Messages.HAS_PERMISSION
                .replaceAll("%target%", player.getUsername())
                .replaceAll("%perms%", String.valueOf(perms))));
    }
}
