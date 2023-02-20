package carpet.mixin.tpAcrossDimensions;

import carpet.CarpetSettings;
import carpet.helpers.TeleportHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.TpCommand;

@Mixin(TpCommand.class)
public class TpCommandMixin {
	@Inject(
			method = "method_3279",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/entity/Entity;world:Lnet/minecraft/world/World;",
					ordinal = 0
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	public void fixDimension(MinecraftServer minecraftServer, CommandSource arg, String[] strings, CallbackInfo ci, int i, Entity lv2, Entity lv3) {
		if (CarpetSettings.tpAcrossDimensions && lv2.world != lv3.world) {
			if (lv2 instanceof ServerPlayerEntity && lv3 instanceof ServerPlayerEntity) {
				TeleportHelper.changeDimensions((ServerPlayerEntity) lv2, (ServerPlayerEntity) lv3);
			}
		}
	}
}
