package carpet.helpers;


import carpet.mixin.accessors.CraftingInventoryAccessor;
import carpet.mixin.accessors.HopperBlockEntityAccessor;
import com.google.common.collect.Lists;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeDispatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.class_2960;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class provided by Skyrising ported to 1.12.2 by Xcom and DeadlyMC
 *
 * Auto crafting table tile entity class enabling autocrafting. When carpet rule enabled that tile
 * crafting table turns into a automatic crafting table where it can be used to automatically craft items.
 */
public class CraftingTableBlockEntity extends LockableContainerBlockEntity implements SidedInventory
{
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

    public CraftingTableBlockEntity()
    {
        super();
    }

    public static void init()
    {
    } // registers BE type

    public static boolean checkIfCanCraft(Inventory source, Inventory destination, ItemStack itemstack) {
        if (destination instanceof CraftingTableBlockEntity && !itemstack.isEmpty()) {
            int i = source.getInvSize();
            for (int j = 0; j < i; ++j) {
                if(source.getInvStack(j).isEmpty()){
                    return false;
                }
                if(HopperBlockEntityAccessor.invokeCanMergeItems(itemstack, source.getInvStack(j))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public NbtCompound toNbt(NbtCompound tag)
    {
        super.toNbt(tag);
        class_2960.method_13923(tag, ((CraftingInventoryAccessor) inventory).getStacks());
        tag.put("Output", output.toNbt(new NbtCompound()));
        return tag;
    }

    @Override
    public void fromNbt(NbtCompound tag)
    {
        super.fromNbt(tag);
        class_2960.method_13927(tag, ((CraftingInventoryAccessor) inventory).getStacks());
        this.output = new ItemStack(tag.getCompound("Output"));
    }

    @Override
    public String getTranslationKey() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public Text getName()
    {
        return new TranslatableText("container.crafting");
    }

    // Not sure about this one so left it commented
    @Override
    public ScreenHandler createScreenHandler(PlayerInventory playerInventory, PlayerEntity playerIn)
    {
        ContainerAutoCraftingTable container = new ContainerAutoCraftingTable(playerInventory, this, this.world, this.pos);
        ((CraftingInventoryAccessor) inventory).setContainer(container);
        this.openContainers.add(container);
        return container;
    }

    @Override
    public String getId()
    {
        return "minecraft:crafting_table";
    }

    @Override
    public int[] getAvailableSlots(Direction dir)
    {
        if (dir == Direction.DOWN && (!output.isEmpty() || getCurrentRecipe().isPresent()))
            return OUTPUT_SLOTS;
        return INPUT_SLOTS;
    }

    @Override
    public boolean canInsertInvStack(int slot, ItemStack stack, Direction dir)
    {
        return slot > 0 && getInvStack(slot).isEmpty();
    }

    @Override
    public boolean canExtractInvStack(int slot, ItemStack stack, Direction dir)
    {
        if (slot == 0)
            return !output.isEmpty() || getCurrentRecipe().isPresent();
        return true;
    }

    @Override
    public boolean isValidInvStack(int slot, ItemStack stack)
    {
        return slot != 0;
    }

    @Override
    public int getProperty(int id) {
        return 0;
    }

    @Override
    public void setProperty(int id, int value) {

    }

    @Override
    public int getProperties() {
        return 0;
    }

    @Override
    public int getInvSize()
    {
        return 10;
    }

    @Override
    public boolean isEmpty()
    {
        return inventory.isEmpty() && output.isEmpty();
    }

    @Override
    public ItemStack getInvStack(int slot)
    {
        if (slot > 0)
            return this.inventory.getInvStack(slot - 1);
        if (!output.isEmpty())
            return output;
        Optional<RecipeType> recipe = getCurrentRecipe();
        return recipe.map(r -> r.getResult(inventory)).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack takeInvStack(int slot, int amount)
    {
        if (slot == 0)
        {
            if (output.isEmpty())
            {
                amountCrafted += amount;
                output = craft(this.player);
            }
            return output.split(amount);
        }
        return class_2960.method_13926(((CraftingInventoryAccessor) inventory).getStacks(), slot - 1, amount);
    }

    @Override
    public ItemStack removeInvStack(int slot)
    {
        if (slot == 0)
        {
            Thread.dumpStack();
            ItemStack output = this.output;
            this.output = ItemStack.EMPTY;
            return output;
        }
        return this.inventory.removeInvStack(slot - 1);
    }

    @Override
    public void setInvStack(int slot, ItemStack stack)
    {
        if (slot == 0)
        {
            output = stack;
            return;
        }
        ((CraftingInventoryAccessor) inventory).getStacks().set(slot - 1, stack);
    }

    @Override
    public int getInvMaxStackAmount() {
        return 64;
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        for (ContainerAutoCraftingTable c : openContainers){
            c.onContentChanged(this);
        }
    }

    @Override
    public boolean canPlayerUseInv(PlayerEntity var1)
    {
        return true;
    }

    @Override
    public void onInvOpen(PlayerEntity player) {

    }

    @Override
    public void onInvClose(PlayerEntity player) {

    }

    @Override
    public void clear()
    {
        this.inventory.clear();
    }

    private Optional<RecipeType> getCurrentRecipe()
    {
        if (this.world == null)
            return Optional.empty();
        return Optional.ofNullable(RecipeDispatcher.method_14262(inventory, this.world));
    }

    protected void onCrafting(PlayerEntity player, RecipeType irecipe, ItemStack stack)
    {
        if(player == null){
            return;
        }
        if (this.amountCrafted > 0)
        {
            stack.onCraft(player.world, player, this.amountCrafted);
        }

        this.amountCrafted = 0;

        if (irecipe != null && !irecipe.method_14251())
        {
            player.method_14154(Lists.newArrayList(irecipe));
        }
    }

    public ItemStack craft(PlayerEntity player)
    {
        if (this.world == null) return ItemStack.EMPTY;
        Optional<RecipeType> optionalRecipe = getCurrentRecipe();
        if (!optionalRecipe.isPresent()) return ItemStack.EMPTY;
        RecipeType recipe = optionalRecipe.get();
        ItemStack stack = recipe.getResult(this.inventory);
        onCrafting(player, recipe, stack);
        DefaultedList<ItemStack> remaining = recipe.method_13670(this.inventory);

        for (int i = 0; i < remaining.size(); ++i)
        {
            ItemStack itemstack = this.inventory.getInvStack(i);
            ItemStack itemstack1 = remaining.get(i);

            if (!itemstack.isEmpty())
            {
                this.takeInvStack(i + 1, 1);
                itemstack = this.inventory.getInvStack(i);
            }

            if (!itemstack1.isEmpty())
            {
                if (itemstack.isEmpty())
                {
                    this.setInvStack(i + 1, itemstack1);
                }
                else if (ItemStack.equalsIgnoreDamage(itemstack, stack) && ItemStack.equalsAll(itemstack, itemstack1))
                {
                    itemstack1.increment(itemstack.getCount());
                    this.setInvStack(i + 1, itemstack1);
                }else{
                    ItemScatterer.spawnItemStack(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), itemstack1);
                }
            }
        }
        return stack;
    }

    public void onContainerClose(ContainerAutoCraftingTable container)
    {
        this.openContainers.remove(container);
    }

    public void dropContent(World worldIn, BlockPos pos){
        ItemScatterer.spawn(worldIn, pos, inventory);
        ItemScatterer.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), output);
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }
}