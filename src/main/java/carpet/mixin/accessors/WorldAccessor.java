package carpet.mixin.accessors;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(World.class)
public interface WorldAccessor {
    @Accessor("doTicksImmediately") void setScheduledUpdatesAreImmediate(boolean scheduledUpdatesAreImmediate);
    @Accessor("randomTickLCG") int getUpdateLCG();
    @Accessor("randomTickLCG") void setUpdateLCG(int seed);
    @Invoker("isChunkLoadedAt") boolean invokeIsChunkLoaded(int x, int z, boolean allowEmpty);
    @Invoker("isOutsideWorldHeight") boolean invokeIsOutsideWorld(BlockPos pos);
}
