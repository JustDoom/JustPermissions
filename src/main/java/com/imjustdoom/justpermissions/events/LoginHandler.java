package com.imjustdoom.justpermissions.events;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.permission.PermissionHandler;

public class LoginHandler {

    public LoginHandler(){
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(AsyncPlayerPreLoginEvent.class, event -> {
            PermissionHandler
        });
    }
}