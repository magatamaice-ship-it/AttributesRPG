
package id.attributesrpg;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class DatabaseManager {

    private final AttributesRPG plugin;
    private Connection connection;

    public DatabaseManager(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public void initialize() throws SQLException {
        if (!plugin.getDataFolder().exists()
                && !plugin.getDataFolder().mkdirs()) {
            throw new SQLException("Gagal membuat folder plugin.");
        }

        String databaseName = plugin.getConfig()
                .getString("database.file", "players.db");

        File databaseFile = new File(
                plugin.getDataFolder(),
                databaseName
        );

        connection = DriverManager.getConnection(
                "jdbc:sqlite:" + databaseFile.getAbsolutePath()
        );

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL;");
            statement.execute("PRAGMA foreign_keys=ON;");

            statement.execute("""
                CREATE TABLE IF NOT EXISTS player_data (
                    uuid TEXT PRIMARY KEY,
                    player_name TEXT NOT NULL,
                    rpg_level INTEGER NOT NULL DEFAULT 0,
                    exp INTEGER NOT NULL DEFAULT 0,
                    attribute_points INTEGER NOT NULL DEFAULT 0,

                    strength INTEGER NOT NULL DEFAULT 0,
                    defense INTEGER NOT NULL DEFAULT 0,
                    vitality INTEGER NOT NULL DEFAULT 0,
                    agility INTEGER NOT NULL DEFAULT 0,
                    intelligence INTEGER NOT NULL DEFAULT 0,
                    luck INTEGER NOT NULL DEFAULT 0,
                    endurance INTEGER NOT NULL DEFAULT 0,
                    dexterity INTEGER NOT NULL DEFAULT 0,

                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_player_level
                ON player_data(rpg_level DESC);
            """);
        }

        plugin.getLogger().info("Database SQLite berhasil disiapkan!");
    }

    public void createPlayerIfAbsent(UUID uuid, String playerName)
            throws SQLException {

        String sql = """
            INSERT OR IGNORE INTO player_data
            (uuid, player_name)
            VALUES (?, ?);
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, uuid.toString());
            statement.setString(2, playerName);
            statement.executeUpdate();
        }

        updatePlayerName(uuid, playerName);
    }

    public void updatePlayerName(UUID uuid, String playerName)
            throws SQLException {

        String sql = """
            UPDATE player_data
            SET player_name = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE uuid = ?;
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, playerName);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
            plugin.getLogger().info("Database SQLite ditutup.");
        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal menutup database: " + exception.getMessage()
            );
        }
    }
}
