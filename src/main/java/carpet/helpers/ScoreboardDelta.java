package carpet.helpers;

import carpet.CarpetServer;
import carpet.utils.extensions.ExtendedScore;

import java.util.Collection;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.server.MinecraftServer;

public class ScoreboardDelta {
	public static void update() {
		MinecraftServer server = CarpetServer.getMinecraftServer();
		for (int i = 0; i < 2; i++) {
			Scoreboard scoreboard = server.getWorld(0).getScoreboard();
			ScoreboardObjective objective = scoreboard.getDisplayObjective(i);
			Collection<ScoreboardScore> list = scoreboard.getScores(objective);

			for (ScoreboardScore s : list) {
				((ExtendedScore) s).computeScoreDelta();
				s.getScoreboard().onScoreUpdated(s);
				if (((ExtendedScore) s).getScorePointsDelta() == 0) {
					s.getScoreboard().onScoresRemoved(s.getOwner());
				}
			}
		}
	}

	public static void resetScoreboardDelta() {
		MinecraftServer server = CarpetServer.getMinecraftServer();
		for (int i = 0; i < 2; i++) {
			Scoreboard scoreboard = server.getWorld(0).getScoreboard();
			ScoreboardObjective objective = scoreboard.getDisplayObjective(i);
			Collection<ScoreboardScore> list = scoreboard.getScores(objective);

			for (ScoreboardScore s : list) {
				((ExtendedScore) s).computeScoreDelta();
				s.getScoreboard().onScoreUpdated(s);
			}
		}
	}
}
