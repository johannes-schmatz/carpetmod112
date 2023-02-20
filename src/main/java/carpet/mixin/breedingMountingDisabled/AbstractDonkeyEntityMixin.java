package carpet.mixin.breedingMountingDisabled;

import carpet.CarpetSettings;

import net.minecraft.class_3135;
import net.minecraft.class_3136;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(class_3135.class)
public class AbstractDonkeyEntityMixin extends class_3136 {
    public AbstractDonkeyEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(
            method = "interactMob",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_3135;method_14003(Lnet/minecraft/entity/player/PlayerEntity;)V"
            )
    )
    private void mountIfNotBreeding(class_3135 abstractChestHorse, PlayerEntity player, PlayerEntity playerAgain, Hand hand) {
        if (CarpetSettings.breedingMountingDisabled && this.isValidBreedingItem(playerAgain.getStackInHand(hand))) return;
        this.method_14003(player);
    }

    protected boolean isValidBreedingItem(ItemStack stack) {
        return true;
    }
}
