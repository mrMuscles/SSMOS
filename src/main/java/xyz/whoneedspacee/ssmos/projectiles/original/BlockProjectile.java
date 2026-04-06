package xyz.whoneedspacee.ssmos.projectiles.original;

import xyz.whoneedspacee.ssmos.events.SmashDamageEvent;
import xyz.whoneedspacee.ssmos.projectiles.SmashProjectile;
import xyz.whoneedspacee.ssmos.utilities.VelocityUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BlockProjectile extends SmashProjectile {

    protected double max_damage;
    protected long charge;
    protected double mult;
    protected BlockData blockData;
    protected FallingBlock hit_block_effect;

    public BlockProjectile(Player firer, String name, long charge, double mult, BlockData blockData) {
        super(firer, name);
        this.damage = 8;
        this.max_damage = 9;
        this.hitbox_size = 0.75;
        this.knockback_mult = 2.5;
        this.charge = charge;
        this.mult = mult;
        this.blockData = blockData;
    }

    /** Legacy constructor kept for compatibility; block_id/block_data are ignored, uses STONE. */
    public BlockProjectile(Player firer, String name, long charge, double mult, int block_id, byte block_data) {
        this(firer, name, charge, mult, Material.STONE.createBlockData());
    }

    @Override
    protected Entity createProjectileEntity() {
        Location spawn_location = firer.getEyeLocation().add(firer.getLocation().getDirection());
        return firer.getWorld().spawnFallingBlock(spawn_location, blockData);
    }

    @Override
    protected void doVelocity() {
        VelocityUtil.setVelocity(projectile, firer.getLocation().getDirection(),
                mult, false, 0.2, 0, 1, true);
    }

    @Override
    protected void doEffect() {
        return;
    }

    @Override
    protected boolean onExpire() {
        return true;
    }

    @Override
    protected boolean onHitLivingEntity(LivingEntity hit) {
        double block_damage = Math.min(max_damage, projectile.getVelocity().length() * damage);
        SmashDamageEvent smashDamageEvent = new SmashDamageEvent(hit, firer, block_damage);
        smashDamageEvent.multiplyKnockback(knockback_mult);
        smashDamageEvent.setIgnoreDamageDelay(true);
        smashDamageEvent.setReason(name);
        smashDamageEvent.callEvent();
        if (projectile instanceof FallingBlock) {
            FallingBlock thrown = (FallingBlock) projectile;
            hit_block_effect = projectile.getWorld().spawnFallingBlock(projectile.getLocation(), thrown.getBlockData());
        }
        return true;
    }

    @Override
    protected boolean onHitBlock(Block hit) {
        return false;
    }

    @Override
    protected boolean onIdle() {
        return false;
    }

    // Prevents falling blocks from forming
    // This is mainly for the falling block spawned
    // As an effect after a livingentity is hit
    @EventHandler
    public void blockForm(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof FallingBlock)) {
            return;
        }
        FallingBlock falling = (FallingBlock) e.getEntity();
        if(e.getEntity().equals(projectile)) {
            falling.getWorld().playEffect(e.getBlock().getLocation(), Effect.STEP_SOUND, falling.getBlockData().getMaterial());
            cancel();
        }
        if(e.getEntity().equals(hit_block_effect)) {
            falling.getWorld().playEffect(e.getBlock().getLocation(), Effect.STEP_SOUND, falling.getBlockData().getMaterial());
            falling.remove();
        }
        e.setCancelled(true);
    }

}
