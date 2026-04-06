package xyz.whoneedspacee.ssmos.projectiles;

import org.bukkit.Location;
import xyz.whoneedspacee.ssmos.commands.CommandShowHitboxes;
import xyz.whoneedspacee.ssmos.managers.KitManager;
import xyz.whoneedspacee.ssmos.kits.Kit;
import xyz.whoneedspacee.ssmos.Main;
import xyz.whoneedspacee.ssmos.utilities.BlocksUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.core.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.whoneedspacee.ssmos.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public abstract class SmashProjectile extends BukkitRunnable implements Listener {

    protected static JavaPlugin plugin = Main.getInstance();
    protected Player firer;
    protected String name;
    protected Entity projectile;
    protected double damage;
    protected double hitbox_size;
    protected double knockback_mult;
    protected long expiration_ticks = 300;
    protected boolean running = false;
    protected boolean entityDetection = true;
    protected boolean blockDetection = true;
    protected boolean idleDetection = true;

    public SmashProjectile(Player firer, String name) {
        this.firer = firer;
        this.name = name;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void launchProjectile() {
        setProjectileEntity(createProjectileEntity());
        this.doVelocity();
        this.runTaskTimer(plugin, 0L, 0L);
    }

    @Override
    public void run() {
        running = true;
        if (projectile == null || !projectile.isValid() || !projectile.getWorld().equals(firer.getWorld())) {
            destroy();
            return;
        }
        if (projectile.getTicksLived() > getExpirationTicks()) {
            if (onExpire()) {
                destroy();
                return;
            }
        }
        // Check if we hit an entity first
        if(entityDetection) {
            LivingEntity target = checkClosestTarget();
            if (target != null) {
                if (onHitLivingEntity(target)) {
                    playHitSound();
                    destroy();
                    return;
                }
            }
        }
        // Check if we hit a block next
        if(blockDetection) {
            Block block = checkHitBlock();
            if (block != null) {
                if (onHitBlock(block)) {
                    destroy();
                    return;
                }
            }
        }
        // Check if we're idle next
        if(idleDetection) {
            if (checkIdle()) {
                if (onIdle()) {
                    destroy();
                    return;
                }
            }
        }
        doEffect();
    }

    @Override
    public synchronized void cancel() {
        running = false;
        super.cancel();
    }

    public void destroy() {
        if (projectile != null) {
            projectile.remove();
        }
        if(running) {
            this.cancel();
        }
    }

    protected boolean canHitEntity(Entity entity) {
        return true;
    }

    protected void playHitSound() {
        firer.playSound(firer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.25f);
    }

    protected LivingEntity checkClosestTarget() {
        // Realistically the fastest both a player and entity will move is about
        // 80 blocks per second or 4 blocks per tick, (hue fast blocking with autoclicker got 3.5)
        // If we do 15 iterations this should be more than enough for most projectiles leeway
        // If you want to optimize this probably start with checking the magnitude of velocity
        double max_realistic_velocity = 4;
        int max_iterations = 15;
        net.minecraft.world.entity.Entity entity = ((CraftEntity) projectile).getHandle();
        Vec3 entityMotion = entity.getDeltaMovement();
        // Get possible projectiles that could be hit on this tick
        List<LivingEntity> possible = new ArrayList<LivingEntity>();
        for (Entity check : projectile.getWorld().getNearbyEntities(projectile.getLocation(),
                Math.abs(entityMotion.x) + hitbox_size + max_realistic_velocity,
                Math.abs(entityMotion.y) + hitbox_size + max_realistic_velocity,
                Math.abs(entityMotion.z) + hitbox_size + max_realistic_velocity)) {
            if (!(check instanceof LivingEntity)) {
                continue;
            }
            if (check.equals(firer) || check.equals(projectile)) {
                continue;
            }
            if(!canHitEntity(check)) {
                continue;
            }
            if(check instanceof Player) {
                Kit kit = KitManager.getPlayerKit((Player) check);
                if(kit != null && kit.isIntangible()) {
                    continue;
                }
            }
            possible.add((LivingEntity) check);
        }
        for (double i = 0; i < max_iterations; i++) {
            double percent = i / (max_iterations - 1);
            // Linearly interpolate the player and entity hitbox and see if they overlap
            for (LivingEntity check : possible) {
                net.minecraft.world.entity.LivingEntity living = (net.minecraft.world.entity.LivingEntity) ((CraftEntity) check).getHandle();
                AABB bb = living.getBoundingBox();
                Vec3 livingMotion = living.getDeltaMovement();
                double l_x = livingMotion.x * percent;
                double l_y = livingMotion.y * percent;
                double l_z = livingMotion.z * percent;
                AABB livingBB = new AABB(bb.minX + l_x, bb.minY + l_y, bb.minZ + l_z,
                        bb.maxX + l_x, bb.maxY + l_y, bb.maxZ + l_z);
                double p_x = entity.getX() + entityMotion.x * percent;
                double p_y = entity.getY() + entityMotion.y * percent;
                double p_z = entity.getZ() + entityMotion.z * percent;
                AABB projectileBB = new AABB(p_x, p_y, p_z, p_x, p_y, p_z);
                // Grow by hitbox size given
                // Falling Blocks are grown by 0.49 default (0.98 size)
                // Item Entities are grown by 0.125 default (0.25 size)
                projectileBB = projectileBB.inflate(hitbox_size, hitbox_size, hitbox_size);
                // Attempt at visually displaying the hitbox path of the projectile
                if(CommandShowHitboxes.show_hitboxes) {
                    for (double x_iterate : new double[]{projectileBB.minX, projectileBB.maxX}) {
                        for (double y_iterate : new double[]{projectileBB.minY, projectileBB.maxY}) {
                            for (double z_iterate : new double[]{projectileBB.minZ, projectileBB.maxZ}) {
                                Location vertex_loc = new Location(projectile.getWorld(), x_iterate, y_iterate, z_iterate);
                                Utils.playParticle(Particle.FIREWORK, vertex_loc, 0, 0, 0, 0, 1, 96, projectile.getWorld().getPlayers());
                            }
                        }
                    }
                }
                if (projectileBB.intersects(livingBB)) {
                    return check;
                }
            }
        }
        return null;
    }

    // This modifies projectile motion and location, this can cause
    // Bugs with projectiles that do not delete themselves
    protected Block checkHitBlock() {
        net.minecraft.world.entity.Entity entity = ((CraftEntity) projectile).getHandle();
        Vec3 deltaMovement = entity.getDeltaMovement();
        // Do a raytrace to see what our real position is going to be
        Vec3 vecOld = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        Vec3 vecNew = new Vec3(entity.getX() + deltaMovement.x, entity.getY() + deltaMovement.y, entity.getZ() + deltaMovement.z);
        ClipContext clipContext = new ClipContext(vecOld, vecNew, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        BlockHitResult hitResult = entity.level().clip(clipContext);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return null;
        }
        BlockPos blockPos = hitResult.getBlockPos();
        Block block = projectile.getWorld().getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (block.isLiquid() || !block.getType().isSolid()) {
            return null;
        }
        // Set our motion to stop on the block we are hitting
        Vec3 hitLocation = hitResult.getLocation();
        double newMotX = hitLocation.x - entity.getX();
        double newMotY = hitLocation.y - entity.getY();
        double newMotZ = hitLocation.z - entity.getZ();
        entity.setDeltaMovement(newMotX, newMotY, newMotZ);
        // Get the magnitude of the motion vector
        float f2 = Mth.sqrt((float) (newMotX * newMotX + newMotY * newMotY + newMotZ * newMotZ));
        entity.setPos(entity.getX() - newMotX / f2 * 0.0500000007450581D,
                entity.getY() - newMotY / f2 * 0.0500000007450581D,
                entity.getZ() - newMotZ / f2 * 0.0500000007450581D);
        return block;
    }

    protected boolean checkIdle() {
        if (projectile.isDead() || !projectile.isValid()) {
            return true;
        }
        Block check_block = projectile.getLocation().getBlock().getRelative(BlockFace.DOWN);
        if (projectile.getVelocity().length() < 0.2 && (projectile.isOnGround() || !BlocksUtil.isAirOrFoliage(check_block))) {
            return true;
        }
        return false;
    }

    public void setProjectileEntity(Entity projectile) {
        if (projectile == null) {
            return;
        }
        this.projectile = projectile;
        if (projectile instanceof Item) {
            Item item = (Item) projectile;
            item.setPickupDelay(1000000);
        }
    }

    public Entity getProjectileEntity(){
        return projectile;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getDamage() {
        return damage;
    }

    protected abstract Entity createProjectileEntity();

    // Called once when the projectile is fired
    protected abstract void doVelocity();

    // Visual effects that apply every tick
    protected abstract void doEffect();

    // Returns true to call destroy after
    protected abstract boolean onExpire();

    // Returns true to call destroy after
    protected abstract boolean onHitLivingEntity(LivingEntity hit);

    // Returns true to call destroy after
    protected abstract boolean onHitBlock(Block hit);

    // Returns true to call destroy after
    protected abstract boolean onIdle();

    public long getExpirationTicks() {
        return expiration_ticks;
    }

    public String getName() {
        return name;
    }

}
