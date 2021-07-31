package com.imjustdoom.justpermissions.commands.subcommands;

import com.imjustdoom.justpermissions.JustPermissions;
import com.imjustdoom.justpermissions.PermissionHandler;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static net.minestom.server.command.builder.arguments.ArgumentType.*;

public class GroupSubcommand extends Command {

    public GroupSubcommand() {
        super("group");

        //ArgumentEntity players = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);
        ArgumentWord option = Word("option").from("permission");
        ArgumentWord action = Word("action").from("add", "remove");
        ArgumentWord permission = Word("permission");

        addSyntax(this::executePerm, Word("group"), option, action, permission);
        addSyntax(this::executeInfo, Word("group"), Literal("info"));
    }

    private void executePerm(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final String action = context.get("action");
        final String permission = context.get("permission");
        final String group = context.get("group");

        try {
            if (!JustPermissions.getInstance().getSqLite().doesContain("'" + group + "'", "name", "groups")) {
                sender.sendMessage("The group " + group + " was unable to be found");
                return;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        try {

            ResultSet rs = JustPermissions.getInstance().getSqLite().stmt.
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

    private void executeInfo(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final String group = context.get("group");

        List<String> perms = new ArrayList<>();

        try {
            ResultSet rs = JustPermissions.getInstance().getSqLite().stmt.
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