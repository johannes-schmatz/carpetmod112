package carpet.mixin.accessors;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(World.class)
public interface WorldAccessor {
    @Accessor("immediateUpdates") void setScheduledUpdatesAreImmediate(boolean scheduledUpdatesAreImmediate);
    @Accessor("lcgBlockSeed") int getUpdateLCG();
    @Accessor("lcgBlockSeed") void setUpdateLCG(int seed);
    @Invoker("isChunkLoaded") boolean invokeIsChunkLoaded(int x, int z, boolean allowEmpty);
    @Invoker("method_11475") boolean invokeIsOutsideWorld(BlockPos pos);
}
