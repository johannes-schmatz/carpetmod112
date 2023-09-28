package carpet.mixin.persistentParrots;

import carpet.CarpetSettings;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow public PlayerAbilities abilities;
    @Shadow protected abstract void dropShoulderEntities();
    @Shadow protected abstract void dropShoulderEntity(NbtCompound tag);
    @Shadow protected abstract void setLeftShoulderData(NbtCompound tag);
    @Shadow public abstract NbtCompound getLeftShoulderData();
    @Shadow public abstract NbtCompound getRightShoulderData();
    @Shadow protected abstract void setRightShoulderData(NbtCompound tag);

    public PlayerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(
            method = "tickAI",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/World;isClient:Z",
                    ordinal = 1
            )
    )
    private boolean isClientCheck(World world) {
        return false;
    }

    @Inject(
            method = "tickAI",
            at = @At("TAIL")
    )
    private void onLivingUpdateEnd(CallbackInfo ci) {
        boolean parrots_will_drop = !CarpetSettings.persistentParrots || this.abilities.invulnerable;
        if (!this.world.isClient && ((parrots_will_drop && this.fallDistance > 0.5F) || this.isInWater() || (parrots_will_drop && this.hasVehicle())) || this.abilities.flying) {
            this.dropShoulderEntities();
        }
    }

    @Redirect(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/living/player/PlayerEntity;dropShoulderEntities()V"
            )
    )
    private void dropParrotsOnAttack(PlayerEntity entityPlayer, DamageSource source, float amount) {
        if (CarpetSettings.persistentParrots && !this.isSneaking()) {
            if (this.random.nextFloat() < amount / 15.0) {
                this.dropShoulderEntity(this.getLeftShoulderData());
                this.setLeftShoulderData(new NbtCompound());
            }
            if (this.random.nextFloat() < amount / 15.0) {
                this.dropShoulderEntity(this.getRightShoulderData());
                this.setRightShoulderData(new NbtCompound());
            }
        } else {
            // bug in carpet? (there it's missing)
            this.dropShoulderEntities();
        }
    }
}
