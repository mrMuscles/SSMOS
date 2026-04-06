package xyz.whoneedspacee.ssmos.managers.disguises;

import net.minecraft.world.entity.animal.Pig;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class PigDisguise extends Disguise {

    public PigDisguise(Player owner) {
        super(owner);
        name = "Pig";
        type = EntityType.PIG;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Pig(net.minecraft.world.entity.EntityType.PIG,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

}
