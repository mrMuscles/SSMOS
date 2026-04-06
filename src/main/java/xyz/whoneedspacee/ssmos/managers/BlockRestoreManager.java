package xyz.whoneedspacee.ssmos.managers;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Snow;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.whoneedspacee.ssmos.Main;
import xyz.whoneedspacee.ssmos.utilities.VelocityUtil;

import java.util.*;

public class BlockRestoreManager implements Listener, Runnable {

    public static BlockRestoreManager ourInstance;
    private JavaPlugin plugin = Main.getInstance();
    private HashMap<Block, BlockRestoreData> blocks = new HashMap<>();
    private LinkedList<BlockRestoreMap> restoreMaps;

    public BlockRestoreManager() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        ourInstance = this;
        restoreMaps = new LinkedList<BlockRestoreMap>();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 0L);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void blockBreak(BlockBreakEvent event) {
        if (contains(event.getBlock())) {
            BlockRestoreData data = blocks.get(event.getBlock());
            if (data != null && data.isRestoreOnBreak()) {
                blocks.remove(event.getBlock());
                data.restore();
            }

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void blockPlace(BlockPlaceEvent event) {
        if (contains(event.getBlockPlaced()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void piston(BlockPistonExtendEvent event) {
        if (event.isCancelled())
            return;

        Block push = event.getBlock();
        for (int i = 0; i < 13; i++) {
            push = push.getRelative(event.getDirection());

            if (push.getType() == Material.AIR)
                return;

            if (contains(push)) {
                push.getWorld().playEffect(push.getLocation(), Effect.STEP_SOUND, push.getType());
                event.setCancelled(true);
                return;
            }
        }
    }

    public void run() {
        ArrayList<Block> toRemove = new ArrayList<Block>();

        for (BlockRestoreData cur : blocks.values())
            if (cur.expire())
                toRemove.add(cur.block);

        //Remove Handled
        for (Block cur : toRemove)
            blocks.remove(cur);
    }

    @EventHandler
    public void expireUnload(ChunkUnloadEvent event) {
        Iterator<Map.Entry<Block, BlockRestoreData>> iterator = blocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Block, BlockRestoreData> entry = iterator.next();
            if (entry.getKey().getChunk().equals(event.getChunk())) {
                entry.getValue().restore();
                iterator.remove();
            }
        }
    }

    public boolean restore(Block block) {
        if (!contains(block))
            return false;

        blocks.remove(block).restore();
        return true;
    }

    public void restoreAll() {
        for (BlockRestoreData data : blocks.values())
            data.restore();

        blocks.clear();
    }

    public HashSet<Location> restoreBlockAround(Material type, Location location, int radius) {
        HashSet<Location> restored = new HashSet<Location>();

        Iterator<Block> blockIterator = blocks.keySet().iterator();

        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();

            if (block.getType() != type)
                continue;

            if (block.getLocation().add(0.5, 0.5, 0.5).distance(location) > radius)
                continue;

            restored.add(block.getLocation().add(0.5, 0.5, 0.5));

            blocks.get(block).restore();

            blockIterator.remove();
        }

        return restored;
    }

    public void add(Block block, int toID, byte toData, long expireTime) {
        add(block, toID, toData, expireTime, false);
    }

    public void add(Block block, int toID, byte toData, long expireTime, boolean restoreOnBreak) {
        add(block, toID, toData, 0, (byte) 0, expireTime, restoreOnBreak);
    }

    public void add(Block block, int toID, byte toData, int fromID, byte fromData, long expireTime) {
        add(block, toID, toData, fromID, fromData, expireTime, false);
    }

    public void add(Block block, int toID, byte toData, int fromID, byte fromData, long expireTime, boolean restoreOnBreak) {
        if (!contains(block))
            getBlocks().put(block, new BlockRestoreData(block, toID, toData, fromID, fromData, expireTime, 0, restoreOnBreak));
        else {
            if (getData(block) != null) {
                getData(block).update(toID, toData, expireTime);
            }
        }
    }

    public void add(Block block, org.bukkit.block.data.BlockData toData, long expireTime) {
        add(block, toData, expireTime, false);
    }

    public void add(Block block, org.bukkit.block.data.BlockData toData, long expireTime, boolean restoreOnBreak) {
        if (!contains(block))
            getBlocks().put(block, new BlockRestoreData(block, toData, expireTime, restoreOnBreak));
        else {
            if (getData(block) != null)
                getData(block).update(toData, expireTime);
        }
    }

    /**
     * Maps legacy numeric block IDs and data bytes to modern BlockData.
     * Only covers IDs used within this plugin.
     */
    public static org.bukkit.block.data.BlockData legacyIdToBlockData(int id, byte data) {
        switch (id) {
            case 0:   return Bukkit.createBlockData(Material.AIR);
            case 1:   return Bukkit.createBlockData(Material.STONE);
            case 2:   return Bukkit.createBlockData(Material.GRASS_BLOCK);
            case 3:   return Bukkit.createBlockData(Material.DIRT);
            case 4:   return Bukkit.createBlockData(Material.COBBLESTONE);
            case 8:
            case 9:   return Bukkit.createBlockData(Material.WATER);
            case 30:  return Bukkit.createBlockData(Material.COBWEB);
            case 44:  return Bukkit.createBlockData(Material.SMOOTH_STONE_SLAB);
            case 78: {
                // data 0-7 → layers 1-8
                org.bukkit.block.data.BlockData bd = Bukkit.createBlockData(Material.SNOW);
                ((Snow) bd).setLayers(Math.min(8, Math.max(1, (data & 0xFF) + 1)));
                return bd;
            }
            case 79:  return Bukkit.createBlockData(Material.ICE);
            case 80:  return Bukkit.createBlockData(Material.SNOW_BLOCK);
            case 98:
                if (data == 2) return Bukkit.createBlockData(Material.CRACKED_STONE_BRICKS);
                return Bukkit.createBlockData(Material.STONE_BRICKS);
            case 126: return Bukkit.createBlockData(Material.OAK_SLAB);
            case 174: return Bukkit.createBlockData(Material.PACKED_ICE);
            default:  return Bukkit.createBlockData(Material.STONE);
        }
    }

    public void snow(Block block, byte heightAdd, byte heightMax, long expireTime, long meltDelay, int heightJumps) {
        Material blockMat = block.getType();
        Material downMat = block.getRelative(BlockFace.DOWN).getType();

        // Current snow layer data (0-7 equivalent) when block is already a snow layer
        byte currentLayerData = 0;
        if (blockMat == Material.SNOW) {
            currentLayerData = (byte) (((Snow) block.getBlockData()).getLayers() - 1);
        }

        //Fill Above - block is full snow (layer 8 = data 7) or snow block
        if (((blockMat == Material.SNOW && currentLayerData >= (byte) 7) || blockMat == Material.SNOW_BLOCK) && getData(block) != null) {
            if (getData(block) != null)
                getData(block).update(78, heightAdd, expireTime, meltDelay);

            if (heightJumps > 0)
                snow(block.getRelative(BlockFace.UP), heightAdd, heightMax, expireTime, meltDelay, heightJumps - 1);
            if (heightJumps == -1)
                snow(block.getRelative(BlockFace.UP), heightAdd, heightMax, expireTime, meltDelay, -1);

            return;
        }

        //Not Grounded
        if (!downMat.isSolid() && downMat != Material.SNOW)
            return;

        //Not on Solid Snow (must be at max layer height)
        if (downMat == Material.SNOW) {
            int downLayers = ((Snow) block.getRelative(BlockFace.DOWN).getBlockData()).getLayers() - 1;
            if (downLayers < 7) return;
        }

        //No Snow on Ice
        if (downMat == Material.ICE || downMat == Material.PACKED_ICE)
            return;

        //No Snow on Slabs
        if (downMat == Material.SMOOTH_STONE_SLAB || downMat == Material.OAK_SLAB)
            return;

        //No Snow on Stairs
        if (block.getRelative(BlockFace.DOWN).getType().toString().contains("STAIRS"))
            return;

        //No Snow on Fence or Walls
        if (downMat.name().toLowerCase().contains("fence") ||
                downMat.name().toLowerCase().contains("wall"))
            return;

        //Not Buildable
        if (blockMat.isSolid() && blockMat != Material.SNOW && !blockMat.name().endsWith("_CARPET"))
            return;

        //Limit Build Height
        byte adjustedHeightAdd = heightAdd;
        if (blockMat == Material.SNOW)
            if (currentLayerData >= (byte) (heightMax - 1))
                adjustedHeightAdd = 0;

        //Snow
        if (!contains(block))
            getBlocks().put(block, new BlockRestoreData(block, 78, (byte) Math.max(0, adjustedHeightAdd - 1), 0, (byte) 0, expireTime, meltDelay, false));
        else {
            if (getData(block) != null)
                getData(block).update(78, adjustedHeightAdd, expireTime, meltDelay);
        }
    }

    public boolean contains(Block block) {
        if (getBlocks().containsKey(block))
            return true;

        for (BlockRestoreMap restoreMap : restoreMaps) {
            if (restoreMap.contains(block))
                return true;
        }

        return false;
    }

    public BlockRestoreData getData(Block block) {
        if (blocks.containsKey(block))
            return blocks.get(block);
        return null;
    }

    public Map<Block, BlockRestoreData> getBlocks() {
        return blocks;
    }

    public BlockRestoreMap createMap() {
        BlockRestoreMap map = new BlockRestoreMap(this);
        restoreMaps.add(map);
        return map;
    }

    protected void removeMap(BlockRestoreMap blockRestore) {
        restoreMaps.remove(blockRestore);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (blocks.containsKey(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    public void disable() {
        // Clear all restore maps
        for (BlockRestoreMap restoreMap : restoreMaps) {
            restoreMap.restoreInstant();
        }

        restoreAll();
    }

    public static class BlockRestoreData {

        protected Block block;

        protected int fromID;
        protected byte fromData;

        protected int toID;
        protected byte toData;

        protected long expireDelay;
        protected long epoch;

        protected long meltDelay = 0;
        protected long meltLast = 0;

        protected BlockState fromState;

        protected HashMap<Location, Byte> pad = new HashMap<Location, Byte>();

        protected boolean restoreOnBreak;

        public BlockRestoreData(Block block, int toID, byte toData, int fromID, byte fromData, long expireDelay, long meltDelay, boolean restoreOnBreak) {
            this.block = block;
            this.fromState = block.getState();

            this.fromID = fromID;
            this.fromData = fromData;

            this.toID = toID;
            this.toData = toData;

            this.expireDelay = expireDelay;
            this.epoch = System.currentTimeMillis();

            this.meltDelay = meltDelay;
            this.meltLast = System.currentTimeMillis();

            this.restoreOnBreak = restoreOnBreak;

            //Set
            set();
        }

        public BlockRestoreData(Block block, org.bukkit.block.data.BlockData toBlockData, long expireDelay, boolean restoreOnBreak) {
            this.block = block;
            this.fromState = block.getState();

            this.fromID = 0;
            this.fromData = 0;
            this.toID = 0;
            this.toData = 0;

            this.expireDelay = expireDelay;
            this.epoch = System.currentTimeMillis();

            this.meltDelay = 0;
            this.meltLast = System.currentTimeMillis();

            this.restoreOnBreak = restoreOnBreak;

            block.setBlockData(toBlockData, true);
        }

        public boolean expire() {
            if (System.currentTimeMillis() - epoch < expireDelay)
                return false;

            //Try to Melt
            if (melt())
                return false;

            //Restore
            restore();
            return true;
        }

        public boolean melt() {
            Material blockMat = block.getType();
            if (blockMat != Material.SNOW && blockMat != Material.SNOW_BLOCK)
                return false;

            Material upMat = block.getRelative(BlockFace.UP).getType();
            if (upMat == Material.SNOW || upMat == Material.SNOW_BLOCK) {
                meltLast = System.currentTimeMillis();
                return true;
            }

            if (System.currentTimeMillis() - meltLast < meltDelay)
                return true;

            //Return to Cover: snow block → full snow layer (8 layers)
            if (blockMat == Material.SNOW_BLOCK) {
                org.bukkit.block.data.BlockData bd = Bukkit.createBlockData(Material.SNOW);
                ((Snow) bd).setLayers(8);
                block.setBlockData(bd, false);
            }

            //Reduce one snow layer
            if (block.getType() == Material.SNOW) {
                Snow snow = (Snow) block.getBlockData();
                int layers = snow.getLayers();
                if (layers <= 1) return false;
                snow.setLayers(layers - 1);
                block.setBlockData(snow, false);
            }

            meltLast = System.currentTimeMillis();
            return true;
        }

        public void update(int toIDIn, byte toDataIn) {
            toID = toIDIn;
            toData = toDataIn;

            //Set
            set();
        }

        public void update(int toID, byte addData, long expireTime) {
            //Snow Data
            if (toID == 78) {
                this.toData = (byte) Math.min(7, this.toData + addData);
            } else {
                this.toData = addData;
            }

            this.toID = toID;

            //Set
            set();

            //Reset Time
            expireDelay = expireTime;
            epoch = System.currentTimeMillis();
        }

        public void update(int toID, byte addData, long expireTime, long meltDelay) {
            //Snow Data
            if (toID == 78) {
                this.toData = (byte) Math.min(7, this.toData + addData);
            }

            this.toID = toID;

            //Set
            set();

            //Reset Time
            expireDelay = expireTime;
            epoch = System.currentTimeMillis();

            //Melt Delay
            if (meltDelay < this.meltDelay)
                this.meltDelay = (this.meltDelay + meltDelay) / 2;
        }

        public void update(org.bukkit.block.data.BlockData newBlockData, long expireTime) {
            block.setBlockData(newBlockData, true);
            expireDelay = expireTime;
            epoch = System.currentTimeMillis();
        }

        public void set() {
            if (toID == 78 && toData == (byte) 7) {
                block.setBlockData(Bukkit.createBlockData(Material.SNOW_BLOCK), true);
            } else if (toID == 8 || toID == 9 || toID == 79) {
                handleLilypad(false);
                block.setBlockData(legacyIdToBlockData(toID, toData), true);
            } else {
                block.setBlockData(legacyIdToBlockData(toID, toData), true);
            }
        }

        public boolean isRestoreOnBreak() {
            return restoreOnBreak;
        }

        public void restore() {
            fromState.update(true, false);
            handleLilypad(true);
        }

        public void setFromId(int i) {
            fromID = i;
        }

        public void setFromData(byte i) {
            fromData = i;
        }

        private void handleLilypad(boolean restore) {
            if (restore) {
                for (Location l : pad.keySet()) {
                    l.getBlock().setType(Material.LILY_PAD);
                }
            } else {
                if (block.getRelative(BlockFace.UP, 1).getType() == Material.LILY_PAD) {
                    pad.put(block.getRelative(BlockFace.UP, 1).getLocation(), (byte) 0);
                    block.getRelative(BlockFace.UP, 1).setType(Material.AIR);
                }
            }
        }

    }

    public static void BlockExplosion(Collection<Block> blockSet, Location mid, boolean onlyAbove, boolean removeBlock, long time_restore_ms) {
        if (blockSet.isEmpty())
            return;

        int lowestY = Integer.MAX_VALUE;

        for (Block block : blockSet) {
            int y = block.getLocation().getBlockY();

            if (y < lowestY) {
                lowestY = y;
            }
        }

        //Save
        final HashMap<Block, org.bukkit.block.data.BlockData> blocks = new HashMap<>();

        for (Block cur : blockSet) {
            if (cur.getType() == Material.AIR || onlyAbove && cur.getY() < mid.getY())
                continue;

            blocks.put(cur, cur.getBlockData());

            if (removeBlock) {
                BlockRestoreManager.ourInstance.add(cur, 0, (byte) 0, (long) (time_restore_ms + ((cur.getLocation().getBlockY() - lowestY) * 3000L) + (Math.random() * 1500)));
            }
        }

        //DELAY
        HashSet<FallingBlock> explosionBlocks = new HashSet<FallingBlock>();
        final Location fLoc = mid;
        Bukkit.getServer().getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
            public void run() {
                //Launch
                for (Block cur : blocks.keySet()) {
                    org.bukkit.block.data.BlockData bd = blocks.get(cur);
                    Material mat = bd.getMaterial();
                    // Skip plain and chiseled stone bricks (legacy id 98 data 0 and data 3)
                    if (mat == Material.STONE_BRICKS || mat == Material.CHISELED_STONE_BRICKS)
                        continue;

                    double chance = 0.2 + (double) explosionBlocks.size() / (double) 80;
                    if (Math.random() > Math.min(0.98, chance)) {
                        FallingBlock fall = cur.getWorld().spawnFallingBlock(cur.getLocation().add(0.5, 0.5, 0.5), bd);
                        fall.setDropItem(false);

                        Vector vec = fall.getLocation().subtract(fLoc).toVector().normalize();
                        if (vec.getY() < 0) vec.setY(vec.getY() * -1);

                        VelocityUtil.setVelocity(fall, vec, 0.5 + 0.25 * Math.random(), false, 0, 0.4 + 0.20 * Math.random(), 10, false);

                        explosionBlocks.add(fall);
                    }
                }
            }
        }, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockForm(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof FallingBlock)) {
            return;
        }
        FallingBlock falling = (FallingBlock) e.getEntity();
        falling.getWorld().playEffect(e.getBlock().getLocation(), Effect.STEP_SOUND, falling.getBlockData().getMaterial());
        falling.remove();
        e.setCancelled(true);
    }

    public static class BlockRestoreMap {

        private BlockRestoreManager _blockRestore;
        // The rate at which we restore blocks
        private int _blocksPerTick;
        // Easy access to all the blocks we have modified
        private HashSet<Block> _changedBlocks;
        // A hashmap for each level, so we can quickly restore top down
        private HashMap<Block, BlockData>[] _blocks;

        protected BlockRestoreMap(BlockRestoreManager blockRestore) {
            this(blockRestore, 50);
        }

        protected BlockRestoreMap(BlockRestoreManager blockRestore, int blocksPerTick) {
            _blockRestore = blockRestore;
            _blocksPerTick = blocksPerTick;
            _changedBlocks = new HashSet<Block>();
            _blocks = new HashMap[256];

            // Populate Array
            for (int i = 0; i < 256; i++) {
                _blocks[i] = new HashMap<Block, BlockData>();
            }
        }

        public void addBlockData(BlockData blockData) {
            Block block = blockData.Block;

            if (block.getY() > 0 && block.getY() < _blocks.length) {
                if (!_blocks[block.getY()].containsKey(block)) {
                    _blocks[block.getY()].put(block, blockData);
                }
            }

            _changedBlocks.add(blockData.Block);
        }

        public void set(Block block, Material material) {
            addBlockData(new BlockData(block));
            block.setBlockData(Bukkit.createBlockData(material), false);
        }

        public void set(Block block, Material material, byte toData) {
            addBlockData(new BlockData(block));
            block.setBlockData(Bukkit.createBlockData(material), false);
        }

        public void set(Block block, int toId, byte toData) {
            addBlockData(new BlockData(block));
            block.setBlockData(legacyIdToBlockData(toId, toData), false);
        }

        public boolean contains(Block block) {
            return _changedBlocks.contains(block);
        }

        public HashSet<Block> getChangedBlocks() {
            return _changedBlocks;
        }

        /**
         * Restore all the blocks changed in this BlockRestoreMap
         * NOTE: You should not use the same BlockRestoreMap instance after you run restore.
         * You must initialize a new BlockRestoreMap from BlockRestore
         */
        public void restore() {
            // The idea behind this is that the runnable will restore blocks over time
            // If the server happens to shutdown while the runnable is running, we will still
            // restore all our blocks with restoreInstant (as called by BlockRestore)
            BlockDataRunnable runnable = new BlockDataRunnable(_blockRestore.plugin, new RestoreIterator(), _blocksPerTick, new Runnable() {
                @Override
                public void run() {
                    clearMaps();
                    _blockRestore.removeMap(BlockRestoreMap.this);
                }
            });
            runnable.start();
        }

        private void clearMaps() {
            for (int i = 0; i < 256; i++) {
                _blocks[i].clear();
            }

            _changedBlocks.clear();
        }

        public void restoreInstant() {
            for (int i = 0; i < 256; i++) {
                HashMap<Block, BlockData> map = _blocks[i];
                for (BlockData data : map.values()) {
                    data.restore();
                }
            }

            clearMaps();
        }

        public int getBlocksPerTick() {
            return _blocksPerTick;
        }

        public void setBlocksPerTick(int blocksPerTick) {
            _blocksPerTick = blocksPerTick;
        }

        private class RestoreIterator implements Iterator<BlockData> {
            private Iterator<BlockData> _currentIterator;
            private int _currentIndex;

            public RestoreIterator() {
                _currentIndex = 255;
                updateIterator();
            }

            private void updateIterator() {
                _currentIterator = _blocks[_currentIndex].values().iterator();
            }

            @Override
            public boolean hasNext() {
                while (!_currentIterator.hasNext() && _currentIndex > 0) {
                    _currentIndex--;
                    updateIterator();
                }

                return _currentIterator.hasNext();
            }

            @Override
            public BlockData next() {
                while (!_currentIterator.hasNext() && _currentIndex > 0) {
                    _currentIndex--;
                    updateIterator();
                }

                return _currentIterator.next();
            }

            @Override
            public void remove() {
                _currentIterator.remove();
            }
        }
    }

    public static class BlockDataRunnable implements Runnable {

        private JavaPlugin _plugin;
        private boolean _started;
        private BukkitTask _task;
        private List<BlockData> _changedBlocks;
        private Runnable _onComplete;
        private int _blocksPerTick;
        private Iterator<BlockData> _blockIterator;

        public BlockDataRunnable(JavaPlugin plugin, Iterator<BlockData> blockIterator, int blocksPerTick, Runnable onComplete) {
            _plugin = plugin;
            _changedBlocks = new ArrayList<BlockData>();
            _started = false;
            _blocksPerTick = blocksPerTick;
            _onComplete = onComplete;
            _blockIterator = blockIterator;
        }

        public void start() {
            if (!_started) {
                _task = Bukkit.getScheduler().runTaskTimer(_plugin, this, 1, 1);
                _started = true;
            }
        }

        public void pause() {
            if (_started) {
                _task.cancel();
                _started = false;
            }
        }

        public void setBlocksPerTick(int blocksPerTick) {
            _blocksPerTick = blocksPerTick;
        }

        @Override
        public void run() {
            for (int i = 0; i < _blocksPerTick; i++) {
                if (_blockIterator.hasNext()) {
                    BlockData data = _blockIterator.next();
                    data.restore();
                } else {
                    // We are done
                    _task.cancel();
                    _onComplete.run();
                    return;
                }
            }
        }

    }

    public static class BlockData {
        public Block Block;
        public Material Material;
        public org.bukkit.block.data.BlockData blockDataState;
        public long Time;

        public BlockData(Block block) {
            Block = block;
            Material = block.getType();
            blockDataState = block.getBlockData();
            Time = System.currentTimeMillis();
        }

        public void restore() {
            restore(false);
        }

        public void restore(boolean requireNotAir) {
            if (requireNotAir && Block.getType() == org.bukkit.Material.AIR)
                return;

            Block.setBlockData(blockDataState, true);
        }
    }

}
