package carpet.mixin.newLight;

import carpet.CarpetSettings;
import carpet.helpers.LightingHooks;
import carpet.utils.extensions.NewLightChunk;
import carpet.utils.extensions.NewLightWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.dimension.Dimension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Chunk.class)
public abstract class ChunkMixin implements NewLightChunk {
    private short[] neighborLightChecks = null;
    private short pendingNeighborLightInits;

    @Shadow @Final private World world;
    @Shadow @Final private ChunkSection[] chunkSections;
    @Shadow private boolean terrainPopulated;
    @Shadow private boolean lightPopulated;
    @Shadow @Final public int chunkX;
    @Shadow @Final public int chunkZ;
    @Shadow public abstract boolean hasDirectSunlight(BlockPos pos);
    @Shadow protected abstract void method_3911(int x, int z);

    @Inject(
            method = "calculateSkyLight",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/dimension/Dimension;isOverworld()Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void generateSkyLight(CallbackInfo ci, int top, int x, int z) {
        if (CarpetSettings.newLight) LightingHooks.fillSkylightColumn((Chunk) (Object) this, x, z);
    }

    @Redirect(
            method = "calculateSkyLight",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/dimension/Dimension;isOverworld()Z"
            )
    )
    private boolean generateSkyLightCancelVanilla(Dimension worldProvider) {
        return !CarpetSettings.newLight && worldProvider.isOverworld();
    }

    @ModifyConstant(
            method = "method_3917",
            constant = @Constant(intValue = 255)
    )
    private int noMask(int mask) {
        return CarpetSettings.newLight ? -1 : 255;
    }

    @Redirect(
            method = "method_3917",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;method_3704(IIII)V"
            )
    )
    private void markDirtyInRelightBlock(World world, int x, int z, int y1, int y2) {
        if (!CarpetSettings.newLight) world.method_3704(x, z, y1, y2);
    }

    @Redirect(
            method = "method_3917",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/dimension/Dimension;isOverworld()Z",
                    ordinal = 0
            )
    )
    private boolean relightSkyLight(Dimension worldProvider, int x, int y, int z) {
        boolean hasSkylight = worldProvider.isOverworld();
        if (!hasSkylight || !CarpetSettings.newLight) return hasSkylight;
        LightingHooks.relightSkylightColumn(world, (Chunk) (Object) this, x, z, this.chunkX * 16 + x, this.chunkZ * 16 + z);
        return false; // cancel vanilla code
    }

    @Redirect(
            method = "method_3917",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/dimension/Dimension;isOverworld()Z",
                    ordinal = 1
            )
    )
    private boolean relightSkyLight2(Dimension worldProvider) {
        return !CarpetSettings.newLight && worldProvider.isOverworld();
    }

    @Inject(
            method = "getLightAtPos",
            at = @At("HEAD")
    )
    private void procOnGetLightFor(LightType type, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.newLight) ((NewLightWorld) world).getLightingEngine().procLightUpdates(type);
    }

    @Inject(
            method = "getLightLevel",
            at = @At("HEAD")
    )
    private void procOnGetLightSubtracted(BlockPos pos, int amount, CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.newLight) ((NewLightWorld) world).getLightingEngine().procLightUpdates();
    }

    @Redirect(
            method = "setLightAtPos",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;calculateSkyLight()V"
            )
    )
    private void newLightGenerateSkylightMap(Chunk chunk, LightType type, BlockPos pos) {
        if (CarpetSettings.newLight) {
            //Forge: generateSkylightMap produces the wrong result (See #3870)
            LightingHooks.initSkylightForSection(world, chunk, chunkSections[pos.getY() >> 4]);
        } else {
            chunk.calculateSkyLight();
        }
    }

    @Inject(
            method = "loadToWorld",
            at = @At("RETURN")
    )
    private void onOnLoad(CallbackInfo ci) {
        if (CarpetSettings.newLight) LightingHooks.onLoad(world, (Chunk) (Object) this);
    }

    @Redirect(
            method = "populate(Lnet/minecraft/server/world/ChunkGenerator;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;populate()V"
            )
    )
    private void checkLightInPopulate(Chunk chunk) {
        if (CarpetSettings.newLight) {
            this.terrainPopulated = true;
        } else {
            chunk.populate();
        }
    }

    @Redirect(
            method = "populateBlockEntities",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/chunk/Chunk;lightPopulated:Z"
            )
    )
    private boolean checkLightInOnTick(Chunk chunk) {
        return CarpetSettings.newLight || lightPopulated;
    }


    @ModifyVariable(
            method = "getBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;",
            index = 12,
            at = @At(
                    value = "STORE",
                    ordinal = 1
            )
    )
    private boolean setBlockStateInitSkylight(boolean flag, BlockPos pos) {
        if (CarpetSettings.newLight){
            //Forge: Always initialize sections properly (See #3870 and #3879)
            LightingHooks.initSkylightForSection(world, (Chunk) (Object) this, chunkSections[pos.getY() >> 4]);
            //Forge: Don't call generateSkylightMap (as it produces the wrong result; sections are initialized above). Never bypass relightBlock (See #3870)
            return false;
        }
        return flag;
    }

    //Forge: Error correction is unnecessary as these are fixed (See #3871)
    @Redirect(
            method = "getBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;method_3911(II)V"
            )
    )
    private void dontPropagateSkylightOcclusion(Chunk chunk, int x, int z) {
        if (CarpetSettings.newLight) return;
        this.method_3911(x, z);
    }

    @Override
    public short[] getNeighborLightChecks() {
        return neighborLightChecks;
    }

    @Override
    public void setNeighborLightChecks(short[] checks) {
        neighborLightChecks = checks;
    }

    @Override
    public short getPendingNeighborLightInits() {
        return pendingNeighborLightInits;
    }

    @Override
    public void setPendingNeighborLightInits(int inits) {
        pendingNeighborLightInits = (short) inits;
    }

    @Override
    public int getCachedLightFor(LightType type, BlockPos pos)
    {
        int x = pos.getX() & 15;
        int y = pos.getY();
        int z = pos.getZ() & 15;
        ChunkSection section = this.chunkSections[y >> 4];
        if (section == Chunk.EMPTY) {
            return this.hasDirectSunlight(pos) ? type.defaultValue : 0;
        }
        if (type == LightType.SKY) {
            return !this.world.dimension.isOverworld() ? 0 : section.getSkyLight(x, y & 15, z);
        }
        return type == LightType.BLOCK ? section.getBlockLight(x, y & 15, z) : type.defaultValue;
    }
}
