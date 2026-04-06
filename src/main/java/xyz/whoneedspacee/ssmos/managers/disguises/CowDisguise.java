package xyz.whoneedspacee.ssmos.managers.disguises;

import net.minecraft.world.entity.animal.Cow;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CowDisguise extends Disguise {

    public CowDisguise(Player owner) {
        super(owner);
        name = "Cow";
        type = EntityType.COW;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Cow(net.minecraft.world.entity.EntityType.COW,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

}
