package carpet.helpers;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PortalHelper {
	public static boolean playerHoldsObsidian(PlayerEntity playerIn) {
        ItemStack mainHand = playerIn.getMainHandStack();

        if (mainHand.isEmpty()) return false;

        Item item = mainHand.getItem();

        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();

            return block == Blocks.OBSIDIAN;
        } else {
            return false;
        }
	}
}
