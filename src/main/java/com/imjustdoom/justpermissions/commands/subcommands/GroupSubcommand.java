package com.imjustdoom.justpermissions.commands.subcommands;

import com.imjustdoom.justpermissions.JustPermissions;
import com.imjustdoom.justpermissions.PermissionHandler;
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
        for (Extension extension : MinecraftServer.getExtensionManager().getExtensions()){
            if(extension.getOrigin().getMeta().get("permissions") == null) continue;
            for (int i = 0; i < extension.getOrigin().getMeta().get("permissions").getAsJsonArray().size(); i++) {
                if(extension.getOrigin().getMeta().get("permissions").getAsJsonArray().get(i) == null) continue;
                permissions.add(extension.getOrigin().getMeta().get("permissions").getAsJsonArray().get(i).getAsString());
            }
        }

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

        try {
            if (!JustPermissions.getInstance().getDbCon().doesContain("'" + group + "'", "name", "groups")) {
                sender.sendMessage("The group " + group + " was unable to be found");
                return;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {

            ResultSet rs = JustPermissions.getInstance().getDbCon().stmt.
                    executeQuery("SELECT * FROM group_permissions WHERE name = '" + group + "' AND permission = '" + permission + "'");

            switch (action) {
                case "add" -> {
                    if (rs.next()) {
                        sender.sendMessage(group + " already has the permission " + permission);
                        return;
                    }

                    PermissionHandler.addPermission(group, permission);

                    for (Map.Entry<Player, String> pair : JustPermissions.getInstance().getPlayers().entrySet()) {
                        if (pair.getValue().equalsIgnoreCase(group)) {
                            pair.getKey().addPermission(new Permission(permission));
                        }
                    }

                    sender.sendMessage("Added the permission " + permission + " to " + group);
                }
                case "remove" -> {
                    if (!rs.next()) {
                        sender.sendMessage(group + " doesn't have the permission " + permission);
                        return;
                    }

                    PermissionHandler.removePermission(group, permission);

                    for (Map.Entry<Player, String> pair : JustPermissions.getInstance().getPlayers().entrySet()) {
                        if (pair.getValue().equalsIgnoreCase(group)) {
                            pair.getKey().removePermission(permission);
                        }
                    }

                    sender.sendMessage("Removed the permission " + permission + " from " + group);
                }
            }

            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void executeClear(@NotNull CommandSender sender, @NotNull CommandContext context){
        final String group = context.get("group");

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

            sender.sendMessage("Cleared all permissions from " +group);
        } catch (SQLException throwables) {
            sender.sendMessage("Error while trying to remove all permissions from " + group);
            throwables.printStackTrace();
        }
    }

    private void executeInfo(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final String group = context.get("group");

        List<String> perms = new ArrayList<>();

        /**
         * Get all permissions from the group
         */
        try {
            ResultSet rs = JustPermissions.getInstance().getDbCon().stmt.
                    executeQuery("SELECT * FROM group_permissions WHERE name = '" + group + "'");

            while (rs.next()) {
                perms.add(rs.getString("permission"));
            }

            sender.sendMessage(group + " has the permissions " + perms);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}