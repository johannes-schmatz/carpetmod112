package carpet.mixin.core;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;

@Mixin(NbtCompound.class)
public class CompoundTagMixin {
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;",
                    remap = false
            )
    )
    private HashMap<String, NbtElement> createMap() {
        return new HashMap<>(2, 1.0f);
    }
}
