package carpet.mixin.persistentParrots;

import carpet.CarpetSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
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
    @Shadow protected abstract void method_14157();
    @Shadow protected abstract void method_14164(NbtCompound tag);
    @Shadow protected abstract void method_14161(NbtCompound tag);
    @Shadow public abstract NbtCompound method_14158();
    @Shadow public abstract NbtCompound method_14159();
    @Shadow protected abstract void method_14162(NbtCompound tag);

    public PlayerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(
            method = "tickMovement",
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
            method = "tickMovement",
            at = @At("TAIL")
    )
    private void onLivingUpdateEnd(CallbackInfo ci) {
        boolean parrots_will_drop = !CarpetSettings.persistentParrots || this.abilities.invulnerable;
        if (!this.world.isClient && ((parrots_will_drop && this.fallDistance > 0.5F) || this.isTouchingWater() || (parrots_will_drop && this.hasMount())) || this.abilities.flying) {
            this.method_14157();
        }
    }

    @Redirect(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;method_14157()V"
            )
    )
    private void dropParrotsOnAttack(PlayerEntity entityPlayer, DamageSource source, float amount) {
        if (CarpetSettings.persistentParrots && !this.isSneaking()) {
            if (this.random.nextFloat() < amount / 15.0) {
                this.method_14164(this.method_14158());
                this.method_14161(new NbtCompound());
            }
            if (this.random.nextFloat() < amount / 15.0) {
                this.method_14164(this.method_14159());
                this.method_14162(new NbtCompound());
            }
        } else {
            // bug in carpet? (there it's missing)
            this.method_14157();
        }
    }
}
