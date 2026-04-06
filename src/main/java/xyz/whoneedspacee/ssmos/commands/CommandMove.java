package xyz.whoneedspacee.ssmos.commands;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class CommandMove implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        if(!commandSender.isOp()) {
            return true;
        }
        Player player = (Player) commandSender;
        CraftPlayer craftplayer = (CraftPlayer) player;
        float power = 1;
        if (args.length == 1) {
            power = Float.parseFloat(args[0]);
        }
        craftplayer.getHandle().connection.send(new ClientboundSetEntityMotionPacket(player.getEntityId(), power, 0, 0));
        return true;
    }

}
