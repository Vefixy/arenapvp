package com.arenapvp.storage;

import com.arenapvp.ArenaPVPPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public final class DatabaseManager {

    private final ArenaPVPPlugin plugin;
    private Connection connection;

    public DatabaseManager(ArenaPVPPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            File dbFile = new File(plugin.getDataFolder(), plugin.pluginConfig().getDatabaseFilename());
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_stats (
                        uuid TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        kills INTEGER NOT NULL DEFAULT 0,
                        deaths INTEGER NOT NULL DEFAULT 0,
                        current_streak INTEGER NOT NULL DEFAULT 0,
                        best_streak INTEGER NOT NULL DEFAULT 0
                    )
                    """);
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS arenas (
                        name TEXT PRIMARY KEY,
                        world TEXT NOT NULL,
                        x REAL NOT NULL,
                        y REAL NOT NULL,
                        z REAL NOT NULL,
                        yaw REAL NOT NULL,
                        pitch REAL NOT NULL
                    )
                    """);
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS local_kits (
                        name TEXT PRIMARY KEY,
                        contents TEXT NOT NULL,
                        armor TEXT NOT NULL,
                        offhand TEXT
                    )
                    """);
                migrateLegacyArena(statement);
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", ex);
        }
    }

    private void migrateLegacyArena(Statement statement) throws SQLException {
        boolean legacyExists;
        try (ResultSet tables = connection.getMetaData().getTables(null, null, "arena_location", null)) {
            legacyExists = tables.next();
        }
        if (!legacyExists) {
            return;
        }
        try (ResultSet rs = statement.executeQuery("SELECT world, x, y, z, yaw, pitch FROM arena_location WHERE id = 1")) {
            if (rs.next()) {
                try (var ps = connection.prepareStatement("""
                        INSERT OR IGNORE INTO arenas (name, world, x, y, z, yaw, pitch)
                        VALUES ('default', ?, ?, ?, ?, ?, ?)
                        """)) {
                    ps.setString(1, rs.getString("world"));
                    ps.setDouble(2, rs.getDouble("x"));
                    ps.setDouble(3, rs.getDouble("y"));
                    ps.setDouble(4, rs.getDouble("z"));
                    ps.setFloat(5, rs.getFloat("yaw"));
                    ps.setFloat(6, rs.getFloat("pitch"));
                    ps.executeUpdate();
                }
                plugin.getLogger().info("Migrated legacy arena_location to arenas table as 'default'.");
            }
        }
        statement.execute("DROP TABLE IF EXISTS arena_location");
    }

    public Connection connection() {
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.WARNING, "Failed to close database", ex);
            }
        }
    }
}
