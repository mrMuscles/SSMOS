package xyz.whoneedspacee.ssmos.managers.disguises;

import net.minecraft.world.entity.animal.IronGolem;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class IronGolemDisguise extends Disguise {

    public IronGolemDisguise(Player owner) {
        super(owner);
        name = "Iron Golem";
        type = EntityType.IRON_GOLEM;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new IronGolem(net.minecraft.world.entity.EntityType.IRON_GOLEM,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

}
