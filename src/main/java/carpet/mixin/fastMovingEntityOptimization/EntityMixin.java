package carpet.mixin.fastMovingEntityOptimization;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public World world;
    @Shadow public abstract Box getShape();

    private boolean optimize;

    @Redirect(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;",
                    ordinal = 3
            )
    )
    private List<Box> fastMovingEntityOptimization(World world, Entity entity, Box box, MoverType type, double x, double y, double z) {
        if (CarpetSettings.fastMovingEntityOptimization &&
                (x > 4 || x < -4 ||
                y > 4 || y < -4 ||
                z > 4 || z < -4)) {
            optimize = true;
            return new ArrayList<>();
        } else {
            optimize = false;
            return world.getCollisions(entity, box);
        }
    }

    @Inject(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I",
                    remap = false,
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void fastMovingEntityOptimizationY(MoverType type, double x, double y, double z, CallbackInfo ci, double d10, double d11, double d1, double d2, double d3, double d4, List<Box> list1) {
        if (optimize) {
            list1.addAll(this.world.getCollisions((Entity) (Object) this, this.getShape().grow(0, y, 0)));
        }
    }

    @Inject(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I",
                    remap = false,
                    ordinal = 1
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void fastMovingEntityOptimizationX(MoverType type, double x, double y, double z, CallbackInfo ci, double d10, double d11, double d1, double d2, double d3, double d4, List<Box> list1) {
        if (optimize) {
            list1.clear();
            list1.addAll(this.world.getCollisions((Entity) (Object) this, this.getShape().grow(x, 0, 0)));
        }
    }

    @Inject(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I",
                    remap = false,
                    ordinal = 2
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void fastMovingEntityOptimizationZ(MoverType type, double x, double y, double z, CallbackInfo ci, double d10, double d11, double d1, double d2, double d3, double d4, List<Box> list1) {
        if (optimize) {
            list1.clear();
            list1.addAll(this.world.getCollisions((Entity) (Object) this, this.getShape().grow(0, 0, z)));
        }
    }
}
