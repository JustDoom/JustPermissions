package com.imjustdoom.justpermissions;

import com.imjustdoom.justpermissions.commands.JustPermissionsCommand;
import com.imjustdoom.justpermissions.listeners.LoginHandler;
import com.imjustdoom.justpermissions.storage.SQLite;
import com.imjustdoom.justpermissions.util.FileUtil;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.extensions.Extension;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class JustPermissions extends Extension {

    private SQLite sqLite;
    public static JustPermissions instance;
    private CommentedConfigurationNode root;
    private List<String> groups = new ArrayList<>();
    private HashMap<Player, String> players = new HashMap<>();

    public JustPermissions(){
        instance = this;
    }

    public static JustPermissions getInstance(){
        return instance;
    }

    @Override
    public void initialize() {

        /**
         * Checks if the config file exists
         * if not creates one
         */
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

        /**
         * Loads config
         */
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

        /**
         * Adds groups to the group list
         */
        try {
            ResultSet rs = JustPermissions.getInstance().getSqLite().stmt.executeQuery("SELECT * FROM groups");
            while (rs.next()){
                groups.add(rs.getString("name"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void terminate() {
        //hi
    }
}