package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.monster.Slime;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SlimeDisguise extends Disguise {

    @SuppressWarnings("unchecked")
    protected static final EntityDataAccessor<Integer> ID_SIZE;

    static {
        try {
            Field f = Slime.class.getDeclaredField("ID_SIZE");
            f.setAccessible(true);
            ID_SIZE = (EntityDataAccessor<Integer>) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access Slime data fields", e);
        }
    }

    protected int size = 1;

    public SlimeDisguise(Player owner) {
        super(owner);
        name = "Slime";
        type = EntityType.SLIME;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        Slime slime = new Slime(net.minecraft.world.entity.EntityType.SLIME,
                ((CraftWorld) owner.getWorld()).getHandle());
        slime.setSize(1, false);
        return slime;
    }

    @Override
    public void update() {
        if (living == null) {
            return;
        }
        size = 1;
        if (owner.getExp() > 0.8) {
            size = 3;
        } else if (owner.getExp() > 0.55) {
            size = 2;
        }
        List<SynchedEntityData.DataValue<?>> dataValues = new ArrayList<>();
        dataValues.add(SynchedEntityData.DataValue.create(ID_SIZE, size));
        ClientboundSetEntityDataPacket size_packet = new ClientboundSetEntityDataPacket(living.getId(), dataValues);
        Utils.sendPacketToAll(size_packet);
        super.update();
    }

    @Override
    public Sound getDamageSound() {
        if(size > 1) {
            return Sound.ENTITY_SLIME_SQUISH;
        }
        return Sound.ENTITY_SLIME_SQUISH_SMALL;
    }

    @Override
    public float getVolume() {
        return 0.4f * (float) size;
    }

}
