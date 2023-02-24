package carpet.mixin.redstoneMultimeter;

import carpet.CarpetSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import redstone.multimeter.helper.WorldHelper;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;

@Mixin(ComparatorBlockEntity.class)
public class ComparatorBlockEntityMixin extends BlockEntity {
	@Shadow private int outputSignal;

	@Inject(
			method = "setOutputSignal",
			at = @At("HEAD")
	)
	private void onSetOutputSignal(int newOutputSignal, CallbackInfo ci) {
		if (CarpetSettings.redstoneMultimeter && !world.isClient) {
			WorldHelper.getMultimeter().logPowerChange(world, pos, outputSignal, newOutputSignal);
		}
	}
}
