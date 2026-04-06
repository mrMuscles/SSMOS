package xyz.whoneedspacee.ssmos.abilities.original;

import xyz.whoneedspacee.ssmos.managers.disguises.Disguise;
import xyz.whoneedspacee.ssmos.managers.disguises.EndermanDisguise;
import xyz.whoneedspacee.ssmos.managers.ownerevents.OwnerRightClickEvent;
import xyz.whoneedspacee.ssmos.projectiles.original.BlockProjectile;
import xyz.whoneedspacee.ssmos.abilities.Ability;
import xyz.whoneedspacee.ssmos.managers.DisguiseManager;
import xyz.whoneedspacee.ssmos.utilities.BlocksUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.whoneedspacee.ssmos.attributes.Attribute;

public class BlockToss extends Ability implements OwnerRightClickEvent {

    private BlockData holding_block = null;
    private long pickup_time_ms = 0;
    private int toss_task = -1;
    protected long charge_time_ms = 1200;

    public BlockToss() {
        super();
        this.name = "Block Toss";
        this.cooldownTime = 2.5;
        this.usage = Attribute.AbilityUsage.BLOCKING;
        this.description = new String[] {
                ChatColor.RESET + "Picks up a block from the ground, and",
                ChatColor.RESET + "then hurls it at opponents, causing huge",
                ChatColor.RESET + "damage and knockback if it hits.",
                ChatColor.RESET + "",
                ChatColor.RESET + "The longer you hold the block, the harder",
                ChatColor.RESET + "you throw it. You will hear a 'tick' sound",
                ChatColor.RESET + "when it is fully charged.",
        };
    }

    public void onOwnerRightClick(PlayerInteractEvent e) {
        if(!check()) {
            return;
        }
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = e.getClickedBlock();
        Material material = e.getClickedBlock().getType();
        if(BlocksUtil.isUsable(block) || material == Material.REDSTONE_WIRE) {
            return;
        }
        BlockData blockData = block.getBlockData();
        setDisguiseBlock(blockData);
        holding_block = blockData;
        pickup_time_ms = System.currentTimeMillis();
        owner.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, material);
        if(Bukkit.getScheduler().isQueued(toss_task) || Bukkit.getScheduler().isCurrentlyRunning(toss_task)) {
            Bukkit.getScheduler().cancelTask(toss_task);
        }
        toss_task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            boolean clicked = false;

            @Override
            public void run() {
                if(owner == null) {
                    Bukkit.getScheduler().cancelTask(toss_task);
                    return;
                }
                if(!owner.isBlocking()) {
                    Bukkit.getScheduler().cancelTask(toss_task);
                    checkAndActivate();
                    return;
                }
                if(clicked) {
                    return;
                }
                if(System.currentTimeMillis() - pickup_time_ms > charge_time_ms) {
                    owner.getWorld().playEffect(owner.getLocation(), Effect.CLICK1, 0);
                    clicked = true;
                }
            }
        }, 0L, 0L);
    }

    public void activate() {
        long charge = System.currentTimeMillis() - pickup_time_ms;
        double mult = Math.min(1.4, 1.4 * ((double) charge / charge_time_ms));
        BlockData bd = holding_block != null ? holding_block : Material.STONE.createBlockData();
        BlockProjectile projectile = new BlockProjectile(owner, name, charge, mult, bd);
        projectile.launchProjectile();
        setDisguiseBlock(null);
    }

    public void setDisguiseBlock(BlockData blockData) {
        Disguise disguise = DisguiseManager.disguises.get(owner);
        if(disguise == null || !(disguise instanceof EndermanDisguise)) {
            return;
        }
        EndermanDisguise endermanDisguise = (EndermanDisguise) disguise;
        if (blockData == null) {
            endermanDisguise.setHeldBlock(0, (byte) 0);
        } else {
            endermanDisguise.setHeldBlock(blockData);
        }
    }

}
