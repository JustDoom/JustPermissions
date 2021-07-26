package com.imjustdoom.justpermissions.commands.subcommands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import net.minestom.server.utils.entity.EntityFinder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InfoCommand extends Command {

    public InfoCommand() {
        super("info");

        addSyntax(this::execute);
    }

    private void execute(@NotNull CommandSender sender, @NotNull CommandContext context){
        final EntityFinder target = context.get("player");
        Player player = target.findFirstPlayer(sender);

        List<String> perms = new ArrayList<>();
        for(Permission perm:player.getAllPermissions()){
            perms.add(perm.getPermissionName());
        }

        sender.sendMessage(player.getUsername() + " has the permissions " + perms);
    }
}
