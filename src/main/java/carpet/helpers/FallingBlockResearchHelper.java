package carpet.helpers;

import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;

import java.util.ArrayDeque;
import java.util.Deque;

public class FallingBlockResearchHelper {
	public static long start_time = 0;
	public static long[] end_times = new long[3];

	public static Deque<Time> lastTimes = new ArrayDeque<>();
	public static void start() {
		start_time = System.nanoTime();
	}

	public static void end(ServerWorld world) {
		int i = toId(world);
		end_times[i] = System.nanoTime();
		if (i == 2) {
			saveTime();
		}
	}

	public static void saveTime() {
		if (lastTimes.size() > 100_000) {
			lastTimes.removeFirst();
		}
		lastTimes.add(new Time(start_time, end_times));
	}

	public static void log(ServerPlayerEntity player) {
		long e1 = 0;
		long e2 = 0;
		long e3 = 0;
		for (Time t : lastTimes) {
			player.sendMessage(new LiteralText(t.toString()));
			e1 += t.elapsedOverworld;
			e2 += t.elapsedNether;
			e3 += t.elapsedEnd;
		}
		int s = lastTimes.size();
		e1 /= s;
		e2 /= s;
		e3 /= s;
		player.sendMessage(new LiteralText("avg: " + e1 + ", " + e2 + ", " + e3));

		long x1 = 0;
		long x2 = 0;
		long x3 = 0;
		for (Time t : lastTimes) {
			x1 += (t.elapsedOverworld - e1) * (t.elapsedOverworld - e1);
			x2 += (t.elapsedNether - e2) * (t.elapsedNether - e2);
			x3 += (t.elapsedEnd - e3) * (t.elapsedEnd - e3);
		}

		double c1 = Math.sqrt((double) x1 / (s - 1));
		double c2 = Math.sqrt((double) x2 / (s - 1));
		double c3 = Math.sqrt((double) x3 / (s - 1));

		player.sendMessage(new LiteralText("std dev: " + c1 + ", " + c2 + ", " + c3));
	}

	public static int toId(ServerWorld world) {
		int i = world.dimension.getType().getId();
		if (i == -1) return 1;
		if (i == 1) return 2;
		return i;
	}

	public static class Time {
		public final long elapsedOverworld;
		public final long elapsedNether;
		public final long elapsedEnd;
		public Time(long start, long[] ends) {
			this.elapsedOverworld = ends[0] - start;
			this.elapsedNether = ends[1] - start;
			this.elapsedEnd = ends[2] - start;
		}

		@Override
		public String toString() {
			return "Time{" +
					"elapsedOverworld=" + elapsedOverworld +
					", elapsedNether=" + elapsedNether +
					", elapsedEnd=" + elapsedEnd +
					'}';
		}
	}
}
