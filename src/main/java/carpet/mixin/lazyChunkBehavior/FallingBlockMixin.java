package carpet.mixin.lazyChunkBehavior;

import carpet.CarpetSettings;
import carpet.helpers.LazyChunkBehaviorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(FallingBlock.class)
public class FallingBlockMixin {
	@Shadow public static boolean instantFall;

	@Redirect(
			method = "scheduledTick",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/block/FallingBlock;instantFall:Z"
			)
	)
	public boolean redirectInstantFall(World world, BlockPos pos) {
		if (CarpetSettings.commandLazyChunkBehavior) {
			return (!LazyChunkBehaviorHelper.shouldUpdate(world, pos)) || instantFall;
		}
		return instantFall;
	}
}
