package carpet.mixin.accessors;

import net.minecraft.block.state.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.server.command.CloneCommand$ClonedBlock")
public interface StaticCloneDataAccessor {
    @Accessor("pos") BlockPos getPos();
    @Accessor("state") BlockState getBlockState();
    @Accessor("nbt")
    NbtCompound getNbt();
}
