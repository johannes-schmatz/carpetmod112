package carpet.worldedit;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

class TileEntityBaseBlock extends BaseBlock implements TileEntityBlock {

	public TileEntityBaseBlock(int type, int data, BlockEntity tile) {
		super(type, data);
		setNbtData(NBTConverter.fromNative(copyNbtData(tile)));
	}

	private static NbtCompound copyNbtData(BlockEntity tile) {
		NbtCompound tag = new NbtCompound();
		tile.writeNbt(tag);
		return tag;
	}

}