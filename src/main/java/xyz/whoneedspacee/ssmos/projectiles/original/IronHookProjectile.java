package xyz.whoneedspacee.ssmos.projectiles.original;
import org.bukkit.Particle;

import xyz.whoneedspacee.ssmos.events.SmashDamageEvent;
import xyz.whoneedspacee.ssmos.projectiles.SmashProjectile;
import xyz.whoneedspacee.ssmos.utilities.ServerMessageType;
import xyz.whoneedspacee.ssmos.utilities.Utils;
import xyz.whoneedspacee.ssmos.utilities.VelocityUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class IronHookProjectile extends SmashProjectile {

    public IronHookProjectile(Player firer, String name) {
        super(firer, name);
        this.damage = 4;
        this.hitbox_size = 0.6;
        this.knockback_mult = 0;
    }

    @Override
    public void launchProjectile() {
        super.launchProjectile();
        firer.getWorld().playSound(firer.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 2f, 0.8f);
    }

    @Override
    protected Entity createProjectileEntity() {
        ItemStack hook = new ItemStack(Material.TRIPWIRE_HOOK);
        return firer.getWorld().dropItem(firer.getEyeLocation().add(firer.getLocation().getDirection()), hook);
    }

    @Override
    protected void doVelocity() {
        VelocityUtil.setVelocity(projectile, firer.getLocation().getDirection(),
                1.8, false, 0, 0.2, 10, false);
    }

    @Override
    protected void doEffect() {
        firer.getWorld().playSound(projectile.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1.4f, 0.8f);
        Utils.playParticle(Particle.CRIT, projectile.getLocation(),
                0.0f, 0.0f, 0.0f, 0.0f, 1, 96,
                projectile.getWorld().getPlayers());
    }

    @Override
    protected boolean onExpire() {
        return true;
    }

    @Override
    protected boolean onHitLivingEntity(LivingEntity hit) {
        SmashDamageEvent smashDamageEvent = new SmashDamageEvent(hit, firer, damage * projectile.getVelocity().length());
        smashDamageEvent.multiplyKnockback(knockback_mult);
        smashDamageEvent.setIgnoreDamageDelay(true);
        smashDamageEvent.setReason(name);
        smashDamageEvent.callEvent();
        if(smashDamageEvent.isCancelled()) {
            return true;
        }
        // From + X = To
        // X = To - From
        Vector pull = firer.getLocation().toVector().subtract(hit.getLocation().toVector()).normalize();
        VelocityUtil.setVelocity(hit, pull, 2, false, 0, 0.8, 1.5, true);
        if(hit instanceof Player) {
            Player player = (Player) hit;
            Utils.sendAttributeMessage(ChatColor.YELLOW + firer.getName() +
                    ChatColor.GRAY + " hit you with", name, player, ServerMessageType.SKILL);
        }
        return true;
    }

    @Override
    protected boolean onHitBlock(Block hit) {
        return true;
    }

    @Override
    protected boolean onIdle() {
        return true;
    }

}
