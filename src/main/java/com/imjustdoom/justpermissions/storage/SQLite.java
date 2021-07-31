package com.imjustdoom.justpermissions.storage;

import com.imjustdoom.justpermissions.JustPermissions;

import java.sql.*;

public class SQLite {

    public Statement stmt;

    public SQLite() {

        //Get db info from config
        String user = JustPermissions.getInstance().getRoot().node("mysql", "username").getString();
        String pass = JustPermissions.getInstance().getRoot().node("mysql", "pass").getString();
        String server = JustPermissions.getInstance().getRoot().node("mysql", "server").getString();
        String port = JustPermissions.getInstance().getRoot().node("mysql", "port").getString();
        String database = JustPermissions.getInstance().getRoot().node("mysql", "database").getString();

        //Connect and setup database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://" + server + ":" + port + "/" + database, user, pass);
            stmt = con.createStatement();

            createTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if table contains something
     * @param contains - check if the table contains this
     * @param column - column to check
     * @param table - table to check
     * @return - was a value returned?
     */
    public boolean doesContain(String contains, String column, String table) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT " + column + " FROM " + table + " WHERE " + column + " = " + contains);

        return rs.next();
    }

    /**
     * Inserts a record into a table
     * @param table - table to insert into
     * @param values - values to insert
     */
    public void insertRecord(String table, String values) throws SQLException {
        String sql = "INSERT INTO " + table + " VALUES(" + values + ")";
        stmt.executeUpdate(sql);
    }

    /**
     * Runs sql
     * @param task
     * @throws SQLException
     */
    public void runSql(String task) throws SQLException {
        stmt.executeUpdate(task);
    }

    /**
     * Creates the tables
     * @throws SQLException
     */
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

        sql = "CREATE TABLE IF NOT EXISTS groups (" +
                "`name` VARCHAR(36) NOT NULL," +
                "PRIMARY KEY (`name`)" +
                ") ENGINE=InnoDB;";

        stmt.executeUpdate(sql);

        sql = "CREATE TABLE IF NOT EXISTS group_permissions (" +
                "`id` INT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(36) NOT NULL," +
                "`permission` VARCHAR(200) NOT NULL," +
                "KEY `group_permissions_group` (`name`) USING BTREE," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB;";

        stmt.executeUpdate(sql);

        //Creates the default group
        insertRecord("groups", "'default'");
    }
}