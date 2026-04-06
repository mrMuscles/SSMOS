package xyz.whoneedspacee.ssmos.utilities;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class VelocityUtil {

    public static void setVelocity(Entity entity, Vector velocity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        nmsEntity.setDeltaMovement(velocity.getX(), velocity.getY(), velocity.getZ());
        nmsEntity.hurtMarked = true;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            Utils.sendPacket(player, new ClientboundSetEntityMotionPacket(entityPlayer));
        }
    }

    public static void setVelocity(Entity ent, double str, double yAdd, double yMax, boolean groundBoost) {
        setVelocity(ent, ent.getLocation().getDirection(), str, false, 0, yAdd, yMax, groundBoost);
    }

    public static void setVelocity(Entity ent, Vector vec, double str, boolean ySet, double yBase, double yAdd, double yMax, boolean groundBoost) {
        if (Double.isNaN(vec.getX()) || Double.isNaN(vec.getY()) || Double.isNaN(vec.getZ()) || vec.length() == 0) {
            return;
        }

        //YSet
        if (ySet)
            vec.setY(yBase);

        //Modify
        vec.normalize();
        vec.multiply(str);

        //YAdd
        vec.setY(vec.getY() + yAdd);

        //Limit
        if (vec.getY() > yMax)
            vec.setY(yMax);

        if (groundBoost)
            if (ent.isOnGround())
                vec.setY(vec.getY() + 0.2);

        //Velocity
        ent.setFallDistance(0);

        VelocityUtil.setVelocity(ent, vec);
        //Bukkit.broadcastMessage("Set Velocity: " + vec);
    }

}
