package carpet.mixin.scoreboardDelta;

import carpet.utils.extensions.ExtendedScore;

import net.minecraft.network.packet.s2c.play.ScoreboardScoreS2CPacket;
import net.minecraft.scoreboard.ScoreboardScore;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScoreboardScoreS2CPacket.class)
public class ScoreboardPlayerUpdateS2CPacketMixin {
    @Redirect(
            method = "<init>(Lnet/minecraft/scoreboard/ScoreboardScore;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/scoreboard/ScoreboardScore;get()I"
            )
    )
    private int getScorePoints(ScoreboardScore score) {
        return ((ExtendedScore) score).getScorePointsDelta();
    }
}
