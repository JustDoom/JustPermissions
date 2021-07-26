package com.imjustdoom.justpermissions.commands;

import com.imjustdoom.justpermissions.commands.subcommands.GroupSubcommand;
import com.imjustdoom.justpermissions.commands.subcommands.PlayerSubcommand;
import net.minestom.server.command.builder.Command;

public class JustPermissionsCommand extends Command {

    public JustPermissionsCommand() {
        super("justpermissions", "jp");

        addSubcommand(new PlayerSubcommand());
        addSubcommand(new GroupSubcommand());
    }
}