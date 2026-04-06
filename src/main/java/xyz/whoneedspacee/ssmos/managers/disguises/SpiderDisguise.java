package xyz.whoneedspacee.ssmos.managers.disguises;

import net.minecraft.world.entity.monster.Spider;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SpiderDisguise extends Disguise {

    public SpiderDisguise(Player owner) {
        super(owner);
        name = "Spider";
        type = EntityType.SPIDER;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Spider(net.minecraft.world.entity.EntityType.SPIDER,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

}
