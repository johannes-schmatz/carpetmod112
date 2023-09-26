package carpet.mixin.betterStatistics;

import carpet.helpers.StatSubItem;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Shadow public abstract void incrementStat(Stat stat, int amount);

    @Inject(
            method = "incrementStat",
            at = @At("RETURN")
    )
    private void onAddStat(Stat stat, int amount, CallbackInfo ci) {
        if (stat instanceof StatSubItem) {
            incrementStat(((StatSubItem) stat).getBase(), amount);
        }
    }
}
