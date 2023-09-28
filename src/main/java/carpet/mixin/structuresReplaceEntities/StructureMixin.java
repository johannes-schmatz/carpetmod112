package carpet.mixin.structuresReplaceEntities;

import carpet.CarpetSettings;

import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.block.BlockMirror;
import net.minecraft.block.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBox;
import net.minecraft.world.gen.structure.template.StructureTemplate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(StructureTemplate.class)
public abstract class StructureMixin {
    @Shadow private BlockPos size;

    @Shadow private static Vec3d transform(Vec3d vec, BlockMirror mirrorIn, BlockRotation rotationIn) { throw new AbstractMethodError(); }

    @Inject(
            method = "placeEntities",
            at = @At("HEAD")
    )
    private void replaceEntities(World worldIn, BlockPos pos, BlockMirror mirrorIn, BlockRotation rotationIn, StructureBox aabb, CallbackInfo ci) {
        if (!CarpetSettings.structuresReplaceEntities) return;
        Box box = new Box(pos, new BlockPos(transform((new Vec3d(pos.add(size))), mirrorIn, rotationIn)));
        List<Entity> entities = worldIn.getEntities(Entity.class, box, entity -> !(entity instanceof PlayerEntity));
        if (!entities.isEmpty()) System.out.println("Killing entities because of structure block paste");
        for (Entity e : entities) {
            System.out.println(e);
            e.discard();
        }
    }
}
