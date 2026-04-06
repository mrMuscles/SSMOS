package xyz.whoneedspacee.ssmos.managers.disguises;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftEntity;
import xyz.whoneedspacee.ssmos.Main;
import xyz.whoneedspacee.ssmos.managers.gamestate.GameState;
import xyz.whoneedspacee.ssmos.commands.CommandShowHealth;
import xyz.whoneedspacee.ssmos.managers.GameManager;
import xyz.whoneedspacee.ssmos.managers.smashscoreboard.SmashScoreboard;
import xyz.whoneedspacee.ssmos.managers.smashserver.SmashServer;
import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.animal.Squid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class Disguise {

    @SuppressWarnings("unchecked")
    protected static final net.minecraft.network.syncher.EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID;
    @SuppressWarnings("unchecked")
    private static final net.minecraft.network.syncher.EntityDataAccessor<Optional<net.minecraft.network.chat.Component>> DATA_CUSTOM_NAME;
    @SuppressWarnings("unchecked")
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE;

    static {
        try {
            Field f1 = net.minecraft.world.entity.Entity.class.getDeclaredField("DATA_SHARED_FLAGS_ID");
            f1.setAccessible(true);
            DATA_SHARED_FLAGS_ID = (net.minecraft.network.syncher.EntityDataAccessor<Byte>) f1.get(null);

            Field f2 = net.minecraft.world.entity.Entity.class.getDeclaredField("DATA_CUSTOM_NAME");
            f2.setAccessible(true);
            DATA_CUSTOM_NAME = (net.minecraft.network.syncher.EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) f2.get(null);

            Field f3 = net.minecraft.world.entity.Entity.class.getDeclaredField("DATA_CUSTOM_NAME_VISIBLE");
            f3.setAccessible(true);
            DATA_CUSTOM_NAME_VISIBLE = (net.minecraft.network.syncher.EntityDataAccessor<Boolean>) f3.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access Entity data fields", e);
        }
    }

    protected String name;
    protected EntityType type;
    protected Player owner;
    protected net.minecraft.world.entity.LivingEntity living;
    protected ArmorStand armorstand;
    protected Squid squid;
    protected boolean showAttackAnimation = true;
    protected org.bukkit.entity.Entity target = null;
    protected List<Player> viewers = new ArrayList<Player>();
    protected HashMap<Player, Double> last_viewer_distance = new HashMap<Player, Double>();

    public Disguise(Player owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public EntityType getType() {
        return type;
    }

    public Player getOwner() {
        return owner;
    }

    public boolean isViewer(Player player) {
        return viewers.contains(player);
    }

    public net.minecraft.world.entity.LivingEntity getLiving() {
        return living;
    }

    public ArmorStand getArmorStand() {
        return armorstand;
    }

    public Squid getSquid() {
        return squid;
    }

    public void spawnLiving() {
        if (living != null) {
            deleteLiving();
        }
        living = newLiving();
        armorstand = new ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND,
                ((CraftWorld) owner.getWorld()).getHandle());
        // Had no idea this existed, but seems like this is what other servers must be doing
        ((org.bukkit.entity.ArmorStand) armorstand.getBukkitEntity()).setMarker(true);
        squid = new Squid(net.minecraft.world.entity.EntityType.SQUID,
                ((CraftWorld) owner.getWorld()).getHandle());
        for (Player player : owner.getWorld().getPlayers()) {
            if (player.equals(owner)) {
                continue;
            }
            showDisguise(player);
        }
    }

    public void reloadDisguise(Player player) {
        hideDisguise(player);
        showDisguise(player);
    }

    public void reloadLiving(Player player) {
        // Living Destroy (if player sees them already)
        ClientboundRemoveEntitiesPacket destroy_packet = new ClientboundRemoveEntitiesPacket(living.getId());
        Utils.sendPacket(player, destroy_packet);
        // Set exact head rotation before entity is spawned since moveTo does not do that
        // The body of a mob is rotated on the client so this will make the body spawn already rotated
        living.setYHeadRot(owner.getLocation().getYaw());
        // Spawn living
        ClientboundAddEntityPacket living_packet = new ClientboundAddEntityPacket(
                living.getId(), living.getUUID(),
                living.getX(), living.getY(), living.getZ(),
                living.getXRot(), living.getYRot(),
                living.getType(), 0,
                living.getDeltaMovement(), living.getYHeadRot());
        Utils.sendPacket(player, living_packet);
    }

    public void showDisguise(Player player) {
        // Do not show disguise to self
        if (player.equals(owner)) {
            return;
        }
        // Can't show disguise if it doesn't exist
        if(living == null) {
            return;
        }
        viewers.add(player);
        // Armor Stand Destroy (if player sees them already)
        ClientboundRemoveEntitiesPacket destroy_packet = new ClientboundRemoveEntitiesPacket(armorstand.getId());
        Utils.sendPacket(player, destroy_packet);
        // Squid Destroy (if player sees them already)
        destroy_packet = new ClientboundRemoveEntitiesPacket(squid.getId());
        Utils.sendPacket(player, destroy_packet);
        // Living Destroy (if player sees them already)
        destroy_packet = new ClientboundRemoveEntitiesPacket(living.getId());
        Utils.sendPacket(player, destroy_packet);
        // Living Spawn
        living.moveTo(owner.getLocation().getX(), owner.getLocation().getY(), owner.getLocation().getZ(),
                    owner.getLocation().getYaw(), owner.getLocation().getPitch());
        // Set exact head rotation before entity is spawned since moveTo does not do that
        // The body of a mob is rotated on the client so this will make the body spawn already rotated
        living.setYHeadRot(owner.getLocation().getYaw());
        ClientboundAddEntityPacket living_packet = new ClientboundAddEntityPacket(
                living.getId(), living.getUUID(),
                living.getX(), living.getY(), living.getZ(),
                living.getXRot(), living.getYRot(),
                living.getType(), 0,
                living.getDeltaMovement(), living.getYHeadRot());
        Utils.sendPacket(player, living_packet);
        // Squid Spawn
        squid.moveTo(owner.getLocation().getX(),
                living.getY() + living.getBbHeight() + squid.getBbHeight(),
                owner.getLocation().getZ(),
                owner.getLocation().getYaw(), owner.getLocation().getPitch());
        ClientboundAddEntityPacket squid_packet = new ClientboundAddEntityPacket(
                squid.getId(), squid.getUUID(),
                squid.getX(), squid.getY(), squid.getZ(),
                squid.getXRot(), squid.getYRot(),
                squid.getType(), 0,
                squid.getDeltaMovement(), squid.getYHeadRot());
        Utils.sendPacket(player, squid_packet);
        // Armor Stand Spawn
        armorstand.moveTo(owner.getLocation().getX(),
                squid.getY() + squid.getBbHeight() + armorstand.getBbHeight(),
                owner.getLocation().getZ(),
                owner.getLocation().getYaw(), owner.getLocation().getPitch());
        ClientboundAddEntityPacket armorstand_packet = new ClientboundAddEntityPacket(
                armorstand.getId(), armorstand.getUUID(),
                armorstand.getX(), armorstand.getY(), armorstand.getZ(),
                armorstand.getXRot(), armorstand.getYRot(),
                armorstand.getType(), 0,
                armorstand.getDeltaMovement(), armorstand.getYHeadRot());
        Utils.sendPacket(player, armorstand_packet);
        // Invisibility for Armor Stand
        List<SynchedEntityData.DataValue<?>> armorstand_invis_values = new ArrayList<>();
        armorstand_invis_values.add(SynchedEntityData.DataValue.create(
                DATA_SHARED_FLAGS_ID, (byte) 0x20));
        ClientboundSetEntityDataPacket invisiblity_packet = new ClientboundSetEntityDataPacket(
                armorstand.getId(), armorstand_invis_values);
        Utils.sendPacket(player, invisiblity_packet);
        // Invisibility for Squid
        List<SynchedEntityData.DataValue<?>> squid_invis_values = new ArrayList<>();
        squid_invis_values.add(SynchedEntityData.DataValue.create(
                DATA_SHARED_FLAGS_ID, (byte) 0x20));
        invisiblity_packet = new ClientboundSetEntityDataPacket(squid.getId(), squid_invis_values);
        Utils.sendPacket(player, invisiblity_packet);
        update();
    }

    public void hideDisguise(Player player) {
        ClientboundRemoveEntitiesPacket destroy_living_packet = new ClientboundRemoveEntitiesPacket(living.getId());
        ClientboundRemoveEntitiesPacket destroy_armorstand_packet = new ClientboundRemoveEntitiesPacket(armorstand.getId());
        ClientboundRemoveEntitiesPacket destroy_squid_packet = new ClientboundRemoveEntitiesPacket(squid.getId());
        Utils.sendPacket(player, destroy_living_packet);
        Utils.sendPacket(player, destroy_armorstand_packet);
        Utils.sendPacket(player, destroy_squid_packet);
        showOwner();
        viewers.remove(player);
    }

    public void update() {
        if(living == null) {
            return;
        }
        Location location = owner.getLocation();
        // Hide the disguised player from other players
        // Don't use HidePlayer here, it stops the melees
        // But also stops the server from recognizing projectile hits like arrows
        hideOwner();
        // Hide the mob from the disguised player
        ClientboundRemoveEntitiesPacket disguise_destroy_packet = new ClientboundRemoveEntitiesPacket(living.getId());
        Utils.sendPacket(owner, disguise_destroy_packet);
        // Don't teleport to spectator player if the mob is dead
        if (!living.isAlive()) {
            return;
        }
        for(Player player : viewers) {
            if(!player.getWorld().equals(living.getBukkitEntity().getWorld())) {
                continue;
            }
            Location player_xz = player.getLocation();
            player_xz.setY(0);
            Location living_xz = living.getBukkitEntity().getLocation();
            living_xz.setY(0);
            double distance = player_xz.distance(living_xz);
            double distance_check = 32;
            last_viewer_distance.putIfAbsent(player, distance);
            if(last_viewer_distance.get(player) > distance_check && distance <= distance_check) {
                reloadLiving(player);
            }
            last_viewer_distance.put(player, distance);
        }
        // Update nametag
        for(Player viewer : viewers) {
            String custom_name = "";
            SmashServer server = GameManager.getPlayerServer(viewer);
            if(CommandShowHealth.show_health || (server != null && server.getLives(viewer) <= 0)) {
                if(server != null && server.getState() >= GameState.GAME_STARTING) {
                    custom_name += ChatColor.RED + "\u2764 " + ChatColor.WHITE + String.format("%.2f", owner.getHealth()) + " ";
                }
            }
            custom_name += ChatColor.RESET + SmashScoreboard.getPlayerColor(owner, false) + owner.getName();
            List<SynchedEntityData.DataValue<?>> nametag_values = new ArrayList<>();
            nametag_values.add(SynchedEntityData.DataValue.create(
                    DATA_CUSTOM_NAME,
                    Optional.of(Component.literal(custom_name))));
            nametag_values.add(SynchedEntityData.DataValue.create(
                    DATA_CUSTOM_NAME_VISIBLE, true));
            ClientboundSetEntityDataPacket data_packet = new ClientboundSetEntityDataPacket(armorstand.getId(), nametag_values);
            Utils.sendPacket(viewer, data_packet);
        }
        living.setOnGround(Utils.entityIsOnGround(owner));
        if(target != null) {
            Vector direction = target.getLocation().toVector().subtract(location.toVector());
            location.setDirection(direction);
        }
        living.moveTo(location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
        ClientboundTeleportEntityPacket teleport_packet = new ClientboundTeleportEntityPacket(
                living.getId(), PositionMoveRotation.of(living), Set.of(), living.onGround());
        Utils.sendPacketToAllBut(owner, teleport_packet);
        ClientboundRotateHeadPacket head_packet = new ClientboundRotateHeadPacket(living,
                (byte) ((location.getYaw() * 256.0F) / 360.0F));
        Utils.sendPacketToAllBut(owner, head_packet);
        // From living.mount source code all the way to Entity.class mount
        // In the Entity.class al() method appears to be where it sets the passengers position
        squid.moveTo(location.getX(), living.getY() + living.getBbHeight() + squid.getBbHeight(), location.getZ(),
                owner.getLocation().getYaw(), owner.getLocation().getPitch());
        teleport_packet = new ClientboundTeleportEntityPacket(
                squid.getId(), PositionMoveRotation.of(squid), Set.of(), squid.onGround());
        Utils.sendPacketToAllBut(owner, teleport_packet);
        armorstand.moveTo(location.getX(), squid.getY() + squid.getBbHeight() + armorstand.getBbHeight(), location.getZ(),
                owner.getLocation().getYaw(), owner.getLocation().getPitch());
        teleport_packet = new ClientboundTeleportEntityPacket(
                armorstand.getId(), PositionMoveRotation.of(armorstand), Set.of(), armorstand.onGround());
        Utils.sendPacketToAllBut(owner, teleport_packet);
        // Show player data
        byte player_data = 0;
        if(owner.getFireTicks() > 0) {
            player_data = (byte) (player_data | 0x01);
        }
        if(owner.isSneaking()) {
            player_data = (byte) (player_data | 0x02);
        }
        if(owner.isSprinting()) {
            player_data = (byte) (player_data | 0x08);
        }
        List<SynchedEntityData.DataValue<?>> player_data_values = new ArrayList<>();
        player_data_values.add(SynchedEntityData.DataValue.create(
                DATA_SHARED_FLAGS_ID, player_data));
        ClientboundSetEntityDataPacket data_packet = new ClientboundSetEntityDataPacket(living.getId(), player_data_values);
        Utils.sendPacketToAllBut(owner, data_packet);
        // Keep the entity loaded by sending status 0 packets repeatedly
        ClientboundEntityEventPacket status_packet = new ClientboundEntityEventPacket(living, (byte) 0);
        Utils.sendPacketToAllBut(owner, status_packet);
    }

    public void deleteLiving() {
        if (living == null) {
            return;
        }
        ClientboundRemoveEntitiesPacket destroy_living_packet = new ClientboundRemoveEntitiesPacket(living.getId());
        ClientboundRemoveEntitiesPacket destroy_armorstand_packet = new ClientboundRemoveEntitiesPacket(armorstand.getId());
        ClientboundRemoveEntitiesPacket destroy_squid_packet = new ClientboundRemoveEntitiesPacket(squid.getId());
        for (Player player : Bukkit.getOnlinePlayers()) {
            Utils.sendPacket(player, destroy_living_packet);
            Utils.sendPacket(player, destroy_armorstand_packet);
            Utils.sendPacket(player, destroy_squid_packet);
        }
        showOwner();
        living = null;
        armorstand = null;
        squid = null;
    }

    public void hideOwner() {
        ClientboundRemoveEntitiesPacket destroy_packet = new ClientboundRemoveEntitiesPacket(owner.getEntityId());
        for(Player player : viewers) {
            Utils.sendPacket(player, destroy_packet);
        }
    }

    public void showOwner() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            // Hide and then re-show so bukkit will recognize the player as having been hidden
            player.hidePlayer(Main.getInstance(), owner);
            player.showPlayer(Main.getInstance(), owner);
        }
    }

    protected abstract net.minecraft.world.entity.LivingEntity newLiving();

    public boolean getShowAttackAnimation() {
        return showAttackAnimation;
    }

    public Sound getDamageSound() {
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
        return sound;
    }

    public float getVolume() {
        return 1.0f;
    }

    public float getPitch() {
        return ((float) ((Math.random() - Math.random()) * 0.2f + 1.0f));
    }

    public void playDamageSound() {
        owner.getWorld().playSound(owner.getLocation(), getDamageSound(), getVolume(), getPitch());
    }

    // Leashes the living mob to the specified entity
    public void setAsLeashHolder(LivingEntity livingEntity) {
        net.minecraft.world.entity.Entity nms_vehicle = ((CraftEntity) livingEntity).getHandle();
        ClientboundSetEntityLinkPacket attach_living_packet = new ClientboundSetEntityLinkPacket(living, nms_vehicle);
        Utils.sendPacketToAllBut(owner, attach_living_packet);
    }

    // Unleashes the living mob
    public void removeLeashHolder(LivingEntity livingEntity) {
        ClientboundSetEntityLinkPacket detach_living_packet = new ClientboundSetEntityLinkPacket(living, null);
        Utils.sendPacketToAllBut(owner, detach_living_packet);
    }

}
