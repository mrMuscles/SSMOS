package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.monster.Guardian;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

public class GuardianDisguise extends Disguise {

    public GuardianDisguise(Player owner) {
        super(owner);
        name = "Guardian";
        type = EntityType.GUARDIAN;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Guardian(net.minecraft.world.entity.EntityType.GUARDIAN,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

    public void setTarget(Entity entity) {
        this.target = entity;
        int id = -1;
        if(target != null) {
            id = target.getEntityId();
        }
        ((Guardian) living).setActiveAttackTarget(id);
        List<SynchedEntityData.DataValue<?>> dataValues = living.getEntityData().packDirty();
        if (dataValues != null) {
            ClientboundSetEntityDataPacket target_packet = new ClientboundSetEntityDataPacket(living.getId(), dataValues);
            Utils.sendPacketToAll(target_packet);
        }
    }

    @Override
    public void playDamageSound() {
        for(Player player : owner.getWorld().getPlayers()) {
            player.playSound(owner.getLocation(), "mob.guardian.hit", getVolume(), getPitch());
        }
    }

}
