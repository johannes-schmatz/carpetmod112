package redstone.multimeter.helper;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import redstone.multimeter.block.PowerSource;
import redstone.multimeter.server.Multimeter;
import redstone.multimeter.server.MultimeterServer;

public class BlockChestHelper {

	public static boolean isTrapped(ChestBlock chest) {
		return chest.field_12621 == ChestBlock.Type.TRAP;
	}

	public static boolean isTrapped(ChestBlockEntity chest) {
		return chest.method_4806() == ChestBlock.Type.TRAP;
	}

	public static int getPower(World world, BlockPos pos, BlockState state) {
    	BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof ChestBlockEntity) {
        	return getPowerFromViewerCount(((ChestBlockEntity)blockEntity).viewerCount);
        }

        return PowerSource.MIN_POWER;
	}

	public static int getPowerFromViewerCount(int viewerCount) {
		return MathHelper.clamp(viewerCount, PowerSource.MIN_POWER, PowerSource.MAX_POWER);
	}

	public static void onInvOpenOrClosed(ChestBlockEntity chest, boolean open) {
		if (CarpetSettings.redstoneMultimeter && isTrapped(chest)) {
			ServerWorld world = (ServerWorld)chest.getEntityWorld();
			BlockPos pos = chest.getPos();

			MultimeterServer server = WorldHelper.getMultimeterServer();
			Multimeter multimeter = server.getMultimeter();

			int viewerCount = chest.viewerCount;
			int oldViewerCount = open ? viewerCount - 1 : viewerCount + 1;

			int oldPower = BlockChestHelper.getPowerFromViewerCount(oldViewerCount);
			int newPower = BlockChestHelper.getPowerFromViewerCount(viewerCount);

			multimeter.logPowerChange(world, pos, oldPower, newPower);
			multimeter.logActive(world, pos, newPower > PowerSource.MIN_POWER);
        }
    }
}
