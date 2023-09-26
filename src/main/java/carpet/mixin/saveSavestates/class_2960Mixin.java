package carpet.mixin.saveSavestates;

import carpet.CarpetSettings;
import carpet.helpers.SaveSavestatesHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DefaultedList;

@Mixin(InventoryHelper.class)
public class class_2960Mixin {
	@Inject(
			method = "toNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/util/DefaultedList;Z)Lnet/minecraft/nbt/NbtCompound;",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void saveSavestates(NbtCompound arg, DefaultedList<ItemStack> arg2, boolean bl, CallbackInfoReturnable<NbtCompound> cir) {
		if (CarpetSettings.saveSavestates) {
			SaveSavestatesHelper.trySaveItemsCompressed(arg, arg2, bl);
			cir.setReturnValue(arg);
		}
	}
}
