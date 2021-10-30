package com.imjustdoom.justpermissions.listeners;

import com.imjustdoom.justpermissions.JustPermissions;
import com.imjustdoom.justpermissions.data.PlayerJP;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerChatEvent;

public class ChatListener {

    public ChatListener() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addListener(PlayerChatEvent.class, event -> {
            PlayerJP user = JustPermissions.getInstance().getPlayerHandler().getData(event.getPlayer());
            event.setChatFormat(playerChatEvent -> Component.text("<" + user.getPrefix() + event.getPlayer().getUsername() + "> " + playerChatEvent.getMessage()));
        });
    }
}
