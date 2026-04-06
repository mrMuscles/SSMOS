package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.monster.Creeper;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CreeperDisguise extends Disguise {

    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Integer> DATA_SWELL_DIR;
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Boolean> DATA_IS_POWERED;

    static {
        try {
            Field f1 = Creeper.class.getDeclaredField("DATA_SWELL_DIR");
            f1.setAccessible(true);
            DATA_SWELL_DIR = (EntityDataAccessor<Integer>) f1.get(null);

            Field f2 = Creeper.class.getDeclaredField("DATA_IS_POWERED");
            f2.setAccessible(true);
            DATA_IS_POWERED = (EntityDataAccessor<Boolean>) f2.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access Creeper data fields", e);
        }
    }

    private int current_fuse_state = -1;
    private boolean current_powered_state = false;

    public CreeperDisguise(Player owner) {
        super(owner);
        name = "Creeper";
        type = EntityType.CREEPER;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Creeper(net.minecraft.world.entity.EntityType.CREEPER,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

    @Override
    public void update() {
        if(living == null) {
            return;
        }
        List<SynchedEntityData.DataValue<?>> dataValues = new ArrayList<>();
        dataValues.add(SynchedEntityData.DataValue.create(DATA_SWELL_DIR, current_fuse_state));
        dataValues.add(SynchedEntityData.DataValue.create(DATA_IS_POWERED, current_powered_state));
        ClientboundSetEntityDataPacket data_packet = new ClientboundSetEntityDataPacket(living.getId(), dataValues);
        Utils.sendPacketToAll(data_packet);
        super.update();
    }

    public void setFuseState(byte value) {
        if (living == null) {
            return;
        }
        current_fuse_state = value;
        update();
    }

    public void setPoweredState(byte value) {
        if (living == null) {
            return;
        }
        current_powered_state = (value != 0);
        update();
    }

}
