package com.imjustdoom.justpermissions;

import com.imjustdoom.justpermissions.commands.JustPermissionsCommand;
import com.imjustdoom.justpermissions.listeners.LoginHandler;
import com.imjustdoom.justpermissions.storage.SQLite;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;

@Getter
public class JustPermissions extends Extension {

    private SQLite sqLite;
    public static JustPermissions instance;

    public JustPermissions(){
        instance = this;
    }

    public static JustPermissions getInstance(){
        return instance;
    }

    @Override
    public void initialize() {
        MinecraftServer.getCommandManager().register(new JustPermissionsCommand());

        sqLite = new SQLite();

        new LoginHandler();
    }

    @Override
    public void terminate() {

    }
}