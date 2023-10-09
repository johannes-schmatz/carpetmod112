package carpet.mixin.blockEventSerializer;

import carpet.CarpetSettings;
import carpet.helpers.ScheduledBlockEventSerializer;
import carpet.utils.extensions.WorldWithBlockEventSerializer;
import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements WorldWithBlockEventSerializer {
    protected ScheduledBlockEventSerializer blockEventSerializer;

    protected ServerWorldMixin(WorldStorage storage, WorldData data, Dimension dimension, Profiler profiler,
            boolean isClient) {
        super(storage, data, dimension, profiler, isClient);
    }

    @Inject(
            method = "init()Lnet/minecraft/world/World;",
            at = @At("RETURN")
    )
    private void onInit(CallbackInfoReturnable<World> cir) {
        initBlockEventSerializer();
    }

    @Inject(
            method = "addBlockEvent",
            at = @At("RETURN")
    )
    private void onAddBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam, CallbackInfo ci) {
        if(CarpetSettings.blockEventSerializer) blockEventSerializer.markDirty();
    }

    protected void initBlockEventSerializer() {
        blockEventSerializer = (ScheduledBlockEventSerializer)this.savedDataStorage.load(ScheduledBlockEventSerializer.class, "blockEvents");

        if (blockEventSerializer == null) {
            blockEventSerializer = new ScheduledBlockEventSerializer();
            this.savedDataStorage.set("blockEvents", blockEventSerializer);
        }

        blockEventSerializer.setBlockEvents((ServerWorld) (Object) this);
    }

    @Override
    public ScheduledBlockEventSerializer getBlockEventSerializer() {
        return blockEventSerializer;
    }
}
