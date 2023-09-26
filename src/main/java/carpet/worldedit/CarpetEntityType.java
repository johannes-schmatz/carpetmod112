package carpet.worldedit;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.entity.metadata.EntityType;

import net.minecraft.entity.*;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.PaintingEntity;
import net.minecraft.entity.living.ArmorStandEntity;
import net.minecraft.entity.living.mob.GolemEntity;
import net.minecraft.entity.living.mob.MobEntity;
import net.minecraft.entity.living.mob.ambient.AmbientEntity;
import net.minecraft.entity.living.mob.passive.PassiveEntity;
import net.minecraft.entity.living.mob.passive.Trader;
import net.minecraft.entity.living.mob.passive.VillagerEntity;
import net.minecraft.entity.living.mob.passive.animal.tamable.TameableEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;

class CarpetEntityType implements EntityType {

    private final Entity entity;

    public CarpetEntityType(Entity entity) {
        checkNotNull(entity); // TODO...
        this.entity = entity;
    }

    @Override
    public boolean isPlayerDerived() {
        return entity instanceof PlayerEntity;
    }

    @Override
    public boolean isProjectile() {
        return entity instanceof EnderEyeEntity || entity instanceof Dispensable;
    }

    @Override
    public boolean isItem() {
        return entity instanceof ItemEntity;
    }

    @Override
    public boolean isFallingBlock() {
        return entity instanceof FallingBlockEntity;
    }

    @Override
    public boolean isPainting() {
        return entity instanceof PaintingEntity;
    }

    @Override
    public boolean isItemFrame() {
        return entity instanceof ItemFrameEntity;
    }

    @Override
    public boolean isBoat() {
        return entity instanceof BoatEntity;
    }

    @Override
    public boolean isMinecart() {
        return entity instanceof MinecartEntity;
    }

    @Override
    public boolean isTNT() {
        return entity instanceof PrimedTntEntity;
    }

    @Override
    public boolean isExperienceOrb() {
        return entity instanceof XpOrbEntity;
    }

    @Override
    public boolean isLiving() {
        return entity instanceof MobEntity;
    }

    @Override
    public boolean isAnimal() {
        return entity instanceof PassiveEntity;
    }

    @Override
    public boolean isAmbient() {
        return entity instanceof AmbientEntity;
    }

    @Override
    public boolean isNPC() {
        return entity instanceof VillagerEntity || entity instanceof Trader;
    }

    @Override
    public boolean isGolem() {
        return entity instanceof GolemEntity;
    }

    @Override
    public boolean isTamed() {
        return entity instanceof TameableEntity && ((TameableEntity) entity).isTamed();
    }

    @Override
    public boolean isTagged() {
        return entity instanceof MobEntity && entity.hasCustomName();
    }

    @Override
    public boolean isArmorStand() {
        return entity instanceof ArmorStandEntity;
    }
}
