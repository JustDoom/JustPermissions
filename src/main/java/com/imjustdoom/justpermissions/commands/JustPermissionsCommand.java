package com.imjustdoom.justpermissions.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.minestom.server.command.builder.arguments.ArgumentType.Word;

public class JustPermissionsCommand extends Command {

    public JustPermissionsCommand() {
        super("justpermissions", "jp");

        setCondition(Conditions::playerOnly);

        ArgumentEntity players = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);
        ArgumentWord operations = Word("operation").from("player", "group");
        ArgumentWord option = Word("option").from("permission");
        ArgumentWord action = Word("action").from("add", "remove", "info");
        ArgumentWord permission = Word("permission");

        operations.setSuggestionCallback((commandSender, commandContext, suggestion) -> {
            if(commandContext.toString().equals("group")){

            }
        });

        addSyntax(this::execute, operations, players, option, action, permission);
    }

    private void execute(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final String operation = context.get("operation");
        final String option = context.get("option");
        final String action = context.get("action");
        final EntityFinder target = context.get("player");
        final String permission = context.get("permission");

        if(operation.equals("") || option.equals("") || action.equals("")){
            sender.sendMessage("You forgot to enter something");
            return;
        }

        switch (operation){
            case "player":
                player(sender, action, target, permission);
                break;

            case "group":

        }
    }

    private void player(CommandSender sender, String action, EntityFinder target, String permission){
        Player player = target.findFirstPlayer(sender);

        if(player == null){
            sender.sendMessage("The player " + target + " was unable to be found");
            return;
        }

        switch (action) {
            case "add" -> {
                if(player.hasPermission(permission)){
                    sender.sendMessage(player.getUsername() + " already has the permission " + permission);
                    return;
                }

                player.addPermission(new Permission(permission));
                sender.sendMessage("Added the permission " + permission + " to " + player.getUsername());
            }
            case "remove" -> {
                if(!player.hasPermission(permission)){
                    sender.sendMessage(player.getUsername() + " doesn't have the permission " + permission);
                    return;
                }

                player.removePermission(permission);
                sender.sendMessage("Removed the permission " + permission + " from " + player.getUsername());
            }
            case "info" -> {
                List<String> perms = new ArrayList<>();
                for(Permission perm:player.getAllPermissions()){
                    perms.add(perm.getPermissionName());
                }

                sender.sendMessage(player.getUsername() + " has the permissions " + perms);
            }
        }
    }

    private void group(){

    }
}