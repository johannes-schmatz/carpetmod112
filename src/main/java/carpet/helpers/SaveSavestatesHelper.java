package carpet.helpers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.DefaultedList;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class SaveSavestatesHelper {
	public static void trySaveItemsCompressed(NbtCompound destTag, DefaultedList<ItemStack> items, boolean saveEmpty) {
		List<Pair<Integer, ItemStack>> itemsAndSlots = new ArrayList<>(items.size());
		for (int i = 0; i < items.size(); i++) {
			itemsAndSlots.add(Pair.of(i, items.get(i)));
		}

		itemsAndSlots.sort((a, b) -> compareItems(a.getRight(), b.getRight()));

		NbtList itemsTag = new NbtList();
		for (Pair<Integer, ItemStack> itemAndSlot : itemsAndSlots) {
			int slot = itemAndSlot.getLeft();
			ItemStack item = itemAndSlot.getRight();
			if (!item.isEmpty()) {
				NbtCompound itemTag = new NbtCompound();
				itemTag.putByte("Slot", (byte) slot);
				item.writeNbt(itemTag);
				itemsTag.add(itemTag);
			}
		}

		if (!itemsTag.isEmpty() || saveEmpty) {
			destTag.put("Items", itemsTag);
		}
	}

	private static int compareItems(ItemStack a, ItemStack b) {
		int idA = Item.getId(a.getItem());
		int idB = Item.getId(b.getItem());
		if (idA != idB) {
			return Integer.compare(idA, idB);
		}

		NbtCompound tagA = a.getNbt();
		NbtCompound tagB = b.getNbt();
		if (tagA != null && tagB != null) {
			NbtList pagesA = tagA.getList("pages", 8);
			NbtList pagesB = tagB.getList("pages", 8);
			for (int page = 0; page < Math.min(pagesA.size(), pagesB.size()); page++) {
				String pageA = pagesA.getString(page);
				String pageB = pagesB.getString(page);
				int cmp = pageA.compareTo(pageB);
				if (cmp != 0) {
					return cmp;
				}
			}
		}

		return 0;
	}
}