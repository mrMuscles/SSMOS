package xyz.whoneedspacee.ssmos.attributes;

import xyz.whoneedspacee.ssmos.managers.disguises.Disguise;
import xyz.whoneedspacee.ssmos.managers.disguises.SheepDisguise;
import xyz.whoneedspacee.ssmos.managers.DisguiseManager;

public class RainbowSheep extends Attribute {

    public RainbowSheep() {
        super();
        this.name = "Rainbow Sheep";
        task = this.runTaskTimer(plugin, 0, 10);
    }

    @Override
    public void run() {
        checkAndActivate();
    }

    public void activate() {
        Disguise disguise = DisguiseManager.disguises.get(owner);
        if(!(disguise instanceof SheepDisguise)) {
            return;
        }
        SheepDisguise sheepDisguise = (SheepDisguise) disguise;
        net.minecraft.world.entity.LivingEntity living = sheepDisguise.getLiving();
        if (living != null) {
            living.getBukkitEntity().setCustomName("jeb_");
            living.getBukkitEntity().setCustomNameVisible(false);
        }
    }

}
