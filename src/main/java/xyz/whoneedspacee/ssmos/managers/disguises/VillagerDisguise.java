package xyz.whoneedspacee.ssmos.managers.disguises;

import xyz.whoneedspacee.ssmos.utilities.Utils;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import org.bukkit.craftbukkit.CraftWorld;
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
        net.minecraft.core.ResourceKey<VillagerProfession> profKey;
        switch (id) {
            case 1: profKey = VillagerProfession.LIBRARIAN; break;
            case 2: profKey = VillagerProfession.CLERIC; break;
            case 3: profKey = VillagerProfession.WEAPONSMITH; break;
            case 4: profKey = VillagerProfession.BUTCHER; break;
            default: profKey = VillagerProfession.FARMER; break;
        }
        net.minecraft.core.Holder<VillagerProfession> profHolder =
                BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(profKey);
        net.minecraft.core.Holder<VillagerType> typeHolder =
                BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS);
        VillagerData data = new VillagerData(typeHolder, profHolder, 1);
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
