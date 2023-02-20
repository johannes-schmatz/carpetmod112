package carpet.mixin.autoCraftingTable;

import carpet.CarpetSettings;
import carpet.helpers.CraftingTableBlockEntity;
import carpet.mixin.accessors.CraftingInventoryAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import org.jetbrains.annotations.Nullable;

@Mixin(CraftingTableBlock.class)
public class CraftingTableBlockMixin extends Block implements BlockEntityProvider {
    protected CraftingTableBlockMixin(Material materialIn) {
        super(materialIn);
    }

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)V"
            )
    )
    private void displayGui(PlayerEntity player, NamedScreenHandlerFactory gui, World world, BlockPos pos) {
        CraftingTableBlockEntity te = getTileEntity(world, pos);
        player.openHandledScreen(te != null ? te : gui);
    }

    @Override
    public boolean hasBlockEntity() {
        return CarpetSettings.autocrafter;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(World world, int meta) {
        return new CraftingTableBlockEntity();
    }

    @Override
    public boolean method_11577(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        CraftingTableBlockEntity te = getTileEntity(world, pos);
        if (te == null) return 0;
        int count = 0;
        for (ItemStack stack : ((CraftingInventoryAccessor) te.inventory).getStacks()) {
            if (!stack.isEmpty()) count++;
        }
        return (count * 15) / 9;
    }

    @Override
    public void onBreaking(World world, BlockPos pos, BlockState state) {
        // Maybe also check for some carpet rule
        if (hasBlockEntity()) {
            CraftingTableBlockEntity tileEntity = getTileEntity(world, pos);
            if (tileEntity != null) {
                tileEntity.dropContent(world, pos);
                world.updateHorizontalAdjacent(pos, this);
            }
        }
        world.removeBlockEntity(pos);
        super.onBreaking(world, pos, state);
    }

    /**
     * Tries to get the crafting table's block entity when auto crafting is enabled, otherwise null
     * @param world The world
     * @param pos The position of the crafting table
     * @return the block entity or null if auto crafting is disabled or none was found
     */
    @Nullable
    private CraftingTableBlockEntity getTileEntity(World world, BlockPos pos) {
        if (!hasBlockEntity()) return null;
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof CraftingTableBlockEntity) return (CraftingTableBlockEntity) te;
        return null;
    }
}
