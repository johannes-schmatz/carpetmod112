package carpet.mixin.disablePlayerCollision;

import carpet.CarpetSettings;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    public PlayerEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    public boolean isPushable() {
        return !CarpetSettings.disablePlayerCollision;
    }
}
