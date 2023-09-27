package carpet.helpers;

import net.minecraft.server.command.source.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Formatting;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;

public class LazyChunkBehaviorHelper {
	private static final ArrayList<WorldChunk> lazyProcessingChunks = new ArrayList<>(); // TODO: store this?

	public static void listLazyChunks(CommandSource sender) {
		if (lazyProcessingChunks.isEmpty()) {
			Text text = new LiteralText("No chunks to list.");
			text.getStyle().setColor(Formatting.GREEN);
			sender.sendMessage(text);
		} else {
			for (WorldChunk chunk : lazyProcessingChunks) {
				Text text = new LiteralText("Chunk " + chunk.chunkX + ", " + chunk.chunkZ + " in world " + chunk.getWorld().dimension.getType());
				text.getStyle().setColor(Formatting.GREEN);
				sender.sendMessage(text);
			}
		}
	}

	public static void removeAll() {
		lazyProcessingChunks.clear();
	}

	public static boolean addLazyChunk(WorldChunk chunk) {
		if (!containsLazyChunk(chunk)) {
			lazyProcessingChunks.add(chunk);
			return true;
		}
		return false;
	}

	public static boolean removeLazyChunk(WorldChunk chunk) {
		if (containsLazyChunk(chunk)) {
			lazyProcessingChunks.remove(chunk);
			return true;
		}
		return false;
	}

	public static boolean containsLazyChunk(WorldChunk chunk) {
		if (lazyProcessingChunks.isEmpty()) return false;
		return lazyProcessingChunks.contains(chunk);
	}

	public static boolean shouldUpdate(Entity entityIn) {
		World world = entityIn.getSourceWorld();
		return shouldUpdate(world, entityIn.getSourceBlockPos());
	}

	public static boolean shouldUpdate(World worldIn, BlockPos pos) {
		return shouldUpdate(worldIn, new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
	}

	public static boolean shouldUpdate(World worldIn, ChunkPos pos) {
		WorldChunk chunk = worldIn.getChunkAt(pos.x, pos.z);
		return !containsLazyChunk(chunk);
	}
}