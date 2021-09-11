package com.imjustdoom.justpermissions.config;

import com.imjustdoom.justpermissions.JustPermissions;
import com.imjustdoom.justpermissions.util.FileUtil;
import net.minestom.server.MinecraftServer;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Config {

    public static CommentedConfigurationNode configFile;

    public static String STORAGE;

    public static class MySQL {
        public static String USERNAME;
        public static String PASS;
        public static String SERVER;
        public static String PORT;
        public static String DATABASE;
    }

    public static class Formatting {
        public static class Prefix {
            public static List<String> FORMAT;
        }

        public static class Suffix {
            public static List<String> FORMAT;
        }
    }

    public static void load() {

        /**
         * Checks if the config file exists
         * if not creates one
         */
        try {
            if(!FileUtil.doesFileExist(MinecraftServer.getExtensionManager().getExtensionFolder() + "/JustPermissions"))
                FileUtil.createDirectory(MinecraftServer.getExtensionManager().getExtensionFolder().getPath() + "/JustPermissions");

            if(!FileUtil.doesFileExist(MinecraftServer.getExtensionManager().getExtensionFolder().getPath() + "/JustPermissions/config.yml")) {
                JustPermissions.getInstance().getLogger().info("Config not found, creating one now");
                FileUtil.addConfig(MinecraftServer.getExtensionManager().getExtensionFolder().getPath() + "/JustPermissions/config.yml");
                JustPermissions.getInstance().getLogger().info("Config created");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * Loads config
         */
        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(Path.of(MinecraftServer.getExtensionManager().getExtensionFolder().getPath() + "/JustPermissions/config.yml")) // Set where we will load and save to
                .build();

        try {
            configFile = loader.load();
            JustPermissions.getInstance().getLogger().info("Config has been loaded");
        } catch (IOException e) {
            JustPermissions.getInstance().getLogger().error("An error occurred while loading this configuration: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            System.exit(1);
            return;
        }

        STORAGE = configFile.node("storage").getString();

        MySQL.USERNAME = configFile.node("mysql", "username").getString();
        MySQL.PASS = configFile.node("mysql", "pass").getString();
        MySQL.SERVER = configFile.node("mysql", "server").getString();
        MySQL.PORT = configFile.node("mysql", "port").getString();
        MySQL.DATABASE = configFile.node("mysql", "username").getString();

        try {
            Formatting.Prefix.FORMAT = configFile.node("formatting", "prefix", "format").getList(String.class);

            Formatting.Suffix.FORMAT = configFile.node("formatting", "suffix", "format").getList(String.class);
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }
}
