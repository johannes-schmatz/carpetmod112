package carpet.mixin.fillUpdates;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.StructureTemplate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(StructureTemplate.class)
public class StructureMixin {
    @ModifyConstant(
            method = "place(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/structure/template/StructureProcessor;Lnet/minecraft/world/gen/structure/template/StructurePlaceSettings;I)V",
            constant = @Constant(intValue = 4)
    )
    private int changeFlags1(int flags) {
        return flags | (CarpetSettings.fillUpdates ? 0 : 1024);
    }

    @ModifyArg(
            method = "place(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/structure/template/StructureProcessor;Lnet/minecraft/world/gen/structure/template/StructurePlaceSettings;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;I)Z",
                    ordinal = 1
            ),
            index = 2
    )
    private int changeFlags2(int flags) {
        return flags | (CarpetSettings.fillUpdates ? 0 : 1024);
    }

    @Redirect(
            method = "place(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/gen/structure/template/StructureProcessor;Lnet/minecraft/world/gen/structure/template/StructurePlaceSettings;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;onBlockChanged(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Z)V"
            )
    )
    private void notifyNeighbors(World world, BlockPos pos, Block blockType, boolean updateObservers) {
        if (!CarpetSettings.fillUpdates) return;
        world.onBlockChanged(pos, blockType, updateObservers);
    }
}
