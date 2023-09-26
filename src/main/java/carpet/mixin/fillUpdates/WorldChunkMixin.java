package carpet.mixin.fillUpdates;

import carpet.utils.extensions.ExtendedWorldChunk;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import org.jetbrains.annotations.Nullable;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements ExtendedWorldChunk {
    @Shadow @Nullable public abstract BlockState setBlockState(BlockPos pos, BlockState state);

    private boolean skipUpdates;

    @Override
    public BlockState setBlockStateCarpet(BlockPos pos, BlockState state, boolean skipUpdates) {
        try {
            this.skipUpdates = skipUpdates;
            return this.setBlockState(pos, state);
        } finally {
            this.skipUpdates = false;
        }
    }

    @Redirect(
            method = "setBlockState",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/World;isClient:Z"
            )
    )
    private boolean skipUpdates(World world) {
        return skipUpdates || world.isClient;
    }
}
