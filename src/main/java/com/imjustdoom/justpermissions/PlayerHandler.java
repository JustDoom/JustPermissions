package com.imjustdoom.justpermissions;

import com.imjustdoom.justpermissions.data.PlayerJP;
import net.minestom.server.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerHandler {

    public Map<Player, PlayerJP> dataMap = new ConcurrentHashMap<>();

    public void addPlayer(Player player) {
        dataMap.put(player, new PlayerJP());
    }

    public boolean containsPlayer(PlayerJP data){
        if(dataMap.containsValue(data)){
            return true;
        } else {
            return false;
        }
    }

    public void removePlayer(Player player) {
        dataMap.remove(player);
    }

    public PlayerJP getData(Player player) {
        return dataMap.get(player);
    }

    public Map getPlayers() {
        return dataMap;
    }
}
