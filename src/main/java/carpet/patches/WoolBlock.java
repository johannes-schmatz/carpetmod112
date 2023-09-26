package carpet.patches;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.server.MinecraftServer;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class WoolBlock extends net.minecraft.block.ColoredBlock {

    private Map<DyeColor, Set<Pair<Integer, BlockPos>>> woolBlocks = new EnumMap<>(DyeColor.class);
    private EnumSet<DyeColor> alreadyCheckedColors = EnumSet.noneOf(DyeColor.class);
    private boolean updatingWool;
    private Set<Pair<Integer, BlockPos>> updatedBlocks = new HashSet<>();

    public WoolBlock() {
        super(Material.WOOL);
        for (DyeColor color : DyeColor.values())
            woolBlocks.put(color, new HashSet<>());
    }

    public void clearWirelessLocations() {
        woolBlocks.forEach((k, v) -> v.clear());
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return CarpetSettings.wirelessRedstone;
    }

    private List<Pair<Integer, BlockPos>> getAllWoolOfType(MinecraftServer server, DyeColor type) {
        List<Pair<Integer, BlockPos>> woolList = new ArrayList<>();

        Iterator<Pair<Integer, BlockPos>> locationItr = woolBlocks.get(type).iterator();
        while (locationItr.hasNext()) {
            Pair<Integer, BlockPos> location = locationItr.next();
            World world = server.getWorld(location.getLeft());
            CarpetClientChunkLogger.setReason("Carpet wireless redstone");
            BlockState state = world.getBlockState(location.getRight());
            if (state.getBlock() != this || state.get(COLOR) != type) {
                locationItr.remove();
            } else {
                woolList.add(location);
            }
            CarpetClientChunkLogger.resetReason();
        }

        return woolList;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!CarpetSettings.wirelessRedstone) return;

        // Adds this location if absent
        woolBlocks.get(state.get(COLOR)).add(Pair.of(worldIn.dimension.getType().getId(), pos));

        boolean updateRoot = !updatingWool;
        updatingWool = true;

        if (!updatedBlocks.add(Pair.of(worldIn.dimension.getType().getId(), pos)))
            return;

        for (Pair<Integer, BlockPos> wool : getAllWoolOfType(worldIn.getServer(), state.get(COLOR))) {
            World world = worldIn.getServer().getWorld(wool.getLeft());
            CarpetClientChunkLogger.setReason("Carpet wireless redstone");
            world.updateNeighbors(wool.getRight(), this, false);
            CarpetClientChunkLogger.resetReason();
        }

        if (updateRoot) {
            updatingWool = false;
            updatedBlocks.clear();
        }
    }

    @Override
    public int getSignal(BlockState state, WorldView blockAccess, BlockPos pos, Direction side) {
        if (!CarpetSettings.wirelessRedstone)
            return 0;

        World worldIn = (World) blockAccess;
        // Adds this location if absent
        woolBlocks.get(state.get(COLOR)).add(Pair.of(worldIn.dimension.getType().getId(), pos));

        if (!alreadyCheckedColors.add(state.get(COLOR)))
            return 0;

        int power = 0;
        for (Pair<Integer, BlockPos> location : getAllWoolOfType(worldIn.getServer(), state.get(COLOR))) {
            World world = worldIn.getServer().getWorld(location.getLeft());
            CarpetClientChunkLogger.setReason("Carpet wireless redstone");
            for (Direction facing : Direction.values()) {
                BlockPos testPos = location.getRight().offset(facing);
                if (world.getBlockState(testPos) != state)
                    power = Math.max(power, world.getSignal(testPos, facing));
            }
            CarpetClientChunkLogger.resetReason();
        }

        alreadyCheckedColors.clear();

        return power;
    }

    @Override
    public void onAdded(World worldIn, BlockPos pos, BlockState state) {
        if (CarpetSettings.wirelessRedstone) {
            woolBlocks.get(state.get(COLOR)).add(Pair.of(worldIn.dimension.getType().getId(), pos));
        }
    }

    @Override
    public void onRemoved(World worldIn, BlockPos pos, BlockState state) {
        if (CarpetSettings.wirelessRedstone) {
            woolBlocks.get(state.get(COLOR)).remove(Pair.of(worldIn.dimension.getType().getId(), pos));
        }
    }
}
