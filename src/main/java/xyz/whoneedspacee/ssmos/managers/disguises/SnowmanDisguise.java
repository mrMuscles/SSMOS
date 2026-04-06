package xyz.whoneedspacee.ssmos.managers.disguises;

import net.minecraft.world.entity.animal.SnowGolem;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SnowmanDisguise extends Disguise {

    public SnowmanDisguise(Player owner) {
        super(owner);
        name = "Snowman";
        type = EntityType.SNOW_GOLEM;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new SnowGolem(net.minecraft.world.entity.EntityType.SNOW_GOLEM,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

}
