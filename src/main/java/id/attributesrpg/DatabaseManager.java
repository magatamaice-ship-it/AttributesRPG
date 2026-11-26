
package id.attributesrpg;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

                    knocked INTEGER NOT NULL DEFAULT 0,
                    knock_expires_at INTEGER NOT NULL DEFAULT 0,

                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_player_level
                ON player_data(rpg_level DESC);
            """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS player_skills (
                    uuid TEXT NOT NULL,
                    slot INTEGER NOT NULL,
                    skill_name TEXT NOT NULL,
                    PRIMARY KEY (uuid, slot),
                    FOREIGN KEY (uuid)
                        REFERENCES player_data(uuid)
                        ON DELETE CASCADE
                );
            """);
        }

        ensureColumn("knocked", "INTEGER NOT NULL DEFAULT 0");
        ensureColumn("knock_expires_at",
                "INTEGER NOT NULL DEFAULT 0");

        plugin.getLogger().info(
                "Database SQLite berhasil disiapkan!"
        );
    }

    private void ensureColumn(String column, String definition)
            throws SQLException {

        boolean exists = false;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "PRAGMA table_info(player_data);")) {

            while (result.next()) {
                if (column.equalsIgnoreCase(
                        result.getString("name"))) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(
                        "ALTER TABLE player_data ADD COLUMN "
                                + column + " " + definition + ";"
                );
            }

            plugin.getLogger().info(
                    "Kolom database ditambahkan: " + column
            );
        }
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

    public void saveSkill(UUID uuid, int slot, String skillName)
            throws SQLException {

        if (slot < 1 || slot > 3) {
            throw new SQLException("Slot skill tidak valid.");
        }

        String sql = """
            INSERT INTO player_skills (uuid, slot, skill_name)
            VALUES (?, ?, ?)
            ON CONFLICT(uuid, slot)
            DO UPDATE SET skill_name = excluded.skill_name;
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, uuid.toString());
            statement.setInt(2, slot);
            statement.setString(3, skillName);
            statement.executeUpdate();
        }
    }

    public void removeSkill(UUID uuid, int slot)
            throws SQLException {

        String sql = """
            DELETE FROM player_skills
            WHERE uuid = ? AND slot = ?;
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, uuid.toString());
            statement.setInt(2, slot);
            statement.executeUpdate();
        }
    }

    public ResultSet getSkills(UUID uuid) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("""
            SELECT slot, skill_name
            FROM player_skills
            WHERE uuid = ?
            ORDER BY slot ASC;
        """);

        statement.setString(1, uuid.toString());
        return statement.executeQuery();
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
