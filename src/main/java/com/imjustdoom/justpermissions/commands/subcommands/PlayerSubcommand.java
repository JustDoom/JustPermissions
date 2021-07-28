package com.imjustdoom.justpermissions.commands.subcommands;

import com.imjustdoom.justpermissions.JustPermissions;
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;
import static net.minestom.server.command.builder.arguments.ArgumentType.Word;

public class PlayerSubcommand extends Command {

    public PlayerSubcommand() {
        super("player");

        ArgumentEntity players = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);
        ArgumentWord action = Word("action").from("add", "remove");
        ArgumentWord permission = Word("permission");

        addSyntax(this::executeInfo, players, Literal("info"));
        addSyntax(this::executePerm, players, Literal("permission"), action, permission);
    }

    private void executePerm(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final String action = context.get("action");
        final EntityFinder target = context.get("player");
        final String permission = context.get("permission");

        executePerm(target, sender, permission, action);
    }

    private void executePerm(EntityFinder target, CommandSender sender, String permission, String action) {
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

                try {
                    JustPermissions.getInstance().getSqLite().insertRecord("player_permissions", "0, '" + player.getUuid() + "', '" + permission + "'");
                    player.addPermission(new Permission(permission));
                    sender.sendMessage("Added the permission " + permission + " to " + player.getUsername());
                } catch (SQLException throwables) {
                    sender.sendMessage("Error while trying to add permission to " + player.getUsername());
                    throwables.printStackTrace();
                }
            }
            case "remove" -> {
                if (!player.hasPermission(permission)) {
                    sender.sendMessage(player.getUsername() + " doesn't have the permission " + permission);
                    return;
                }

                try {
                    JustPermissions.getInstance().getSqLite().runSql("DELETE FROM player_permissions WHERE uuid = '" + player.getUuid() + "' AND permission = '" + permission + "'");
                    player.removePermission(permission);
                    sender.sendMessage("Removed the permission " + permission + " from " + player.getUsername());
                } catch (SQLException throwables) {
                    sender.sendMessage("Error while trying to remove permission from " + player.getUsername());
                    throwables.printStackTrace();
                }
            }
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
