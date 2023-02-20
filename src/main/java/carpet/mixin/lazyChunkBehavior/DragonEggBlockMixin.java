package carpet.mixin.lazyChunkBehavior;

import carpet.CarpetSettings;
import carpet.helpers.LazyChunkBehaviorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.DragonEggBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(DragonEggBlock.class)
public class DragonEggBlockMixin {
	@Redirect(
			method = "scheduledTick",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/block/FallingBlock;instantFall:Z"
			)
	)
	public boolean redirectInstantFall(World world, BlockPos pos) {
		if (CarpetSettings.commandLazyChunkBehavior) {
			return (!LazyChunkBehaviorHelper.shouldUpdate(world, pos)) || FallingBlock.instantFall;
		}
		return FallingBlock.instantFall;
	}
}
