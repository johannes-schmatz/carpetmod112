package carpet.mixin.breedingMountingDisabled;

import net.minecraft.block.Blocks;
import net.minecraft.entity.living.mob.passive.animal.LlamaEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LlamaEntity.class)
public class LlamaEntityMixin extends AbstractDonkeyEntityMixin {
    public LlamaEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    protected boolean isValidBreedingItem(ItemStack itemstack) {
        Item item = itemstack.getItem();
        return item != Items.WHEAT && item != Item.byBlock(Blocks.HAY);
    }
}
