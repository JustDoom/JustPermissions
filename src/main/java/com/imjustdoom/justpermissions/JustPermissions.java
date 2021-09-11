package com.imjustdoom.justpermissions;

import com.imjustdoom.justpermissions.commands.JustPermissionsCommand;
import com.imjustdoom.justpermissions.config.Config;
import com.imjustdoom.justpermissions.listeners.LoginHandler;
import com.imjustdoom.justpermissions.storage.DatabaseConnection;
import com.imjustdoom.justpermissions.util.FileUtil;
import lombok.Getter;
import lombok.SneakyThrows;
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

    private DatabaseConnection dbCon;
    public static JustPermissions instance;
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

        Config.load();

        MinecraftServer.getCommandManager().register(new JustPermissionsCommand());

        dbCon = new DatabaseConnection();

        new LoginHandler();

        /**
         * Adds groups to the group list
         */
        try {
            ResultSet rs = JustPermissions.getInstance().getDbCon().stmt.executeQuery("SELECT * FROM groups");
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