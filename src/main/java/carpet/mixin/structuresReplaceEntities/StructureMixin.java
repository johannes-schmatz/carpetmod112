package carpet.mixin.structuresReplaceEntities;

import carpet.CarpetSettings;

import net.minecraft.class_2765;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(class_2765.class)
public abstract class StructureMixin {
    @Shadow private BlockPos field_13033;

    @Shadow private static Vec3d method_11888(Vec3d vec, BlockMirror mirrorIn, BlockRotation rotationIn) { throw new AbstractMethodError(); }

    @Inject(
            method = "method_11881",
            at = @At("HEAD")
    )
    private void replaceEntities(World worldIn, BlockPos pos, BlockMirror mirrorIn, BlockRotation rotationIn, BlockBox aabb, CallbackInfo ci) {
        if (!CarpetSettings.structuresReplaceEntities) return;
        Box box = new Box(pos, new BlockPos(method_11888((new Vec3d(pos.add(field_13033))), mirrorIn, rotationIn)));
        List<Entity> entities = worldIn.getEntitiesInBox(Entity.class, box, entity -> !(entity instanceof PlayerEntity));
        if (!entities.isEmpty()) System.out.println("Killing entities because of structure block paste");
        for (Entity e : entities) {
            System.out.println(e);
            e.kill();
        }
    }
}
