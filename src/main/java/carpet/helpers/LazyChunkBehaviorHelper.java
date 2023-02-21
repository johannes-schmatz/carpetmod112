package carpet.helpers;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;


public class LazyChunkBehaviorHelper {
	private static final ArrayList<Chunk> lazyProcessingChunks = new ArrayList<>(); // TODO: store this?

	public static void listLazyChunks(CommandSource sender){
		if(!lazyProcessingChunks.isEmpty()) {
			for (Chunk chunk : lazyProcessingChunks) {
				Text text =
						new LiteralText("Chunk " + chunk.chunkX + ", " + chunk.chunkZ + " in world " + chunk.getWorld().dimension.getDimensionType());
				text.getStyle().setFormatting(Formatting.GREEN);
				sender.sendMessage(text);
			}

		}
		else{
			Text text = new LiteralText("No chunks to list." );
			text.getStyle().setFormatting(Formatting.GREEN);
			sender.sendMessage(text);
		}
	}
	public static void removeAll(){
		lazyProcessingChunks.clear();
	}

	public static boolean addLazyChunk(Chunk chunk){
		if(!containsLazyChunk(chunk)) {
			lazyProcessingChunks.add(chunk);
			return true;
		}
		return false;
	}
	public static boolean removeLazyChunk(Chunk chunk){
		if(containsLazyChunk(chunk)) {
			lazyProcessingChunks.remove(chunk);
			return true;
		}
		return false;
	}
	public static boolean containsLazyChunk(Chunk chunk){
		if(lazyProcessingChunks.isEmpty())
			return false;
		return lazyProcessingChunks.contains(chunk);
	}

	public static boolean shouldUpdate(Entity entityIn) {
		World world = entityIn.getWorld();
		return shouldUpdate(world, entityIn.getBlockPos());
	}

	public static boolean shouldUpdate(World worldIn, BlockPos pos) {
		return shouldUpdate(worldIn, new ChunkPos(pos.getX()>> 4,pos.getZ()>> 4));
	}

	public static boolean shouldUpdate(World worldIn, ChunkPos pos) {
		Chunk chunk = worldIn.getChunk(pos.x, pos.z);
		return !containsLazyChunk(chunk);
	}
}