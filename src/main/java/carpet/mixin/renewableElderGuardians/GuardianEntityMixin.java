package carpet.mixin.renewableElderGuardians;

import carpet.CarpetSettings;

import net.minecraft.entity.living.mob.hostile.ElderGuardianEntity;
import net.minecraft.entity.weather.LightningBoltEntity;
import net.minecraft.entity.living.mob.hostile.GuardianEntity;
import net.minecraft.entity.living.mob.hostile.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuardianEntity.class)
public abstract class GuardianEntityMixin extends HostileEntity {
    public GuardianEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    public void onLightningStrike(LightningBoltEntity lightningBolt) {
        if (!this.world.isClient && !this.removed && CarpetSettings.renewableElderGuardians) {
            ElderGuardianEntity elderGuardian = new ElderGuardianEntity(this.world);
            elderGuardian.refreshPositionAndAngles(this.x, this.y, this.z, this.yaw, this.pitch);
            elderGuardian.initialize(this.world.getLocalDifficulty(new BlockPos(elderGuardian)), null);
            elderGuardian.setNoAi(this.isNoAi());

            if (this.hasCustomName()) {
                elderGuardian.setCustomName(this.getCustomName());
                elderGuardian.setCustomNameVisible(this.isCustomNameVisible());
            }

            this.world.addEntity(elderGuardian);
            this.remove();
        } else {
            super.onLightningStrike(lightningBolt);
        }
    }
}
