package com.imjustdoom.justpermissions;

import com.imjustdoom.justpermissions.commands.JustPermissionsCommand;
import com.imjustdoom.justpermissions.listeners.LoginHandler;
import com.imjustdoom.justpermissions.storage.SQLite;
import com.imjustdoom.justpermissions.util.FileUtil;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;

@Getter
public class JustPermissions extends Extension {

    private SQLite sqLite;
    public static JustPermissions instance;
    public CommentedConfigurationNode root;

    public JustPermissions(){
        instance = this;
    }

    public static JustPermissions getInstance(){
        return instance;
    }

    @Override
    public void initialize() {

        try {
            if(!FileUtil.doesFileExist("./extensions/JustPermissions"))
                FileUtil.createDirectory("./extensions/JustPermissions");

            if(!FileUtil.doesFileExist("./extensions/JustPermissions/config.yml")) {
                getLogger().info("Config not found, creating one now");
                FileUtil.addConfig("./extensions/JustPermissions/config.yml");
                getLogger().info("Config created");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(Path.of("./extensions/JustPermissions/config.yml")) // Set where we will load and save to
                .build();

        try {
            root = loader.load();
            getLogger().info("Config has been loaded");
        } catch (IOException e) {
            System.err.println("An error occurred while loading this configuration: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            System.exit(1);
            return;
        }

        MinecraftServer.getCommandManager().register(new JustPermissionsCommand());

        sqLite = new SQLite();

        new LoginHandler();
    }

    @Override
    public void terminate() {

    }
}