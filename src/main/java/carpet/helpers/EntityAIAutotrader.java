package carpet.helpers;

import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.living.mob.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.village.trade.TradeOffer;
import net.minecraft.world.village.trade.TradeOffers;

import java.util.Iterator;
import java.util.List;

/**
 * Automatically trading villagers, the villager will pickup items from the ground and trade the items in the same order of the trade list. The order of trades
 * can be adjusted by trading in the open GUI ones where last trade is placed first.
 */
public class EntityAIAutotrader extends Goal {

	private VillagerEntity villager;
	private BlockPos emeraldBlockPosition = null;
	private int counter = 0;

	/**
	 * Main constructor
	 *
	 * @param theVillagerIn the villager that is attached to the AI task.
	 */
	public EntityAIAutotrader(VillagerEntity theVillagerIn) {
		this.villager = theVillagerIn;
	}

	/**
	 * Should excecute for all AI tasks, always true here.
	 *
	 * @return
	 */
	@Override
	public boolean canStart() {
		return true;
	}

	/**
	 * AI update task that in this case only searches for a emerald block to throw items toward it when trading.
	 */
	public void tick() {
		counter++;
		if (counter % 100 == 0) {
			findClosestEmeraldBlock();
		}
	}

	/**
	 * Finds an emerald block the trader will use to throw the items towards.
	 */
	private void findClosestEmeraldBlock() {
		World worldIn = villager.getSourceWorld();
		BlockPos villagerPos = new BlockPos(villager);
		for (BlockPos pos : BlockPos.iterateRegion(villagerPos.add(-3, -1, -3), villagerPos.add(3, 4, 3))) {
			if (worldIn.getBlockState(pos).getBlock() == Blocks.EMERALD_BLOCK) {
				emeraldBlockPosition = pos;
				return;
			}
		}
		emeraldBlockPosition = null;
	}

	/**
	 * Update task that will make the villager search for items on the ground and throw items towards.
	 *
	 * @param itemEntity
	 * @param tradeOffers
	 *
	 * @return
	 */
	public boolean updateEquipment(ItemEntity itemEntity, TradeOffers tradeOffers) {
		for (TradeOffer offer : tradeOffers) {
			if (!offer.isDisabled()) {
				ItemStack groundItems = itemEntity.getItemStack();
				ItemStack buyItem = offer.getPrimaryPayment();
				if (groundItems.getItem() == buyItem.getItem()) {
					int max = offer.getMaxUses() - offer.getUses();
					int price = buyItem.getSize();
					int gold = groundItems.getSize();
					int count = gold / price;
					if (count > max) {
						count = max;
					}

					for (int i = 0; i < count; i++) {
						villager.trade(offer);
						dropItem(offer.getResult().copy());
						groundItems.decrease(price);
					}

					return true;
				}
			}
		}
		return true;
	}

	/**
	 * Drop item call when the villager succesfully finds an item to trade.
	 *
	 * @param itemstack
	 */
	private void dropItem(ItemStack itemstack) {
		if (itemstack.isEmpty()) return;

		float f1 = villager.headYaw;
		float f2 = villager.pitch;

		if (emeraldBlockPosition != null) {
			double d0 = emeraldBlockPosition.getX() + 0.5D - villager.x;
			double d1 = emeraldBlockPosition.getY() + 1.5D - (villager.y + (double) villager.getEyeHeight());
			double d2 = emeraldBlockPosition.getZ() + 0.5D - villager.z;
			double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
			f1 = (float) (MathHelper.fastAtan2(d2, d0) * (180.0D / Math.PI)) - 90.0F;
			f2 = (float) (-(MathHelper.fastAtan2(d1, d3) * (180.0D / Math.PI)));
		}

		double d0 = villager.y - 0.30000001192092896D + (double) villager.getEyeHeight();
		ItemEntity entityitem = new ItemEntity(villager.world, villager.x, d0, villager.z, itemstack);
		float f = 0.3F;

		entityitem.velocityX = -MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * f;
		entityitem.velocityY = MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F) * f;
		entityitem.velocityZ = -MathHelper.sin(f2 * 0.017453292F) * 0.3F + 0.1F;
		entityitem.setDefaultPickUpDelay();
		villager.world.addEntity(entityitem);
	}

	/**
	 * Adds the latest villager trade the player has trade to the a list that will be used to order the trades.
	 *
	 * @param buyingList
	 * @param recipe
	 * @param sortedTradeList
	 */
	public void addToFirstList(TradeOffers buyingList, TradeOffer recipe, List<Integer> sortedTradeList) {
		int index = -1;
		for (int i = 0; i < buyingList.size(); i++) {
			TradeOffer b = buyingList.get(i);
			if (b.getPrimaryPayment().getItem().equals(recipe.getPrimaryPayment().getItem()) &&
					b.getPrimaryPayment().getItem().equals(recipe.getPrimaryPayment().getItem())) {
				index = i;
				break;
			}
		}
		if (index == -1) return;
		Iterator<Integer> iter = sortedTradeList.iterator();
		while (iter.hasNext()) {
			int i = iter.next();
			if (i == index) {
				iter.remove();
				break;
			}
		}
		sortedTradeList.add(0, index);
	}

	/**
	 * Sortes the villager trades differently then in vanilla with the last trade first.
	 *
	 * @param buyingList
	 * @param buyingListsorted
	 * @param sortedTradeList
	 */
	public void sortRepopulatedSortedList(TradeOffers buyingList, TradeOffers buyingListsorted, List<Integer> sortedTradeList) {
		if (buyingList == null) return;

		TradeOffers copy = new TradeOffers();
		copy.addAll(buyingList);
		buyingListsorted.clear();
		for (int i : sortedTradeList) {
			TradeOffer r = copy.get(i);
			buyingListsorted.add(r);
		}
		for (TradeOffer r : buyingListsorted) {
			copy.remove(r);
		}
		for (TradeOffer r : copy) {
			buyingListsorted.add(r);
		}
	}

	/**
	 * Reloads the NBT data of the sorted list for the trade order.
	 *
	 * @param nbttagcompound
	 * @param sortedTradeList
	 */
	public void setRecipiesForSaving(NbtCompound nbttagcompound, List<Integer> sortedTradeList) {
		NbtList nbttaglist = nbttagcompound.getList("Recipes", 10);

		for (int i = 0; i < nbttaglist.size(); ++i) {
			NbtCompound nbt = nbttaglist.getCompound(i);
			sortedTradeList.add(nbt.getInt("n"));
		}
	}

	/**
	 * Saves the trade list into NBT for later saving to the villager
	 *
	 * @param list
	 *
	 * @return
	 */
	public NbtCompound getRecipiesForSaving(List<Integer> list) {
		NbtCompound nbttagcompound = new NbtCompound();
		NbtList nbttaglist = new NbtList();

		for (int i = 0; i < list.size(); ++i) {
			int index = list.get(i);
			NbtCompound num = new NbtCompound();
			num.putInt("n", index);
			nbttaglist.add(num);
		}

		nbttagcompound.put("Recipes", nbttaglist);
		return nbttagcompound;
	}
}
