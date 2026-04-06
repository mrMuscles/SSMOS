package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Skeleton;
import com.mojang.datafixers.util.Pair;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SkeletonDisguise extends Disguise {

    public SkeletonDisguise(Player owner) {
        super(owner);
        name = "Skeleton";
        type = EntityType.SKELETON;
    }

    public net.minecraft.world.entity.LivingEntity newLiving() {
        return new Skeleton(net.minecraft.world.entity.EntityType.SKELETON,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

    @Override
    public void update() {
        if (living == null) {
            return;
        }
        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> slots = new ArrayList<>();
        slots.add(Pair.of(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(owner.getInventory().getItemInMainHand())));
        ClientboundSetEquipmentPacket weapon_packet = new ClientboundSetEquipmentPacket(living.getId(), slots);
        Utils.sendPacketToAllBut(owner, weapon_packet);
        super.update();
    }

}
