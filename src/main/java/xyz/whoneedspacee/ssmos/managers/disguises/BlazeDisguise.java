package xyz.whoneedspacee.ssmos.managers.disguises;

import net.minecraft.world.entity.monster.Blaze;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class BlazeDisguise extends Disguise {

    public BlazeDisguise(Player owner) {
        super(owner);
        name = "Blaze";
        type = EntityType.BLAZE;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Blaze(net.minecraft.world.entity.EntityType.BLAZE,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

}
