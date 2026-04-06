package xyz.whoneedspacee.ssmos.utilities;

import xyz.whoneedspacee.ssmos.kits.original.KitTemporarySpectator;
import xyz.whoneedspacee.ssmos.managers.gamestate.GameState;
import xyz.whoneedspacee.ssmos.managers.smashteam.SmashTeam;
import xyz.whoneedspacee.ssmos.events.SmashDamageEvent;
import xyz.whoneedspacee.ssmos.managers.GameManager;
import xyz.whoneedspacee.ssmos.managers.KitManager;
import xyz.whoneedspacee.ssmos.managers.TeamManager;
import xyz.whoneedspacee.ssmos.managers.smashserver.SmashServer;
import xyz.whoneedspacee.ssmos.kits.Kit;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.HashMap;

public class DamageUtil {

    private static HashMap<LivingEntity, DamageRateTracker> damageRateTrackers = new HashMap<LivingEntity, DamageRateTracker>();

    public static void borderKill(Player player, boolean lightning) {
        SmashServer server = GameManager.getPlayerServer(player);
        Kit kit = KitManager.getPlayerKit(player);
        if(lightning && kit != null && !(kit instanceof KitTemporarySpectator)) {
            player.getWorld().strikeLightningEffect(player.getLocation());
        }
        double pre_lives = 0;
        if(server != null) {
            pre_lives = server.getLives(player);
        }
        SmashDamageEvent smashDamageEvent = new SmashDamageEvent(player, null, 1000);
        smashDamageEvent.multiplyKnockback(0);
        smashDamageEvent.setIgnoreArmor(true);
        smashDamageEvent.setIgnoreDamageDelay(true);
        smashDamageEvent.setDamageCause(DamageCause.VOID);
        smashDamageEvent.setDamagerName("Void");
        smashDamageEvent.setReason("World Border");
        smashDamageEvent.callEvent();
        // Only teleport if we died from the damage event
        if(pre_lives > 0) {
            if(pre_lives == server.getLives(player)) {
                return;
            }
        }
        player.teleport(player.getWorld().getSpawnLocation());
    }

    public static boolean canDamage(LivingEntity damagee, LivingEntity damager) {
        if(damagee.equals(damager)) {
            return true;
        }
        if (damagee instanceof Player) {
            Player player = (Player) damagee;
            if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
                return false;
            }
            Kit kit = KitManager.getPlayerKit(player);
            if (kit != null && kit.isInvincible()) {
                return false;
            }
            if(damager instanceof Player) {
                Player player_damager = (Player) damager;
                SmashTeam damagee_team = TeamManager.getPlayerTeam(player);
                SmashTeam damager_team = TeamManager.getPlayerTeam(player_damager);
                if(damagee_team != null && damagee_team.equals(damager_team)) {
                    return false;
                }
            }
            SmashServer server = GameManager.getPlayerServer(player);
            if(server != null) {
                return (server.getState() == GameState.GAME_PLAYING);
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean isIntangible(LivingEntity check) {
        if(!(check instanceof Player)) {
            return false;
        }
        Player player = (Player) check;
        Kit kit = KitManager.getPlayerKit(player);
        if(kit == null) {
            return false;
        }
        return kit.isIntangible();
    }

    public static void playDamageSound(LivingEntity damagee, EntityType type) {
        final float volume = 1.5f + (float) (0.5f * Math.random());
        final float pitch = 0.8f + (float) (0.4f * Math.random());

        // gholemFix: we can do a davito big brain call and use NMS to use a sound string, so we can get guardian sounds (lol bukkit api sucks)
        if (type == EntityType.GUARDIAN) {
            damagee.getWorld().playSound(damagee.getLocation(), Sound.ENTITY_GUARDIAN_HURT, volume, pitch);
            return;
        }

        // gholemFix: ik exactly where space copied the previous code from, so u can thank me later for making it better here
        Sound sound;
        switch (type) {
            default:
                sound = Sound.ENTITY_PLAYER_HURT;
                break;
            case BAT:
                sound = Sound.ENTITY_BAT_HURT;
                break;
            case BLAZE:
                sound = Sound.ENTITY_BLAZE_HURT;
                break;
            case CAVE_SPIDER:
            case SPIDER:
                sound = Sound.ENTITY_SPIDER_AMBIENT;
                break;
            case CHICKEN:
                sound = Sound.ENTITY_CHICKEN_HURT;
                break;
            case COW:
            case MOOSHROOM:
                sound = Sound.ENTITY_COW_HURT;
                break;
            case CREEPER:
                sound = Sound.ENTITY_CREEPER_PRIMED;
                break;
            case ENDER_DRAGON:
                sound = Sound.ENTITY_ENDER_DRAGON_HURT;
                break;
            case ENDERMAN:
                sound = Sound.ENTITY_ENDERMAN_HURT;
                break;
            case GHAST:
                sound = Sound.ENTITY_GHAST_SCREAM;
                break;
            case GIANT:
            case ZOMBIE:
                sound = Sound.ENTITY_ZOMBIE_HURT;
                break;
            case HORSE: // lmao virgin bukkit entity type doesn't differentiate between horse variants
                sound = Sound.ENTITY_SKELETON_HORSE_HURT;
                break;
            case IRON_GOLEM:
                sound = Sound.ENTITY_IRON_GOLEM_HURT;
                break;
            case MAGMA_CUBE:
                sound = Sound.ENTITY_MAGMA_CUBE_HURT;
                break;
            case OCELOT:
                sound = Sound.ENTITY_CAT_HURT;
                break;
            case PIG:
                sound = Sound.ENTITY_PIG_AMBIENT;
                break;
            case ZOMBIFIED_PIGLIN:
                sound = Sound.ENTITY_ZOMBIFIED_PIGLIN_HURT;
                break;
            case SHEEP:
                sound = Sound.ENTITY_SHEEP_AMBIENT;
                break;
            case SILVERFISH:
                sound = Sound.ENTITY_SILVERFISH_HURT;
                break;
            case SKELETON:
                sound = Sound.ENTITY_SKELETON_HURT;
                break;
            case SLIME:
                sound = Sound.ENTITY_SLIME_ATTACK;
                break;
            case SNOW_GOLEM:
                sound = Sound.BLOCK_SNOW_STEP;
                break;
            case VILLAGER:
            case WITCH:
                sound = Sound.ENTITY_VILLAGER_HURT;
                break;
            case WITHER:
                sound = Sound.ENTITY_WITHER_HURT;
                break;
            case WOLF:
                sound = Sound.ENTITY_WOLF_HURT;
                break;
        }
        damagee.getWorld().playSound(damagee.getLocation(), sound, volume, pitch);
    }

    public static DamageRateTracker getDamageRateTracker(LivingEntity living) {
        DamageRateTracker tracker = damageRateTrackers.get(living);
        if(tracker == null) {
            tracker = new DamageRateTracker(living);
            damageRateTrackers.put(living, tracker);
        }
        return tracker;
    }

    public static class DamageRateTracker {

        private LivingEntity living;
        private HashMap<LivingEntity, Long> lastHurt = new HashMap<>();
        private HashMap<LivingEntity, Long> lastHurtBy = new HashMap<>();
        private long lastHurtByWorld = 0;

        public DamageRateTracker(LivingEntity living) {
            this.living = living;
        }

        public boolean canBeHurtBy(LivingEntity damager) {
            if (damager == null) {
                if (System.currentTimeMillis() - lastHurtByWorld > 250) {
                    lastHurtByWorld = System.currentTimeMillis();
                    return true;
                }
                return false;
            }
            if (!lastHurtBy.containsKey(damager)) {
                lastHurtBy.put(damager, System.currentTimeMillis());
                return true;
            }
            if (System.currentTimeMillis() - lastHurtBy.get(damager) > 400) {
                lastHurtBy.put(damager, System.currentTimeMillis());
                return true;
            }
            return false;
        }

        public boolean canHurt(LivingEntity damagee) {
            if (damagee == null) {
                return true;
            }
            if (!lastHurt.containsKey(damagee)) {
                lastHurt.put(damagee, System.currentTimeMillis());
                return true;
            }
            if (System.currentTimeMillis() - lastHurt.get(damagee) > 400) {
                lastHurt.put(damagee, System.currentTimeMillis());
                return true;
            }
            return false;
        }

    }

}
