
package id.attributesrpg;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SkillManager {

    public static final int MAX_EQUIPPED_SKILLS = 3;
    private static final long COMBAT_DURATION = 15_000L;

    private final AttributesRPG plugin;

    private final Map<UUID, List<RpgSkill>> equippedSkills =
            new HashMap<>();

    private final Map<UUID, Map<RpgSkill, Long>> cooldowns =
            new HashMap<>();

    private final Map<UUID, Long> lastCombat =
            new HashMap<>();

    public SkillManager(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public boolean isUnlocked(Player player, RpgSkill skill) {
        PlayerData data = getData(player);

        return data != null
                && data.getAttribute(skill.getAttribute()) >= 10;
    }

    public int getSkillLevel(Player player, RpgSkill skill) {
        PlayerData data = getData(player);

        if (data == null) {
            return 0;
        }

        int attributeLevel =
                data.getAttribute(skill.getAttribute());

        if (attributeLevel >= 40) {
            return 3;
        }

        if (attributeLevel >= 25) {
            return 2;
        }

        if (attributeLevel >= 10) {
            return 1;
        }

        return 0;
    }

    public boolean equipSkill(Player player, RpgSkill skill) {
        if (isInCombat(player)
                || !isUnlocked(player, skill)) {
            return false;
        }

        List<RpgSkill> skills = getEquippedSkills(player);

        if (skills.contains(skill)
                || skills.size() >= MAX_EQUIPPED_SKILLS) {
            return false;
        }

        skills.add(skill);

        if (!saveLoadout(player.getUniqueId(), skills)) {
            skills.remove(skill);
            return false;
        }

        return true;
    }

    public boolean unequipSkill(Player player, RpgSkill skill) {
        if (isInCombat(player)) {
            return false;
        }

        List<RpgSkill> skills = getEquippedSkills(player);

        if (!skills.remove(skill)) {
            return false;
        }

        if (!saveLoadout(player.getUniqueId(), skills)) {
            skills.add(skill);
            return false;
        }

        return true;
    }

    public List<RpgSkill> getEquippedSkills(Player player) {
        return equippedSkills.computeIfAbsent(
                player.getUniqueId(),
                uuid -> new ArrayList<>()
        );
    }

    public boolean isEquipped(Player player, RpgSkill skill) {
        return getEquippedSkills(player).contains(skill);
    }

    public boolean isOnCooldown(Player player, RpgSkill skill) {
        return getCooldowns(player).getOrDefault(skill, 0L)
                > System.currentTimeMillis();
    }

    public long getRemainingCooldown(
            Player player,
            RpgSkill skill
    ) {
        long remaining =
                getCooldowns(player).getOrDefault(skill, 0L)
                        - System.currentTimeMillis();

        return Math.max(0, (remaining + 999) / 1000);
    }

    public void startCooldown(Player player, RpgSkill skill) {
        getCooldowns(player).put(
                skill,
                System.currentTimeMillis()
                        + skill.getCooldownSeconds() * 1000L
        );
    }

    public void recordCombat(Player player) {
        lastCombat.put(
                player.getUniqueId(),
                System.currentTimeMillis()
        );
    }

    public boolean isInCombat(Player player) {
        Long last = lastCombat.get(player.getUniqueId());

        return last != null
                && System.currentTimeMillis() - last
                < COMBAT_DURATION;
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        List<RpgSkill> skills = new ArrayList<>();

        String sql = """
            SELECT slot, skill_name
            FROM player_skills
            WHERE uuid = ?
            ORDER BY slot ASC;
        """;

        try (PreparedStatement statement =
                     plugin.getDatabaseManager()
                             .getConnection()
                             .prepareStatement(sql)) {

            statement.setString(1, uuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    RpgSkill skill = RpgSkill.fromName(
                            result.getString("skill_name")
                    );

                    if (skill != null
                            && skills.size() < MAX_EQUIPPED_SKILLS
                            && !skills.contains(skill)) {
                        skills.add(skill);
                    }
                }
            }

            equippedSkills.put(uuid, skills);

        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal memuat skill " + player.getName()
                            + ": " + exception.getMessage()
            );
        }
    }

    private boolean saveLoadout(
            UUID uuid,
            List<RpgSkill> skills
    ) {
        String deleteSql =
                "DELETE FROM player_skills WHERE uuid = ?";

        String insertSql = """
            INSERT INTO player_skills (uuid, slot, skill_name)
            VALUES (?, ?, ?);
        """;

        Connection connection =
                plugin.getDatabaseManager().getConnection();

        boolean previousAutoCommit;

        try {
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement delete =
                         connection.prepareStatement(deleteSql);
                 PreparedStatement insert =
                         connection.prepareStatement(insertSql)) {

                delete.setString(1, uuid.toString());
                delete.executeUpdate();

                for (int i = 0; i < skills.size(); i++) {
                    insert.setString(1, uuid.toString());
                    insert.setInt(2, i + 1);
                    insert.setString(3, skills.get(i).name());
                    insert.addBatch();
                }

                insert.executeBatch();
                connection.commit();
                return true;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;

            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }

        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan loadout skill: "
                            + exception.getMessage()
            );
            return false;
        }
    }

    private Map<RpgSkill, Long> getCooldowns(Player player) {
        return cooldowns.computeIfAbsent(
                player.getUniqueId(),
                uuid -> new HashMap<>()
        );
    }

    private PlayerData getData(Player player) {
        return plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());
    }

    public void clearPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        equippedSkills.remove(uuid);
        cooldowns.remove(uuid);
        lastCombat.remove(uuid);
    }
}
