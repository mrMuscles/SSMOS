package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import com.mojang.datafixers.util.Pair;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ZombieDisguise extends Disguise {

    public ZombieDisguise(Player owner) {
        super(owner);
        name = "Zombie";
        type = EntityType.ZOMBIE;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Zombie(net.minecraft.world.entity.EntityType.ZOMBIE,
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
