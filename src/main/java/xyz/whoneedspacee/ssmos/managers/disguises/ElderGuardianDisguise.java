package xyz.whoneedspacee.ssmos.managers.disguises;

import net.minecraft.world.entity.monster.ElderGuardian;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ElderGuardianDisguise extends GuardianDisguise {

    public ElderGuardianDisguise(Player owner) {
        super(owner);
        name = "Guardian";
        type = EntityType.GUARDIAN;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new ElderGuardian(net.minecraft.world.entity.EntityType.ELDER_GUARDIAN,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

    @Override
    public void playDamageSound() {
        for(Player player : owner.getWorld().getPlayers()) {
            player.playSound(owner.getLocation(), "mob.guardian.elder.hit", getVolume(), getPitch());
        }
    }

}
