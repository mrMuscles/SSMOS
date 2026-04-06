package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import com.mojang.datafixers.util.Pair;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NetherPigDisguise extends Disguise {

    public NetherPigDisguise(Player owner) {
        super(owner);
        name = "Zombie";
        type = EntityType.ZOMBIFIED_PIGLIN;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new ZombifiedPiglin(net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN,
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
