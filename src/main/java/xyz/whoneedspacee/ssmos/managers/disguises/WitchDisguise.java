package xyz.whoneedspacee.ssmos.managers.disguises;

import net.minecraft.world.entity.monster.Witch;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class WitchDisguise extends Disguise {

    public WitchDisguise(Player owner) {
        super(owner);
        name = "Witch";
        type = EntityType.WITCH;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Witch(net.minecraft.world.entity.EntityType.WITCH,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

}
