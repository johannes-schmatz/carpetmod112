package carpet.mixin.betterStatistics;

import carpet.CarpetServer;
import carpet.helpers.StatHelper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.criterion.ScoreboardCriterion;
import net.minecraft.server.command.ScoreboardCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScoreboardCommand.class)
public class ScoreboardCommandMixin {
    @Redirect(
            method = "addObjective",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/scoreboard/Scoreboard;createObjective(Ljava/lang/String;Lnet/minecraft/scoreboard/criterion/ScoreboardCriterion;)Lnet/minecraft/scoreboard/ScoreboardObjective;"
            )
    )
    private ScoreboardObjective initalizeScores(Scoreboard scoreboard, String name, ScoreboardCriterion criterion) {
        ScoreboardObjective score = scoreboard.createObjective(name, criterion);
        StatHelper.initialize(scoreboard, CarpetServer.getMinecraftServer(), score);
        return score;
    }
}
