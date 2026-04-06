package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.animal.Sheep;
import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SheepDisguise extends Disguise {

    protected int color_id = 0;

    public SheepDisguise(Player owner) {
        super(owner);
        name = "Sheep";
        type = EntityType.SHEEP;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Sheep(net.minecraft.world.entity.EntityType.SHEEP,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

    public void setColor(DyeColor color) {
        setColor(color.ordinal());
    }

    public void setColor(int color_id) {
        this.color_id = color_id;
        List<SynchedEntityData.DataValue<?>> dataValues = new ArrayList<>();
        if (color_id == 16) {
            // Sheared state: bit 4 set (0x10), white color (0)
            dataValues.add(SynchedEntityData.DataValue.create(Sheep.DATA_WOOL_ID, (byte) 0x10));
        } else {
            dataValues.add(SynchedEntityData.DataValue.create(Sheep.DATA_WOOL_ID, (byte) color_id));
        }
        ClientboundSetEntityDataPacket target_packet = new ClientboundSetEntityDataPacket(living.getId(), dataValues);
        Utils.sendPacketToAll(target_packet);
    }

    public int getColor() {
        return color_id;
    }

    public void setSheared() {
        setColor(16);
    }

    public boolean getSheared() {
        return (color_id == 16);
    }

}
