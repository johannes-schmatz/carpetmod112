package carpet.mixin.duplicationFixItemFrame;

import carpet.CarpetSettings;
import net.minecraft.entity.decoration.DecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.world.Gamerules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin extends DecorationEntity {
    public ItemFrameEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(
            method = "dropItemOrItemFrame",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/Gamerules;getBoolean(Ljava/lang/String;)Z"
            )
    )
    private boolean checkDead(Gamerules gameRules, String name) {
        return gameRules.getBoolean(name) && (!CarpetSettings.duplicationFixItemFrame || !removed);
    }
}
