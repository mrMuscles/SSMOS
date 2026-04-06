package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.monster.Creeper;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CreeperDisguise extends Disguise {

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
        dataValues.add(SynchedEntityData.DataValue.create(Creeper.DATA_SWELL_DIR, current_fuse_state));
        dataValues.add(SynchedEntityData.DataValue.create(Creeper.DATA_IS_POWERED, current_powered_state));
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
