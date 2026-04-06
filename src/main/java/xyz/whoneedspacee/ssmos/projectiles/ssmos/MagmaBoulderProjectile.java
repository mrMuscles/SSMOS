package xyz.whoneedspacee.ssmos.projectiles.ssmos;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.whoneedspacee.ssmos.events.SmashDamageEvent;
import xyz.whoneedspacee.ssmos.managers.DamageManager;
import xyz.whoneedspacee.ssmos.projectiles.SmashProjectile;
import xyz.whoneedspacee.ssmos.utilities.Utils;
import xyz.whoneedspacee.ssmos.utilities.VelocityUtil;

import java.util.HashMap;

public class MagmaBoulderProjectile extends SmashProjectile {

    protected Material material;
    protected int max_bounces = 3;
    protected int hit_times = 0;
    protected HashMap<LivingEntity, Long> last_hit_time_ms = new HashMap<LivingEntity, Long>();

    public MagmaBoulderProjectile(Player firer, String name, Material material) {
        super(firer, name);
        this.damage = 4;
        this.hitbox_size = 0.75;
        this.knockback_mult = 1;
        this.material = material;
        this.blockDetection = false;
    }

    /** Legacy constructor kept for compatibility (block_id and block_data are ignored, NETHERRACK is used). */
    public MagmaBoulderProjectile(Player firer, String name, int block_id, byte block_data) {
        this(firer, name, Material.NETHERRACK);
    }

    @Override
    public void run() {
        super.run();
        if(projectile != null) {
            projectile.setVelocity(projectile.getVelocity().setY(Math.max(projectile.getVelocity().getY() - 0.02, -4.0)));
        }
        if(projectile != null && projectile.getVelocity().getY() < 0) {
            if(Utils.entityIsOnGround(projectile, Math.abs(projectile.getVelocity().getY()) + 0.75) && hit_times < max_bounces) {
                doExplosion();
                Vector velocity = projectile.getVelocity();
                velocity.setY(Math.abs(velocity.getY() * 0.8));
                projectile.setVelocity(velocity);
                hit_times++;
            }
        }
        // Check if the block formed
        if(projectile != null && !projectile.isValid()) {
            doExplosion();
            projectile = null;
        }
    }

    @Override
    protected Entity createProjectileEntity() {
        Location spawn_location = firer.getEyeLocation().add(firer.getLocation().getDirection());
        BlockData blockData = material.createBlockData();
        FallingBlock block = firer.getWorld().spawnFallingBlock(spawn_location, blockData);
        DamageManager.no_fast_block.put(block, 1);
        return block;
    }

    @Override
    protected void doVelocity() {
        VelocityUtil.setVelocity(projectile, firer.getLocation().getDirection(),
                1.2, false, 0.2, 0, 1, true);
    }

    @Override
    protected void doEffect() {
        firer.getWorld().playSound(projectile.getLocation(), Sound.FIRE, 1.4f, 0.8f);
        Utils.playParticle(Particle.FLAME, projectile.getLocation(),
                0.0f, 0.0f, 0.0f, 0.25f, 1, 96,
                projectile.getWorld().getPlayers());
    }

    @Override
    protected boolean onExpire() {
        return true;
    }

    @Override
    protected boolean onHitLivingEntity(LivingEntity hit) {
        last_hit_time_ms.putIfAbsent(hit, 0L);
        if(System.currentTimeMillis() - last_hit_time_ms.get(hit) < 500) {
            return false;
        }
        last_hit_time_ms.put(hit, System.currentTimeMillis());
        SmashDamageEvent smashDamageEvent = new SmashDamageEvent(hit, firer, damage * (hit_times + 1));
        smashDamageEvent.multiplyKnockback(knockback_mult + hit_times * 0.5);
        if(!Utils.entityIsOnBlock(hit)) {
            smashDamageEvent.multiplyKnockback(1.5);
        }
        smashDamageEvent.setIgnoreDamageDelay(true);
        smashDamageEvent.setReason(name);
        smashDamageEvent.callEvent();
        return false;
    }

    @Override
    protected boolean onHitBlock(Block hit) {
        return false;
    }

    @Override
    protected boolean onIdle() {
        return false;
    }

    public void doExplosion() {
        Utils.playParticle(Particle.EXPLOSION, projectile.getLocation(),
                0.0f, 0.0f, 0.0f, 0.0f, 1, 96,
                projectile.getWorld().getPlayers());
        projectile.getWorld().playSound(projectile.getLocation(), Sound.EXPLODE, 1.0f, 0.5f);
    }

}
