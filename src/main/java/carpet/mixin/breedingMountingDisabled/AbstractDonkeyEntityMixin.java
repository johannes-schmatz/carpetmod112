package carpet.mixin.breedingMountingDisabled;

import carpet.CarpetSettings;

import net.minecraft.entity.living.mob.passive.animal.HorseBaseEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.unmapped.C_8826268;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(C_8826268.class)
public class AbstractDonkeyEntityMixin extends HorseBaseEntity {
    public AbstractDonkeyEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(
            method = "canInteract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/unmapped/C_8826268;putPlayerOnBack(Lnet/minecraft/entity/living/player/PlayerEntity;)V"
            )
    )
    private void mountIfNotBreeding(C_8826268 abstractChestHorse, PlayerEntity player, PlayerEntity playerAgain, InteractionHand hand) {
        if (CarpetSettings.breedingMountingDisabled && this.isValidBreedingItem(playerAgain.getHandStack(hand))) return;
        this.putPlayerOnBack(player);
    }

    protected boolean isValidBreedingItem(ItemStack stack) {
        return true;
    }
}
