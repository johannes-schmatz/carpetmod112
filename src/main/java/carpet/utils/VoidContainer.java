package carpet.utils;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.menu.InventoryMenu;

public class VoidContainer extends InventoryMenu {
	public VoidContainer() {
		super();
	}

	@Override
	public boolean isValid(PlayerEntity player) {
		return false;
	}

	@Override
	public void onContentChanged(Inventory inv) {

	}
}