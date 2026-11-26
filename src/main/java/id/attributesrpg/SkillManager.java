
package id.attributesrpg;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SkillManager {

    private static final int MAX_EQUIPPED_SKILLS = 3;
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

        if (data == null || skill == null) {
            return false;
        }

        return data.getAttribute(skill.getAttribute())
                >= skill.getUnlockLevel();
    }

    public int getSkillLevel(Player player, RpgSkill skill) {
        PlayerData data = getData(player);

        if (data == null || skill == null
                || !isUnlocked(player, skill)) {
            return 0;
        }

        int attributeLevel = data.getAttribute(
                skill.getAttribute()
        );

        if (attributeLevel >= skill.getSecondUpgradeLevel()) {
            return 3;
        }

        if (attributeLevel >= skill.getFirstUpgradeLevel()) {
            return 2;
        }

        return 1;
    }

    public boolean equipSkill(Player player, RpgSkill skill) {
        if (skill == null || !isUnlocked(player, skill)) {
            return false;
        }

        if (isInCombat(player)) {
            return false;
        }

        List<RpgSkill> equipped = getEquippedSkills(player);

        if (equipped.contains(skill)) {
            return false;
        }

        if (equipped.size() >= MAX_EQUIPPED_SKILLS) {
            return false;
        }

        equipped.add(skill);
        return true;
    }

    public boolean unequipSkill(Player player, RpgSkill skill) {
        if (skill == null || isInCombat(player)) {
            return false;
        }

        return getEquippedSkills(player).remove(skill);
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
        Map<RpgSkill, Long> playerCooldowns =
                cooldowns.get(player.getUniqueId());

        if (playerCooldowns == null) {
            return false;
        }

        Long expiry = playerCooldowns.get(skill);

        if (expiry == null) {
            return false;
        }

        if (System.currentTimeMillis() >= expiry) {
            playerCooldowns.remove(skill);
            return false;
        }

        return true;
    }

    public long getRemainingCooldown(
            Player player,
            RpgSkill skill
    ) {
        Map<RpgSkill, Long> playerCooldowns =
                cooldowns.get(player.getUniqueId());

        if (playerCooldowns == null) {
            return 0;
        }

        Long expiry = playerCooldowns.get(skill);

        if (expiry == null) {
            return 0;
        }

        return Math.max(
                0,
                (expiry - System.currentTimeMillis() + 999) / 1000
        );
    }

    public void startCooldown(Player player, RpgSkill skill) {
        int cooldown = skill.getCooldownSeconds();

        cooldowns.computeIfAbsent(
                player.getUniqueId(),
                uuid -> new HashMap<>()
        ).put(
                skill,
                System.currentTimeMillis() + cooldown * 1000L
        );
    }

    public void recordCombat(Player player) {
        lastCombat.put(
                player.getUniqueId(),
                System.currentTimeMillis()
        );
    }

    public boolean isInCombat(Player player) {
        Long lastHit = lastCombat.get(player.getUniqueId());

        if (lastHit == null) {
            return false;
        }

        if (System.currentTimeMillis() - lastHit >= COMBAT_DURATION) {
            lastCombat.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    public void clearPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        equippedSkills.remove(uuid);
        cooldowns.remove(uuid);
        lastCombat.remove(uuid);
    }

    private PlayerData getData(Player player) {
        return plugin.getPlayerDataManager().getPlayer(
                player.getUniqueId()
        );
    }
}
