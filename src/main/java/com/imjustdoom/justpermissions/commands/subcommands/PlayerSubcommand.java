package com.imjustdoom.justpermissions.commands.subcommands;

import net.minestom.server.command.builder.Command;

public class PlayerSubcommand extends Command {

    public PlayerSubcommand() {
        super("player");

        addSubcommand(new PermissionCommand());
        addSubcommand(new InfoCommand());
    }
}
