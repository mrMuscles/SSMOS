package xyz.whoneedspacee.ssmos.managers.disguises;

import net.minecraft.world.entity.animal.Chicken;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ChickenDisguise extends Disguise {

    public ChickenDisguise(Player owner) {
        super(owner);
        name = "Chicken";
        type = EntityType.CHICKEN;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Chicken(net.minecraft.world.entity.EntityType.CHICKEN,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

}
