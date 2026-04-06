package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SkeletonHorseDisguise extends Disguise {

    private static EntityDataAccessor<Byte> DATA_FLAGS_ID;

    static {
        try {
            Field f = AbstractHorse.class.getDeclaredField("DATA_FLAGS_ID");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            EntityDataAccessor<Byte> accessor = (EntityDataAccessor<Byte>) f.get(null);
            DATA_FLAGS_ID = accessor;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SkeletonHorseDisguise(Player owner) {
        super(owner);
        name = "Sheep";
        type = EntityType.HORSE;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new SkeletonHorse(net.minecraft.world.entity.EntityType.SKELETON_HORSE,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

    public void setRearing(boolean rearing) {
        if (DATA_FLAGS_ID == null) {
            return;
        }
        byte flags = rearing ? (byte) 0x40 : (byte) 0;
        List<SynchedEntityData.DataValue<?>> dataValues = new ArrayList<>();
        dataValues.add(SynchedEntityData.DataValue.create(DATA_FLAGS_ID, flags));
        ClientboundSetEntityDataPacket rearing_packet = new ClientboundSetEntityDataPacket(living.getId(), dataValues);
        Utils.sendPacketToAll(rearing_packet);
    }

}
