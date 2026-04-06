package xyz.whoneedspacee.ssmos.managers.disguises;

import net.minecraft.world.entity.animal.Squid;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SquidDisguise extends Disguise {

    public SquidDisguise(Player owner) {
        super(owner);
        name = "Squid";
        type = EntityType.SQUID;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Squid(net.minecraft.world.entity.EntityType.SQUID,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

}
