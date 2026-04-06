package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.monster.EnderMan;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

public class EndermanDisguise extends Disguise {

    public EndermanDisguise(Player owner) {
        super(owner);
        name = "Enderman";
        type = EntityType.ENDERMAN;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new EnderMan(net.minecraft.world.entity.EntityType.ENDERMAN,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

    @Override
    public void update() {
        if(living == null) {
            return;
        }
        super.update();
    }

    /**
     * Sets the block the enderman appears to carry.
     * Block ID-based lookup is no longer supported in 1.21.4;
     * use setHeldBlock(org.bukkit.block.data.BlockData) instead.
     */
    public void setHeldBlock(int id, byte data) {
        // Block integer IDs are not supported in 1.21.4
    }

    public void setHeldBlock(org.bukkit.block.data.BlockData blockData) {
        if (living == null) {
            return;
        }
        org.bukkit.craftbukkit.v1_21_R3.block.data.CraftBlockData craftData =
                (org.bukkit.craftbukkit.v1_21_R3.block.data.CraftBlockData) blockData;
        ((EnderMan) living).setCarriedBlock(craftData.getState());
        List<SynchedEntityData.DataValue<?>> dataValues = living.getEntityData().packDirty();
        if (dataValues != null) {
            ClientboundSetEntityDataPacket data_packet = new ClientboundSetEntityDataPacket(living.getId(), dataValues);
            Utils.sendPacketToAll(data_packet);
        }
    }

}
