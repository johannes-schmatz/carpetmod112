package carpet.mixin.accessors;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.server.command.CloneCommand$BlockInfo")
public interface StaticCloneDataAccessor {
    @Accessor("field_12011") BlockPos getPos();
    @Accessor("field_12012") BlockState getBlockState();
    @Accessor("field_12013")
    NbtCompound getNbt();
}
