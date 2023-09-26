package carpet.mixin.autoCraftingDropper;

import carpet.CarpetSettings;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DropperBlockEntity.class)
public class DropperBlockEntityMixin extends DispenserBlockEntity {
    @Override
    public boolean canSetStack(int slot, ItemStack stack) {
        if (CarpetSettings.autoCraftingDropper && world != null) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() == Blocks.DROPPER && world.getBlockState(pos.offset(state.get(DispenserBlock.FACING))).getBlock() == Blocks.CRAFTING_TABLE) {
                return this.getInventory().get(slot).isEmpty();
            }
        }
        return super.canSetStack(slot, stack);
    }
}
