
package id.attributesrpg;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final DatabaseManager database;

    private final Map<UUID, PlayerData> cache =
            new ConcurrentHashMap<>();

    public PlayerDataManager(DatabaseManager database) {
        this.database = database;
    }

    public PlayerData loadPlayer(UUID uuid, String name)
            throws SQLException {

        database.createPlayerIfAbsent(uuid, name);

        String sql = """
            SELECT * FROM player_data WHERE uuid = ?;
        """;

        try (PreparedStatement statement =
                     database.getConnection().prepareStatement(sql)) {

            statement.setString(1, uuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                            "Data pemain tidak ditemukan."
                    );
                }

                PlayerData data = new PlayerData(uuid, name);

                data.setLevel(result.getInt("rpg_level"));
                data.setExp(result.getInt("exp"));

                data.setAttributePoints(
                        result.getInt("attribute_points")
                );

                for (String attribute :
                        data.getAttributes().keySet()) {

                    data.setAttribute(
                            attribute,
                            result.getInt(attribute)
                    );
                }

                // Memulihkan status Knock.
                data.setKnocked(
                        result.getInt("knocked") == 1
                );

                data.setKnockExpiresAt(
                        result.getLong("knock_expires_at")
                );

                cache.put(uuid, data);

                return data;
            }
        }
    }

    public PlayerData getPlayer(UUID uuid) {
        return cache.get(uuid);
    }

    public void savePlayer(PlayerData data) throws SQLException {

        String sql = """
            UPDATE player_data SET
                player_name = ?,
                rpg_level = ?,
                exp = ?,
                attribute_points = ?,
                strength = ?,
                defense = ?,
                vitality = ?,
                agility = ?,
                intelligence = ?,
                luck = ?,
                endurance = ?,
                dexterity = ?,
                knocked = ?,
                knock_expires_at = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE uuid = ?;
        """;

        try (PreparedStatement statement =
                     database.getConnection().prepareStatement(sql)) {

            statement.setString(1, data.getPlayerName());
            statement.setInt(2, data.getLevel());
            statement.setInt(3, data.getExp());
            statement.setInt(4, data.getAttributePoints());

            int index = 5;

            for (String attribute :
                    data.getAttributes().keySet()) {

                statement.setInt(
                        index++,
                        data.getAttribute(attribute)
                );
            }

            statement.setInt(
                    13,
                    data.isKnocked() ? 1 : 0
            );

            statement.setLong(
                    14,
                    data.getKnockExpiresAt()
            );

            statement.setString(
                    15,
                    data.getUuid().toString()
            );

            statement.executeUpdate();
        }
    }

    public void unloadPlayer(UUID uuid) throws SQLException {

        PlayerData data = cache.get(uuid);

        if (data != null) {
            savePlayer(data);
            cache.remove(uuid);
        }
    }

    public void saveAll() {
        for (PlayerData data : cache.values()) {
            try {
                savePlayer(data);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }
}
