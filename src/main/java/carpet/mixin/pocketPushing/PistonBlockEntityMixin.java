package carpet.mixin.pocketPushing;

import carpet.CarpetSettings;

import net.minecraft.block.piston.PistonMoveBehavior;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MovingBlockEntity.class)
public abstract class PistonBlockEntityMixin extends BlockEntity {
    @Shadow public abstract Box getShape(WorldView p_184321_1_, BlockPos p_184321_2_);
    @Shadow private boolean extending;
    @Shadow private Direction facing;
    @Shadow private BlockState movedState;

    @Inject(
            method = "moveEntities",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pocketPushing(float nextProgress, CallbackInfo ci) {
        if (CarpetSettings.pocketPushing) {
            translocateCollidedEntities();
            ci.cancel();
        }
    }

    private void translocateCollidedEntities() {
        Box axisalignedbb = this.getShape(this.world, this.pos).move(this.pos);
        List<Entity> entities = this.world.getEntities((Entity) null, axisalignedbb);
        if (!entities.isEmpty()) {
            Direction facing = this.extending ? this.facing : this.facing.getOpposite();
            for (Entity entity : entities) {
                if (entity.getPistonMoveBehavior() != PistonMoveBehavior.IGNORE) {
                    double dx = 0;
                    double dy = 0;
                    double dz = 0;
                    Box box = entity.getShape();
                    if (this.movedState.getBlock() == Blocks.SLIME) {
                        switch (facing.getAxis()) {
                            case X:
                                entity.velocityX = facing.getOffsetX();
                                break;
                            case Y:
                                entity.velocityY = facing.getOffsetY();
                                break;
                            case Z:
                                entity.velocityZ = facing.getOffsetZ();
                                break;
                        }
                    }
                    switch (facing.getAxis()) {
                        case X:
                            if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                                dx = axisalignedbb.maxX - box.minX;
                            } else {
                                dx = box.maxX - axisalignedbb.minX;
                            }
                            dx = dx + 0.01D;
                            break;
                        case Y:
                            if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                                dy = axisalignedbb.maxY - box.minY;
                            } else {
                                dy = box.maxY - axisalignedbb.minY;
                            }
                            dy = dy + 0.01D;
                            break;
                        case Z:
                            if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                                dz = axisalignedbb.maxZ - box.minZ;
                            } else {
                                dz = box.maxZ - axisalignedbb.minZ;
                            }
                            dz = dz + 0.01D;
                            break;
                    }
                    entity.move(MoverType.SELF, dx * facing.getOffsetX(), dy * facing.getOffsetY(), dz * facing.getOffsetZ());
                }
            }
        }
    }
}
