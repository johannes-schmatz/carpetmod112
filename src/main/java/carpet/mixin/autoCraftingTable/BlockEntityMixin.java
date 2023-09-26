package carpet.mixin.autoCraftingTable;

import carpet.helpers.CraftingTableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
    @Shadow private static void register(String id, Class<? extends BlockEntity> clazz) {
        throw new AbstractMethodError();
    }

    @Inject(
            method = "<clinit>",
            at = @At("RETURN")
    )
    private static void registerAutoCraftingTable(CallbackInfo ci) {
        register("crafting_table", CraftingTableBlockEntity.class);
    }
}
