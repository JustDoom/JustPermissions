package com.imjustdoom.justpermissions.storage;

import com.imjustdoom.justpermissions.JustPermissions;

import java.sql.*;

public class SQLite {

    public Statement stmt;

    public SQLite() {

        String user = JustPermissions.getInstance().getRoot().node("mysql", "username").getString();
        String pass = JustPermissions.getInstance().getRoot().node("mysql", "pass").getString();
        String server = JustPermissions.getInstance().getRoot().node("mysql", "server").getString();
        String port = JustPermissions.getInstance().getRoot().node("mysql", "port").getString();
        String database = JustPermissions.getInstance().getRoot().node("mysql", "database").getString();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://" + server + ":" + port + "/" + database, user, pass);
            //here sonoo is database name, root is username and password
            stmt = con.createStatement();

            createTables();

            ResultSet rs = stmt.executeQuery("SELECT username FROM players");

            /*while(rs.next()){
                System.out.print("ID: " + rs.getString("username"));
            }*/
            //con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean doesContain(String contains, String column, String table) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT " + column + " FROM " + table + " WHERE " + column + " = " + contains);

        return rs.next();
    }

    public void insertRecord(String table, String values) throws SQLException {
        String sql = "INSERT INTO " + table + " VALUES(" + values + ")";
        stmt.executeUpdate(sql);
    }

    public void runSql(String task) throws SQLException {
        stmt.executeUpdate(task);
    }

    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS players (" +
                "`uuid` VARCHAR(36) NOT NULL, " +
                "`username` VARCHAR(16) NOT NULL, " +
                "`primary_group` VARCHAR(36) NOT NULL, " +
                "KEY `players_username` (`uuid`) USING BTREE, " +
                "PRIMARY KEY (`uuid`) " +
                ") ENGINE=InnoDB;";

        stmt.executeUpdate(sql);

        sql = "CREATE TABLE IF NOT EXISTS player_permissions (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`uuid` VARCHAR(36) NOT NULL," +
                "`permission` VARCHAR(200) NOT NULL," +
                "KEY `player_permissions_uuid` (`uuid`) USING BTREE," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB;";

        stmt.executeUpdate(sql);
    }
}

//uuid, username, main group

/**
 * CREATE TABLE `players` (
 * 	`uuid` VARCHAR(36) NOT NULL,
 * 	`username` VARCHAR(16) NOT NULL,
 * 	`primary_group` VARCHAR(36) NOT NULL,
 * 	KEY `players_username` (`uuid`) USING BTREE,
 * 	PRIMARY KEY (`uuid`)
 * ) ENGINE=InnoDB;
 */

//id, uuid, permission

/**
 * CREATE TABLE `player_permissions` (
 * 	`id` INT NOT NULL AUTO_INCREMENT,
 * 	`uuid` VARCHAR(36) NOT NULL,
 * 	`permission` VARCHAR(200) NOT NULL,
 * 	KEY `player_permissions_uuid` (`uuid`) USING BTREE,
 * 	PRIMARY KEY (`id`)
 * ) ENGINE=InnoDB;
 */