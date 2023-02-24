package carpet.mixin.redstoneMultimeter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import redstone.multimeter.helper.BlockChestHelper;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(ChestBlockEntity.class)
public class ChestBlockEntityMixin {
	@Inject(
			method = "onInvOpen",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/World;addBlockAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"
			)
	)
	private void onInvOpen(PlayerEntity player, CallbackInfo ci) {
		BlockChestHelper.onInvOpenOrClosed((ChestBlockEntity) ((Object) this), true);
	}

	@Inject(
			method = "onInvClose",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/World;addBlockAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V"
			)
	)
	private void onInvClose(PlayerEntity player, CallbackInfo ci) {
		BlockChestHelper.onInvOpenOrClosed((ChestBlockEntity) ((Object) this), false);
	}
}
