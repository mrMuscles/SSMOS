package xyz.whoneedspacee.ssmos.managers.disguises;

import net.minecraft.world.entity.animal.Wolf;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class WolfDisguise extends Disguise {

    public WolfDisguise(Player owner) {
        super(owner);
        name = "Wolf";
        type = EntityType.WOLF;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Wolf(net.minecraft.world.entity.EntityType.WOLF,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

}
