package xyz.whoneedspacee.ssmos.managers.disguises;

import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class WitherDisguise extends Disguise {

    public WitherDisguise(Player owner) {
        super(owner);
        name = "Wither";
        type = EntityType.WITHER;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new WitherBoss(net.minecraft.world.entity.EntityType.WITHER,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

}
