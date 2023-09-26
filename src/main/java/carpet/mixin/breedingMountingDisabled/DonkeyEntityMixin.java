package carpet.mixin.breedingMountingDisabled;

import net.minecraft.unmapped.C_2970949;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(C_2970949.class)
public class DonkeyEntityMixin extends AbstractDonkeyEntityMixin {
    public DonkeyEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    protected boolean isValidBreedingItem(ItemStack itemstack) {
        Item item = itemstack.getItem();
        return item != Items.GOLDEN_CARROT && item != Items.GOLDEN_APPLE;
    }
}
