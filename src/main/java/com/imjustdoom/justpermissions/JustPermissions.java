package com.imjustdoom.justpermissions;

import com.imjustdoom.justpermissions.commands.JustPermissionsCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;

public class JustPermissions extends Extension {
    @Override
    public void initialize() {
        MinecraftServer.getCommandManager().register(new JustPermissionsCommand());
    }

    @Override
    public void terminate() {

    }
}