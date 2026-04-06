package xyz.whoneedspacee.ssmos.utilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlocksUtil {

    public static Set<Material> blockUseSet = EnumSet.noneOf(Material.class);
    public static Set<Material> blockAirFoliageSet = EnumSet.noneOf(Material.class);

    static {
        blockAirFoliageSet.add(Material.AIR);
        blockAirFoliageSet.add(Material.OAK_SAPLING);
        blockAirFoliageSet.add(Material.BIRCH_SAPLING);
        blockAirFoliageSet.add(Material.SPRUCE_SAPLING);
        blockAirFoliageSet.add(Material.JUNGLE_SAPLING);
        blockAirFoliageSet.add(Material.ACACIA_SAPLING);
        blockAirFoliageSet.add(Material.DARK_OAK_SAPLING);
        blockAirFoliageSet.add(Material.SHORT_GRASS);
        blockAirFoliageSet.add(Material.FERN);
        blockAirFoliageSet.add(Material.LARGE_FERN);
        blockAirFoliageSet.add(Material.DEAD_BUSH);
        blockAirFoliageSet.add(Material.DANDELION);
        blockAirFoliageSet.add(Material.POPPY);
        blockAirFoliageSet.add(Material.BROWN_MUSHROOM);
        blockAirFoliageSet.add(Material.RED_MUSHROOM);
        blockAirFoliageSet.add(Material.FIRE);
        blockAirFoliageSet.add(Material.WHEAT);
        blockAirFoliageSet.add(Material.PUMPKIN_STEM);
        blockAirFoliageSet.add(Material.MELON_STEM);
        blockAirFoliageSet.add(Material.NETHER_WART);
        blockAirFoliageSet.add(Material.TRIPWIRE_HOOK);
        blockAirFoliageSet.add(Material.TRIPWIRE);
        blockAirFoliageSet.add(Material.CARROTS);
        blockAirFoliageSet.add(Material.POTATOES);
        blockAirFoliageSet.add(Material.WHITE_BANNER);
        blockAirFoliageSet.add(Material.WHITE_WALL_BANNER);
        blockAirFoliageSet.add(Material.CAVE_AIR);
        blockAirFoliageSet.add(Material.VOID_AIR);

        blockUseSet.add(Material.DISPENSER);
        blockUseSet.add(Material.WHITE_BED);
        blockUseSet.add(Material.ORANGE_BED);
        blockUseSet.add(Material.MAGENTA_BED);
        blockUseSet.add(Material.LIGHT_BLUE_BED);
        blockUseSet.add(Material.YELLOW_BED);
        blockUseSet.add(Material.LIME_BED);
        blockUseSet.add(Material.PINK_BED);
        blockUseSet.add(Material.GRAY_BED);
        blockUseSet.add(Material.LIGHT_GRAY_BED);
        blockUseSet.add(Material.CYAN_BED);
        blockUseSet.add(Material.PURPLE_BED);
        blockUseSet.add(Material.BLUE_BED);
        blockUseSet.add(Material.BROWN_BED);
        blockUseSet.add(Material.GREEN_BED);
        blockUseSet.add(Material.RED_BED);
        blockUseSet.add(Material.BLACK_BED);
        blockUseSet.add(Material.PISTON);
        blockUseSet.add(Material.BOOKSHELF);
        blockUseSet.add(Material.CHEST);
        blockUseSet.add(Material.CRAFTING_TABLE);
        blockUseSet.add(Material.FURNACE);
        blockUseSet.add(Material.OAK_DOOR);
        blockUseSet.add(Material.SPRUCE_DOOR);
        blockUseSet.add(Material.BIRCH_DOOR);
        blockUseSet.add(Material.JUNGLE_DOOR);
        blockUseSet.add(Material.ACACIA_DOOR);
        blockUseSet.add(Material.DARK_OAK_DOOR);
        blockUseSet.add(Material.IRON_DOOR);
        blockUseSet.add(Material.LEVER);
        blockUseSet.add(Material.STONE_BUTTON);
        blockUseSet.add(Material.OAK_BUTTON);
        blockUseSet.add(Material.OAK_FENCE);
        blockUseSet.add(Material.REPEATER);
        blockUseSet.add(Material.OAK_TRAPDOOR);
        blockUseSet.add(Material.IRON_TRAPDOOR);
        blockUseSet.add(Material.OAK_FENCE_GATE);
        blockUseSet.add(Material.NETHER_BRICK_FENCE);
        blockUseSet.add(Material.ENCHANTING_TABLE);
        blockUseSet.add(Material.BREWING_STAND);
        blockUseSet.add(Material.ENDER_CHEST);
        blockUseSet.add(Material.ANVIL);
        blockUseSet.add(Material.TRAPPED_CHEST);
        blockUseSet.add(Material.HOPPER);
        blockUseSet.add(Material.DROPPER);
        blockUseSet.add(Material.BIRCH_FENCE_GATE);
        blockUseSet.add(Material.JUNGLE_FENCE_GATE);
        blockUseSet.add(Material.DARK_OAK_FENCE_GATE);
        blockUseSet.add(Material.ACACIA_FENCE_GATE);
        blockUseSet.add(Material.SPRUCE_FENCE_GATE);
    }

    public static boolean isAirOrFoliage(Block block) {
        return blockAirFoliageSet.contains(block.getType());
    }

    public static boolean isUsable(Block block) {
        return blockUseSet.contains(block.getType());
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
