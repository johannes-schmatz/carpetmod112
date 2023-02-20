package carpet.helpers;

import net.minecraft.inventory.slot.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ItemAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Class provided by Skyrising ported to 1.12.2 by Xcom and DeadlyMC
 *
 * Auto crafting table tile entity class enabling autocrafting. When carpet rule enabled that tile
 * crafting table turns into a automatic crafting table where it can be used to automatically craft items.
 */
public class ContainerAutoCraftingTable extends CraftingScreenHandler {
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
                this.addSlot(new Slot(this.tileEntity, x + y * 3 + 1, 30 + x * 18, 17 + y * 18));
            }
        }

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 142));
        }
    }

    @Override
    public ItemStack method_3252(int slotId, int dragType, ItemAction clickTypeIn, PlayerEntity player) {
        try {
            tileEntity.setPlayer(player);
            return super.method_3252(slotId, dragType, clickTypeIn, player);
        } finally {
            tileEntity.setPlayer(null);
        }
    }

    @Override
    public void onContentChanged(Inventory inv) {
        if (this.player instanceof ServerPlayerEntity) {
            ServerPlayNetworkHandler netHandler = ((ServerPlayerEntity) this.player).networkHandler;
            netHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, 0, this.tileEntity.getInvStack(0)));
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int slot) {
        if (slot == 0) {
            ItemStack before = this.tileEntity.getInvStack(0).copy();
            ItemStack current = before.copy();
            if (!this.insertItem(current, 10, 46, true)) {
                return ItemStack.EMPTY;
            }
            tileEntity.setPlayer(player);
            this.tileEntity.takeInvStack(0, before.getCount() - current.getCount());
            tileEntity.setPlayer(null);
            return this.tileEntity.getInvStack(0);
        }
        return super.transferSlot(player, slot);
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

    private class OutputSlot extends Slot {
        OutputSlot(Inventory inv) {
            super(inv, 0, 124, 35);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        protected void method_13644(int amount) {
            ContainerAutoCraftingTable.this.tileEntity.takeInvStack(0, amount);
        }
    }

    public CraftingInventory getInventoryCrafting() {
        return tileEntity.inventory;
    }
}