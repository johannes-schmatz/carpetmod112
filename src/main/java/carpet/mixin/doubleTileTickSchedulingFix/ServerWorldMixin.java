package carpet.mixin.doubleTileTickSchedulingFix;

import carpet.CarpetSettings;
import carpet.helpers.ScheduledTickFix;
import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ScheduledTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Redirect(
            method =  {
                    "hasScheduledTick",
                    "method_11489",
                    "createAndScheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V",
                    "scheduleTick"
            },
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/util/ScheduledTick"
            )
    )
    private ScheduledTick newNextTickListEntry(BlockPos pos, Block block) {
        return CarpetSettings.doubleTileTickSchedulingFix ? new ScheduledTickFix<>(pos, block) : new ScheduledTick(pos, block);
    }
}
