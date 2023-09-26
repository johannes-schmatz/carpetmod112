package carpet.mixin.sleepingThreshold;

import carpet.CarpetSettings;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

    protected ServerWorldMixin(WorldStorage storage, WorldData data, Dimension dimension, Profiler profiler,
            boolean isClient) {
        super(storage, data, dimension, profiler, isClient);
    }

    @Redirect(
            method = "updateSleepingPlayers",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I",
                    remap = false
            )
    )
    private int getPlayerListSize(List<PlayerEntity> list) {
        return CarpetSettings.sleepingThreshold < 100 ? 0 : list.size();
    }

    @Inject(
            method = "canSkipNight",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;iterator()Ljava/util/Iterator;",
                    remap = false
            ),
            cancellable = true
    )
    private void sleepingThreshold(CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.sleepingThreshold < 100) {
            int players = 0;
            int sleeping = 0;
            for (PlayerEntity player : this.players) {
                if (player.isSpectator()) continue;
                players++;
                if (player.isSleptEnough()) sleeping++;
            }
            cir.setReturnValue(players == 0 || CarpetSettings.sleepingThreshold * players <= sleeping * 100);
        }
    }
}
