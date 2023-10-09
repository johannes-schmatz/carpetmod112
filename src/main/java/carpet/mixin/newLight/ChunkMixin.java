package carpet.mixin.newLight;

import carpet.CarpetSettings;
import carpet.helpers.LightingHooks;
import carpet.utils.extensions.NewLightChunk;
import carpet.utils.extensions.NewLightWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WorldChunkSection;
import net.minecraft.world.dimension.Dimension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WorldChunk.class)
public abstract class ChunkMixin implements NewLightChunk {
    private short[] neighborLightChecks = null;
    private short pendingNeighborLightInits;

    @Shadow @Final private World world;
    @Shadow @Final private WorldChunkSection[] sections;
    @Shadow private boolean terrainPopulated;
    @Shadow private boolean lightPopulated;
    @Shadow @Final public int chunkX;
    @Shadow @Final public int chunkZ;
    @Shadow public abstract boolean hasSkyAccess(BlockPos pos);
    @Shadow protected abstract void queueLightUpdate(int x, int z);

    @Inject(
            method = "populateSkylight",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/dimension/Dimension;hasSkyLight()Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void generateSkyLight(CallbackInfo ci, int top, int x, int z) {
        if (CarpetSettings.newLight) LightingHooks.fillSkylightColumn((WorldChunk) (Object) this, x, z);
    }

    @Redirect(
            method = "populateSkylight",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/dimension/Dimension;hasSkyLight()Z"
            )
    )
    private boolean generateSkyLightCancelVanilla(Dimension worldProvider) {
        return !CarpetSettings.newLight && worldProvider.isOverworld();
    }

    @ModifyConstant(
            method = "resetLightAt",
            constant = @Constant(intValue = 255)
    )
    private int noMask(int mask) {
        return CarpetSettings.newLight ? -1 : 255;
    }

    @Redirect(
            method = "resetLightAt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;checkLight(IIII)V"
            )
    )
    private void markDirtyInRelightBlock(World world, int x, int z, int y1, int y2) {
        if (!CarpetSettings.newLight) world.checkLight(x, z, y1, y2);
    }

    @Redirect(
            method = "resetLightAt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/dimension/Dimension;hasSkyLight()Z",
                    ordinal = 0
            )
    )
    private boolean relightSkyLight(Dimension worldProvider, int x, int y, int z) {
        boolean hasSkylight = worldProvider.isOverworld();
        if (!hasSkylight || !CarpetSettings.newLight) return hasSkylight;
        LightingHooks.relightSkylightColumn(world, (WorldChunk) (Object) this, x, z, this.chunkX * 16 + x, this.chunkZ * 16 + z);
        return false; // cancel vanilla code
    }

    @Redirect(
            method = "resetLightAt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/dimension/Dimension;hasSkyLight()Z",
                    ordinal = 1
            )
    )
    private boolean relightSkyLight2(Dimension worldProvider) {
        return !CarpetSettings.newLight && worldProvider.isOverworld();
    }

    @Inject(
            method = "getLight(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/BlockPos;)I",
            at = @At("HEAD")
    )
    private void procOnGetLightFor(LightType type, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.newLight) ((NewLightWorld) world).getLightingEngine().procLightUpdates(type);
    }

    @Inject(
            method = "getLight(Lnet/minecraft/util/math/BlockPos;I)I",
            at = @At("HEAD")
    )
    private void procOnGetLightSubtracted(BlockPos pos, int amount, CallbackInfoReturnable<Integer> cir) {
        if (CarpetSettings.newLight) ((NewLightWorld) world).getLightingEngine().procLightUpdates();
    }

    @Redirect(
            method = "setLight",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/WorldChunk;populateSkylight()V"
            )
    )
    private void newLightGenerateSkylightMap(WorldChunk chunk, LightType type, BlockPos pos) {
        if (CarpetSettings.newLight) {
            //Forge: generateSkylightMap produces the wrong result (See #3870)
            LightingHooks.initSkylightForSection(world, chunk, sections[pos.getY() >> 4]);
        } else {
            chunk.populateSkylight();
        }
    }

    @Inject(
            method = "load",
            at = @At("RETURN")
    )
    private void onOnLoad(CallbackInfo ci) {
        if (CarpetSettings.newLight) LightingHooks.onLoad(world, (WorldChunk) (Object) this);
    }

    @Redirect(
            method = "populate(Lnet/minecraft/world/chunk/ChunkGenerator;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/WorldChunk;populateLight()V"
            )
    )
    private void checkLightInPopulate(WorldChunk chunk) {
        if (CarpetSettings.newLight) {
            this.terrainPopulated = true;
        } else {
            chunk.populateLight();
        }
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/chunk/WorldChunk;lightPopulated:Z"
            )
    )
    private boolean checkLightInOnTick(WorldChunk chunk) {
        return CarpetSettings.newLight || lightPopulated;
    }


    @ModifyVariable(
            method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;)Lnet/minecraft/block/state/BlockState;",
            index = 12,
            at = @At(
                    value = "STORE",
                    ordinal = 1
            )
    )
    private boolean setBlockStateInitSkylight(boolean flag, BlockPos pos) {
        if (CarpetSettings.newLight){
            //Forge: Always initialize sections properly (See #3870 and #3879)
            LightingHooks.initSkylightForSection(world, (WorldChunk) (Object) this, sections[pos.getY() >> 4]);
            //Forge: Don't call generateSkylightMap (as it produces the wrong result; sections are initialized above). Never bypass relightBlock (See #3870)
            return false;
        }
        return flag;
    }

    //Forge: Error correction is unnecessary as these are fixed (See #3871)
    @Redirect(
            method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;)Lnet/minecraft/block/state/BlockState;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/WorldChunk;queueLightUpdate(II)V"
            )
    )
    private void dontPropagateSkylightOcclusion(WorldChunk chunk, int x, int z) {
        if (CarpetSettings.newLight) return;
        this.queueLightUpdate(x, z);
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
        WorldChunkSection section = this.sections[y >> 4];
        if (section == WorldChunk.EMPTY) {
            return this.hasSkyAccess(pos) ? type.defaultValue : 0;
        }
        if (type == LightType.SKY) {
            return this.world.dimension.isOverworld() ? section.getSkyLight(x, y & 15, z) : 0;
        }
        return type == LightType.BLOCK ? section.getBlockLight(x, y & 15, z) : type.defaultValue;
    }
}
