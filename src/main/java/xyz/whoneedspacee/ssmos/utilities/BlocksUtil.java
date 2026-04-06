package xyz.whoneedspacee.ssmos.utilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BlocksUtil {

    public static HashSet<Byte> blockUseSet = new HashSet<Byte>();;
    public static HashSet<Byte> blockAirFoliageSet = new HashSet<Byte>();

    static {
        blockAirFoliageSet.add((byte) Material.AIR.getId());
        blockAirFoliageSet.add((byte) Material.OAK_SAPLING.getId());
        blockAirFoliageSet.add((byte) Material.SHORT_GRASS.getId());
        blockAirFoliageSet.add((byte) Material.LARGE_FERN.getId());
        blockAirFoliageSet.add((byte) Material.DEAD_BUSH.getId());
        blockAirFoliageSet.add((byte) Material.DANDELION.getId());
        blockAirFoliageSet.add((byte) Material.POPPY.getId());
        blockAirFoliageSet.add((byte) Material.BROWN_MUSHROOM.getId());
        blockAirFoliageSet.add((byte) Material.RED_MUSHROOM.getId());
        blockAirFoliageSet.add((byte) Material.FIRE.getId());
        blockAirFoliageSet.add((byte) Material.WHEAT.getId());
        blockAirFoliageSet.add((byte) Material.PUMPKIN_STEM.getId());
        blockAirFoliageSet.add((byte) Material.MELON_STEM.getId());
        blockAirFoliageSet.add((byte) Material.NETHER_WART.getId());
        blockAirFoliageSet.add((byte) Material.TRIPWIRE_HOOK.getId());
        blockAirFoliageSet.add((byte) Material.TRIPWIRE.getId());
        blockAirFoliageSet.add((byte) Material.CARROT.getId());
        blockAirFoliageSet.add((byte) Material.POTATO.getId());
        blockAirFoliageSet.add((byte) Material.LARGE_FERN.getId());
        blockAirFoliageSet.add((byte) Material.WHITE_BANNER.getId());
        blockAirFoliageSet.add((byte) Material.WHITE_WALL_BANNER.getId());

        blockUseSet.add((byte) Material.DISPENSER.getId());
        blockUseSet.add((byte) Material.WHITE_BED.getId());
        blockUseSet.add((byte) Material.PISTON.getId());
        blockUseSet.add((byte) Material.BOOKSHELF.getId());
        blockUseSet.add((byte) Material.CHEST.getId());
        blockUseSet.add((byte) Material.WORKBENCH.getId());
        blockUseSet.add((byte) Material.FURNACE.getId());
        blockUseSet.add((byte) Material.FURNACE.getId());
        blockUseSet.add((byte) Material.OAK_DOOR.getId());
        blockUseSet.add((byte) Material.LEVER.getId());
        blockUseSet.add((byte) Material.IRON_DOOR.getId());
        blockUseSet.add((byte) Material.STONE_BUTTON.getId());
        blockUseSet.add((byte) Material.FENCE.getId());
        blockUseSet.add((byte) Material.REPEATER.getId());
        blockUseSet.add((byte) Material.REPEATER.getId());
        blockUseSet.add((byte) Material.TRAP_DOOR.getId());
        blockUseSet.add((byte) Material.FENCE_GATE.getId());
        blockUseSet.add((byte) Material.NETHER_BRICK_FENCE.getId());
        blockUseSet.add((byte) Material.ENCHANTING_TABLE.getId());
        blockUseSet.add((byte) Material.BREWING_STAND.getId());
        blockUseSet.add((byte) Material.ENDER_CHEST.getId());
        blockUseSet.add((byte) Material.ANVIL.getId());
        blockUseSet.add((byte) Material.TRAPPED_CHEST.getId());
        blockUseSet.add((byte) Material.HOPPER.getId());
        blockUseSet.add((byte) Material.DROPPER.getId());

        blockUseSet.add((byte) Material.BIRCH_FENCE_GATE.getId());
        blockUseSet.add((byte) Material.JUNGLE_FENCE_GATE.getId());
        blockUseSet.add((byte) Material.DARK_OAK_FENCE_GATE.getId());
        blockUseSet.add((byte) Material.ACACIA_FENCE_GATE.getId());
        blockUseSet.add((byte) Material.SPRUCE_FENCE_GATE.getId());
        blockUseSet.add((byte) Material.BIRCH_FENCE_GATE.getId());
        blockUseSet.add((byte) Material.JUNGLE_FENCE_GATE.getId());
        blockUseSet.add((byte) Material.DARK_OAK_FENCE_GATE.getId());
        blockUseSet.add((byte) Material.ACACIA_FENCE_GATE.getId());

        blockUseSet.add((byte) Material.SPRUCE_DOOR.getId());
        blockUseSet.add((byte) Material.BIRCH_DOOR.getId());
        blockUseSet.add((byte) Material.JUNGLE_DOOR.getId());
        blockUseSet.add((byte) Material.ACACIA_DOOR.getId());
        blockUseSet.add((byte) Material.DARK_OAK_DOOR.getId());
    }

    public static boolean isAirOrFoliage(Block block) {
        return blockAirFoliageSet.contains((byte) block.getTypeId());
    }

    public static boolean isUsable(Block block) {
        return blockUseSet.contains((byte) block.getTypeId());
    }

    public static List<Block> getBlocks(Location start, int radius) {
        if (radius <= 0) {
            return new ArrayList<Block>(0);
        }
        int iterations = (radius * 2) + 1;
        List<Block> blocks = new ArrayList<Block>(iterations * iterations * iterations);
        blocks.add((Block) start.getBlock());
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    blocks.add((Block) start.getBlock().getRelative(x, y, z));
                }
            }
        }
        return blocks;
    }

    public static HashMap<Block, Double> getInRadius(Location location, double dR) {
        return getInRadius(location, dR, 9999);
    }

    public static HashMap<Block, Double> getInRadius(Location location, double dR, double maxHeight) {
        HashMap<Block, Double> blockList = new HashMap<Block, Double>();
        int iR = (int) dR + 1;
        for (int x = -iR; x <= iR; x++) {
            for (int z = -iR; z <= iR; z++) {
                for (int y = -iR; y <= iR; y++) {
                    if (Math.abs(y) > maxHeight) {
                        continue;
                    }
                    Block curBlock = location.getWorld().getBlockAt(
                            (int) (location.getX() + x), (int) (location.getY() + y), (int) (location.getZ() + z));
                    double offset = location.distance(curBlock.getLocation().add(0.5, 0.5, 0.5));
                    if (offset <= dR) {
                        blockList.put(curBlock, 1 - (offset / dR));
                    }
                }
            }
        }
        return blockList;
    }

}
