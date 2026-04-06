package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

public class VillagerDisguise extends Disguise {

    public VillagerDisguise(Player owner) {
        super(owner);
        name = "Villager";
        type = EntityType.VILLAGER;
    }

    protected net.minecraft.world.entity.LivingEntity newLiving() {
        return new Villager(net.minecraft.world.entity.EntityType.VILLAGER,
                ((CraftWorld) owner.getWorld()).getHandle());
    }

    public void setProfession(int id) {
        VillagerProfession profession;
        switch (id) {
            case 1: profession = VillagerProfession.LIBRARIAN; break;
            case 2: profession = VillagerProfession.CLERIC; break;
            case 3: profession = VillagerProfession.WEAPONSMITH; break;
            case 4: profession = VillagerProfession.BUTCHER; break;
            default: profession = VillagerProfession.FARMER; break;
        }
        VillagerData data = new VillagerData(VillagerType.PLAINS, profession, 1);
        ((Villager) living).setVillagerData(data);
        List<SynchedEntityData.DataValue<?>> dataValues = living.getEntityData().packDirty();
        if (dataValues != null) {
            ClientboundSetEntityDataPacket profession_packet = new ClientboundSetEntityDataPacket(living.getId(), dataValues);
            Utils.sendPacketToAll(profession_packet);
        }
    }

    public void setFarmer() {
        setProfession(0);
    }

    public void setLibrarian() {
        setProfession(1);
    }

    public void setPriest() {
        setProfession(2);
    }

    public void setBlacksmith() {
        setProfession(3);
    }

    public void setButcher() {
        setProfession(4);
    }

}
