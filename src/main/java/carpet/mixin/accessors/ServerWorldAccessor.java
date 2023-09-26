package carpet.mixin.accessors;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ScheduledTick;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.TreeSet;

@Mixin(ServerWorld.class)
public interface ServerWorldAccessor {
    @Accessor("scheduledTicksInOrder") TreeSet<ScheduledTick> getPendingTickListEntriesTreeSet();
    @Accessor("nextBlockEventQueueIndex") int getBlockEventCacheIndex();
    @Invoker("findLightningTarget") BlockPos invokeAdjustPosToNearbyEntity(BlockPos pos);
}
