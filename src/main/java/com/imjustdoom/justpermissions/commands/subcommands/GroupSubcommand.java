package com.imjustdoom.justpermissions.commands.subcommands;

import com.imjustdoom.justpermissions.JustPermissions;
import com.imjustdoom.justpermissions.PermissionHandler;
import com.imjustdoom.justpermissions.config.Config;
import com.imjustdoom.justpermissions.util.MessageUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import net.minestom.server.extensions.Extension;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static net.minestom.server.command.builder.arguments.ArgumentType.Word;

public class GroupSubcommand extends Command {

    public GroupSubcommand() {
        super("group");

        //ArgumentEntity players = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);
        ArgumentWord option = Word("option").from("permission");
        ArgumentWord action = Word("action").from("add", "remove");
        ArgumentWord group = Word("group");

        List<String> permissions = new ArrayList<>();
        /**for (Extension extension : MinecraftServer.getExtensionManager().getExtensions()){
            if(extension.getOrigin().getMeta().get("permissions") == null) continue;
            for (int i = 0; i < extension.getOrigin().getMeta().get("permissions").getAsJsonArray().size(); i++) {
                if(extension.getOrigin().getMeta().get("permissions").getAsJsonArray().get(i) == null) continue;
                permissions.add(extension.getOrigin().getMeta().get("permissions").getAsJsonArray().get(i).getAsString());
            }
        }**/

        String[] permissionArray = permissions.toArray(new String[permissions.size()]);
        ArgumentWord permission = Word("permission").from(permissionArray);

        addSyntax(this::executePerm, group, option, action, permission);
        addSyntax(this::executeClear, group, option, Literal("clear"));
        addSyntax(this::executeInfo, group, Literal("info"));
    }

    private void executePerm(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final String action = context.get("action");
        final String permission = context.get("permission");
        final String group = context.get("group");

        final String perm = "justpermissions.perms";

        if (sender.isPlayer() && !sender.hasPermission(perm)) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.MISSING_PERMISSION.replaceAll("%perm%", perm)));
            return;
        }

        try {
            if (!JustPermissions.getInstance().getDbCon().doesContain("'" + group + "'", "name", "groups")) {
                sender.sendMessage(MessageUtil.translate(Config.Messages.UNABLE_TO_FIND_GROUP.replaceAll("%group%", group)));
                return;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {

            ResultSet rs = JustPermissions.getInstance().getDbCon().stmt.
                    executeQuery("SELECT * FROM group_permissions WHERE name = '" + group + "' AND permission = '" + permission + "'");

            switch (action) {
                case "add":
                    if (rs.next()) {
                        sender.sendMessage(MessageUtil.translate(Config.Messages.HAS_PERMISSION
                                .replaceAll("%target%", group)
                                .replaceAll("%perm%", permission)));
                        return;
                    }

                    PermissionHandler.addPermission(group, permission);

                    for (Map.Entry<Player, String> pair : JustPermissions.getInstance().getPlayers().entrySet()) {
                        if (pair.getValue().equalsIgnoreCase(group)) {
                            pair.getKey().addPermission(new Permission(permission));
                        }
                    }

                    sender.sendMessage(MessageUtil.translate(Config.Messages.ADDED_PERMISSION
                            .replaceAll("%target%", group).replaceAll("%perm%", permission)));
                    break;
                case "remove":
                    if (!rs.next()) {
                        sender.sendMessage(MessageUtil.translate(Config.Messages.DOESNT_HAVE_PERMISSION
                                .replaceAll("%target%", group).replaceAll("%perm%", permission)));
                        return;
                    }

                    PermissionHandler.removePermission(group, permission);

                    for (Map.Entry<Player, String> pair : JustPermissions.getInstance().getPlayers().entrySet())
                        if (pair.getValue().equalsIgnoreCase(group))
                            pair.getKey().removePermission(permission);

                    sender.sendMessage(MessageUtil.translate(Config.Messages.REMOVED_PERMISSION
                            .replaceAll("%target%", group).replaceAll("%perm%", permission)));
                    break;
            }

            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void executeClear(@NotNull CommandSender sender, @NotNull CommandContext context){
        final String group = context.get("group");

        final String perm = "justpermissions.perms";

        if (sender.isPlayer() && !sender.hasPermission(perm)) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.MISSING_PERMISSION.replaceAll("%perm%", perm)));
            return;
        }

        /**
         * Clears all permissions
         */
        try {
            ResultSet rs = JustPermissions.getInstance().getDbCon().stmt.executeQuery("SELECT * FROM group_permissions WHERE name = '" +group + "'");
            while (rs.next()){
                String permission = rs.getString("permission");

                PermissionHandler.removePermission(group, permission);

                for (Map.Entry<Player, String> pair : JustPermissions.getInstance().getPlayers().entrySet()) {
                    if (pair.getValue().equalsIgnoreCase(group)) {
                        pair.getKey().removePermission(permission);
                    }
                }
            }

            rs.close();

            JustPermissions.getInstance().getDbCon().stmt.executeUpdate("DELETE FROM group_permissions WHERE name = '" + group + "'");

            sender.sendMessage(MessageUtil.translate(Config.Messages.CLEARED_PERMISSIONS.replaceAll("%target%", group)));
        } catch (SQLException throwables) {
            sender.sendMessage("Error while trying to remove all permissions from " + group);
            throwables.printStackTrace();
        }
    }

    private void executeInfo(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final String group = context.get("group");

        List<String> perms = new ArrayList<>();

        final String perm = "justpermissions.perms";

        if (sender.isPlayer() && !sender.hasPermission(perm)) {
            sender.sendMessage(MessageUtil.translate(Config.Messages.MISSING_PERMISSION.replaceAll("%perm%", perm)));
            return;
        }

        /**
         * Get all permissions from the group
         */
        try {
            ResultSet rs = JustPermissions.getInstance().getDbCon().stmt.
                    executeQuery("SELECT * FROM group_permissions WHERE name = '" + group + "'");

            while (rs.next()) perms.add(rs.getString("permission"));

            sender.sendMessage(MessageUtil.translate(Config.Messages.HAS_PERMISSION
                    .replaceAll("%target%", group)
                    .replaceAll("%perms%", String.valueOf(perms))));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}