package carpet.mixin.accessors;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ScheduledTick;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.TreeSet;

@Mixin(ServerWorld.class)
public interface ServerWorldAccessor {
    @Accessor("scheduledTicks") TreeSet<ScheduledTick> getPendingTickListEntriesTreeSet();
    @Accessor("field_2816") int getBlockEventCacheIndex();
    @Invoker("method_10749") BlockPos invokeAdjustPosToNearbyEntity(BlockPos pos);
}
