package carpet.helpers;

import net.minecraft.inventory.menu.ActionType;
import net.minecraft.inventory.menu.CraftingTableMenu;
import net.minecraft.inventory.slot.InventorySlot;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.MenuSlotUpdateS2CPacket;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Class provided by Skyrising ported to 1.12.2 by Xcom and DeadlyMC
 * <p>
 * Auto crafting table tile entity class enabling autocrafting. When carpet rule enabled that tile crafting table turns into a automatic crafting table where it
 * can be used to automatically craft items.
 */
public class ContainerAutoCraftingTable extends CraftingTableMenu {
	private final CraftingTableBlockEntity tileEntity;
	private final PlayerEntity player;

	ContainerAutoCraftingTable(PlayerInventory playerInventory, CraftingTableBlockEntity tileEntity, World world, BlockPos pos) {
		super(playerInventory, world, pos);
		this.tileEntity = tileEntity;
		this.player = playerInventory.player;
		slots.clear();
		this.addSlot(new OutputSlot(this.tileEntity));

		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				this.addSlot(new InventorySlot(this.tileEntity, x + y * 3 + 1, 30 + x * 18, 17 + y * 18));
			}
		}

		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 9; ++x) {
				this.addSlot(new InventorySlot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
			}
		}

		for (int x = 0; x < 9; ++x) {
			this.addSlot(new InventorySlot(playerInventory, x, 8 + x * 18, 142));
		}
	}

	@Override
	public ItemStack onClickSlot(int slotId, int dragType, ActionType clickTypeIn, PlayerEntity player) {
		try {
			tileEntity.setPlayer(player);
			return super.onClickSlot(slotId, dragType, clickTypeIn, player);
		} finally {
			tileEntity.setPlayer(null);
		}
	}

	@Override
	public void onContentChanged(Inventory inv) {
		if (this.player instanceof ServerPlayerEntity) {
			ServerPlayNetworkHandler netHandler = ((ServerPlayerEntity) this.player).networkHandler;
			netHandler.sendPacket(new MenuSlotUpdateS2CPacket(this.networkId, 0, this.tileEntity.getStack(0)));
		}
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int slot) {
		if (slot == 0) {
			ItemStack before = this.tileEntity.getStack(0).copy();
			ItemStack current = before.copy();
			if (!this.moveStack(current, 10, 46, true)) {
				return ItemStack.EMPTY;
			}
			tileEntity.setPlayer(player);
			this.tileEntity.removeStack(0, before.getSize() - current.getSize());
			tileEntity.setPlayer(null);
			return this.tileEntity.getStack(0);
		}
		return super.quickMoveStack(player, slot);
	}

	@Override
	public void close(PlayerEntity player) {
		PlayerInventory playerInventory = player.inventory;
		if (!playerInventory.getCursorStack().isEmpty()) {
			player.dropItem(playerInventory.getCursorStack(), false);
			playerInventory.setCursorStack(ItemStack.EMPTY);
		}
		this.tileEntity.onContainerClose(this);
	}

	private class OutputSlot extends InventorySlot {
		OutputSlot(Inventory inv) {
			super(inv, 0, 124, 35);
		}

		@Override
		public boolean canSetStack(ItemStack stack) {
			return false;
		}

		@Override
		protected void onSwapCraft(int amount) {
			ContainerAutoCraftingTable.this.tileEntity.removeStack(0, amount);
		}
	}

	public CraftingInventory getInventoryCrafting() {
		return tileEntity.inventory;
	}
}