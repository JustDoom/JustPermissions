package com.imjustdoom.justpermissions.commands.subcommands;

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

import java.util.ArrayList;
import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.Word;

public class PermissionCommand extends Command {

    public PermissionCommand() {
        super("permission");

        ArgumentWord action = Word("action").from("add", "remove");
        ArgumentWord permission = Word("permission");

        addSyntax(this::execute, action, permission);
    }

    private void execute(@NotNull CommandSender sender, @NotNull CommandContext context){
        final String option = context.get("option");
        final String action = context.get("action");
        final EntityFinder target = context.get("player");
        final String permission = context.get("permission");
        Player player = target.findFirstPlayer(sender);

        if(player == null){
            sender.sendMessage("The player " + target + " was unable to be found");
            return;
        }

        switch (option){
            case "permission" -> {
                switch (action) {
                    case "add" -> addPerm(permission, player, sender);
                    case "remove" -> removePerm(permission, player, sender);
                }
            }
        }
    }

    /**
     *
     * @param permission - The permission to be added
     * @param player - Player to add the permission to
     * @param sender - Command Sender
     */
    private void addPerm(String permission, Player player, CommandSender sender){
        if(player.hasPermission(permission)){
            sender.sendMessage(player.getUsername() + " already has the permission " + permission);
            return;
        }

        player.addPermission(new Permission(permission));
        sender.sendMessage("Added the permission " + permission + " to " + player.getUsername());
    }

    /**
     *
     * @param permission - The permission to be removed
     * @param player - Player to remove the permission from
     * @param sender - Command sender
     */
    private void removePerm(String permission, Player player, CommandSender sender){
        if(!player.hasPermission(permission)){
            sender.sendMessage(player.getUsername() + " doesn't have the permission " + permission);
            return;
        }

        player.removePermission(permission);
        sender.sendMessage("Removed the permission " + permission + " from " + player.getUsername());
    }
}
