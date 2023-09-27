package carpet.helpers;


import carpet.mixin.accessors.CraftingInventoryAccessor;
import carpet.mixin.accessors.HopperBlockEntityAccessor;
import com.google.common.collect.Lists;

import net.minecraft.block.entity.InventoryBlockEntity;
import net.minecraft.crafting.CraftingManager;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.inventory.menu.InventoryMenu;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.crafting.recipe.CraftingRecipe;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class provided by Skyrising ported to 1.12.2 by Xcom and DeadlyMC
 * <p>
 * Auto crafting table tile entity class enabling autocrafting. When carpet rule enabled that tile crafting table turns into a automatic crafting table where it
 * can be used to automatically craft items.
 */
public class CraftingTableBlockEntity extends InventoryBlockEntity implements SidedInventory {
	private static final int[] OUTPUT_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	private static final int[] INPUT_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
	public CraftingInventory inventory = new CraftingInventory(null, 3, 3);
	public ItemStack output = ItemStack.EMPTY;
	private List<ContainerAutoCraftingTable> openContainers = new ArrayList<>();
	private int amountCrafted = 0;
	private PlayerEntity player;

    /*
    public TileEntityCraftingTable() {  //this(BlockEntityType.BARREL);
        this(TYPE);
    }
    */

	public CraftingTableBlockEntity() {
		super();
	}

	public static void init() {
	} // registers BE type

	public static boolean checkIfCanCraft(Inventory source, Inventory destination, ItemStack itemstack) {
		if (destination instanceof CraftingTableBlockEntity && !itemstack.isEmpty()) {
			int i = source.getSize();
			for (int j = 0; j < i; ++j) {
				if (source.getStack(j).isEmpty()) {
					return false;
				}
				if (HopperBlockEntityAccessor.invokeCanMergeItems(itemstack, source.getStack(j))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		InventoryHelper.toNbt(tag, ((CraftingInventoryAccessor) inventory).getStacks());
		tag.put("Output", output.writeNbt(new NbtCompound()));
		return tag;
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		InventoryHelper.fromNbt(tag, ((CraftingInventoryAccessor) inventory).getStacks());
		this.output = new ItemStack(tag.getCompound("Output"));
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public Text getDisplayName() {
		return new TranslatableText("container.crafting");
	}

	// Not sure about this one so left it commented
	@Override
	public InventoryMenu createMenu(PlayerInventory playerInventory, PlayerEntity playerIn) {
		ContainerAutoCraftingTable container = new ContainerAutoCraftingTable(playerInventory, this, this.world, this.pos);
		((CraftingInventoryAccessor) inventory).setContainer(container);
		this.openContainers.add(container);
		return container;
	}

	@Override
	public String getMenuType() {
		return "minecraft:crafting_table";
	}

	@Override
	public int[] getSlots(Direction dir) {
		if (dir == Direction.DOWN && (!output.isEmpty() || getCurrentRecipe().isPresent())) return OUTPUT_SLOTS;
		return INPUT_SLOTS;
	}

	@Override
	public boolean canHopperAddStack(int slot, ItemStack stack, Direction dir) {
		return slot > 0 && getStack(slot).isEmpty();
	}

	@Override
	public boolean canHopperRemoveStack(int slot, ItemStack stack, Direction dir) {
		if (slot == 0) return !output.isEmpty() || getCurrentRecipe().isPresent();
		return true;
	}

	@Override
	public boolean canSetStack(int slot, ItemStack stack) {
		return slot != 0;
	}

	@Override
	public int getData(int id) {
		return 0;
	}

	@Override
	public void setData(int id, int value) {

	}

	@Override
	public int getDataCount() {
		return 0;
	}

	@Override
	public int getSize() {
		return 10;
	}

	@Override
	public boolean isEmpty() {
		return inventory.isEmpty() && output.isEmpty();
	}

	@Override
	public ItemStack getStack(int slot) {
		if (slot > 0) return this.inventory.getStack(slot - 1);
		if (!output.isEmpty()) return output;
		Optional<CraftingRecipe> recipe = getCurrentRecipe();
		return recipe.map(r -> r.apply(inventory)).orElse(ItemStack.EMPTY);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		if (slot == 0) {
			if (output.isEmpty()) {
				amountCrafted += amount;
				output = craft(this.player);
			}
			return output.split(amount);
		}
		return InventoryHelper.split(((CraftingInventoryAccessor) inventory).getStacks(), slot - 1, amount);
	}

	@Override
	public ItemStack removeStackQuietly(int slot) {
		if (slot == 0) {
			Thread.dumpStack();
			ItemStack output = this.output;
			this.output = ItemStack.EMPTY;
			return output;
		}
		return this.inventory.removeStackQuietly(slot - 1);
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		if (slot == 0) {
			output = stack;
			return;
		}
		((CraftingInventoryAccessor) inventory).getStacks().set(slot - 1, stack);
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		for (ContainerAutoCraftingTable c : openContainers) {
			c.onContentChanged(this);
		}
	}

	@Override
	public boolean isValid(PlayerEntity var1) {
		return true;
	}

	@Override
	public void onOpen(PlayerEntity player) {

	}

	@Override
	public void onClose(PlayerEntity player) {

	}

	@Override
	public void clear() {
		this.inventory.clear();
	}

	private Optional<CraftingRecipe> getCurrentRecipe() {
		if (this.world == null) return Optional.empty();
		return Optional.ofNullable(CraftingManager.getRecipe(inventory, this.world));
	}

	protected void onCrafting(PlayerEntity player, CraftingRecipe irecipe, ItemStack stack) {
		if (player == null) {
			return;
		}
		if (this.amountCrafted > 0) {
			stack.onResult(player.world, player, this.amountCrafted);
		}

		this.amountCrafted = 0;

		if (irecipe != null && !irecipe.isSpecial()) {
			player.m_5474670(Lists.newArrayList(irecipe));
		}
	}

	public ItemStack craft(PlayerEntity player) {
		if (this.world == null) return ItemStack.EMPTY;
		Optional<CraftingRecipe> optionalRecipe = getCurrentRecipe();
		if (!optionalRecipe.isPresent()) return ItemStack.EMPTY;
		CraftingRecipe recipe = optionalRecipe.get();
		ItemStack stack = recipe.apply(this.inventory);
		onCrafting(player, recipe, stack);
		DefaultedList<ItemStack> remaining = recipe.getRemainder(this.inventory);

		for (int i = 0; i < remaining.size(); ++i) {
			ItemStack itemstack = this.inventory.getStack(i);
			ItemStack itemstack1 = remaining.get(i);

			if (!itemstack.isEmpty()) {
				this.removeStack(i + 1, 1);
				itemstack = this.inventory.getStack(i);
			}

			if (!itemstack1.isEmpty()) {
				if (itemstack.isEmpty()) {
					this.setStack(i + 1, itemstack1);
				} else if (ItemStack.matchesNbt(itemstack, stack) && ItemStack.matches(itemstack, itemstack1)) {
					itemstack1.increase(itemstack.getSize());
					this.setStack(i + 1, itemstack1);
				} else {
					InventoryUtils.dropStack(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), itemstack1);
				}
			}
		}
		return stack;
	}

	public void onContainerClose(ContainerAutoCraftingTable container) {
		this.openContainers.remove(container);
	}

	public void dropContent(World worldIn, BlockPos pos) {
		InventoryUtils.dropContents(worldIn, pos, inventory);
		InventoryUtils.dropStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), output);
	}

	public void setPlayer(PlayerEntity player) {
		this.player = player;
	}
}