package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MagmaCubeDisguise extends Disguise {

    protected int size = 1;

    public MagmaCubeDisguise(Player owner) {
        super(owner);
        name = "Magma Cube";
        type = EntityType.MAGMA_CUBE;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        MagmaCube magmaCube = new MagmaCube(net.minecraft.world.entity.EntityType.MAGMA_CUBE,
                ((CraftWorld) owner.getWorld()).getHandle());
        magmaCube.setSize(size, false);
        return magmaCube;
    }

    @Override
    public void update() {
        if (living == null) {
            return;
        }
        List<SynchedEntityData.DataValue<?>> dataValues = new ArrayList<>();
        dataValues.add(SynchedEntityData.DataValue.create(SlimeDisguise.ID_SIZE, size));
        ClientboundSetEntityDataPacket size_packet = new ClientboundSetEntityDataPacket(living.getId(), dataValues);
        Utils.sendPacketToAll(size_packet);
        super.update();
    }

    public void setSize(int size) {
        this.size = size;
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
