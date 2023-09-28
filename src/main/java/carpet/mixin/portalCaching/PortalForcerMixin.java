package carpet.mixin.portalCaching;

import carpet.CarpetSettings;
import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.PortalCaching;
import carpet.mixin.accessors.ServerPlayNetworkHandlerAccessor;
import carpet.utils.extensions.ExtendedPortalPosition;
import carpet.utils.extensions.ExtendedPortalForcer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.PortalForcer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

@Mixin(PortalForcer.class)
public class PortalForcerMixin implements ExtendedPortalForcer {
    @Shadow @Final private Long2ObjectMap<PortalForcer.PortalPos> portalCache;
    @Shadow @Final private ServerWorld world;
    private final Long2ObjectMap<PortalForcer.PortalPos> destinationHistoryCache = new Long2ObjectOpenHashMap<>(4096);

    /**
     *
     * @author skyrising
     * @reason carpet mod
     */
    @Overwrite
    public boolean findNetherPortal(Entity entity, float rotationYaw) {
        int range = 128;
        double distance = -1.0D;
        int x = MathHelper.floor(entity.x);
        int z = MathHelper.floor(entity.z);
        boolean flag = true;
        boolean flag_cm = true;
        BlockPos outPos = BlockPos.ORIGIN;
        long posKey = ChunkPos.toLong(x, z);

        if (this.portalCache.containsKey(posKey)) {
            PortalForcer.PortalPos pos = this.portalCache.get(posKey);
            distance = 0.0D;
            outPos = pos;
            pos.lastUseTime = this.world.getTime();
            flag = false;
        } else if (CarpetSettings.portalCaching && this.destinationHistoryCache.containsKey(posKey)) {
            // potential best candidate for linkage.
            PortalForcer.PortalPos pos = this.destinationHistoryCache.get(posKey);
            //just to verify nobody is cheating the system with update suppression
            if (this.world.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL) {
                distance = 0.0D;
                outPos = pos;
                flag_cm = false;
            }
        }

        if (distance < 0.0D) {
            BlockPos entityBlockPos = new BlockPos(entity);

            for (int offX = -range; offX <= range; ++offX) {
                BlockPos blockpos2;

                for (int offZ = -range; offZ <= range; ++offZ) {
                    for (BlockPos currentPos = entityBlockPos.add(offX, this.world.getDimensionHeight() - 1 - entityBlockPos.getY(), offZ); currentPos.getY() >= 0; currentPos = blockpos2) {
                        blockpos2 = currentPos.down();

                        if (this.world.getBlockState(currentPos).getBlock() == Blocks.NETHER_PORTAL) {
                            for (blockpos2 = currentPos.down(); this.world.getBlockState(blockpos2).getBlock() == Blocks.NETHER_PORTAL; blockpos2 = blockpos2.down()) {
                                currentPos = blockpos2;
                            }

                            double currentDistance = currentPos.squaredDistanceTo(entityBlockPos);

                            if (distance < 0.0D || currentDistance < distance) {
                                distance = currentDistance;
                                outPos = currentPos;
                            }
                        }
                    }
                }
            }
        }

        if (!(distance >= 0.0D)) {
            return false;
        }

        if (flag) {
            this.portalCache.put(posKey, createPortalPosition(outPos, this.world.getTime(), new Vec3d(entity.x, entity.y, entity.z)));
        }

        if (CarpetSettings.portalCaching && (flag || flag_cm)) {
            //its timeless
            this.destinationHistoryCache.put(posKey, createPortalPosition(outPos, 0L, new Vec3d(entity.x, entity.y, entity.z)));
        }

        double outX = outPos.getX() + 0.5;
        double outZ = outPos.getZ() + 0.5;
        BlockPattern.Match pattern = Blocks.NETHER_PORTAL.findPortalShape(this.world, outPos);
        boolean axisNegative = pattern.getForward().clockwiseY().getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        double horizontal = pattern.getForward().getAxis() == Direction.Axis.X ? pattern.getTopLeftFront().getZ() : pattern.getTopLeftFront().getX();
        double outY = pattern.getTopLeftFront().getY() + 1 - entity.getLastPortalOffset().y * pattern.getHeight();

        if (axisNegative) {
            ++horizontal;
        }

        //CM portalSuffocationFix
        //removed offset calculation outside of the if statement
        double offset = (1.0D - entity.getLastPortalOffset().x) * pattern.getWidth() * pattern.getForward().clockwiseY().getAxisDirection().getOffset();
        if (CarpetSettings.portalSuffocationFix) {
            double correctedRadius = 1.02 * entity.width / 2;
            if (correctedRadius >= pattern.getWidth() - correctedRadius) {
                //entity is wider than portal, so will suffocate anyways, so place it directly in the middle
                correctedRadius = (double) pattern.getWidth() / 2 - 0.001;
            }

            if (offset >= 0) {
                offset = MathHelper.clamp(offset, correctedRadius, pattern.getWidth() - correctedRadius);
            } else {
                offset = MathHelper.clamp(offset, -pattern.getWidth() + correctedRadius, -correctedRadius);
            }
        }

        if (pattern.getForward().getAxis() == Direction.Axis.X) {
            outZ = horizontal + offset;
        } else {
            outX = horizontal + offset;
        }

        float x2x = 0.0F;
        float z2z = 0.0F;
        float x2z = 0.0F;
        float z2x = 0.0F;

        Direction backwards = pattern.getForward().getOpposite();
        Direction teleportDir = entity.getLastPortalFacing();
        if (backwards == teleportDir) {
            x2x = 1;
            z2z = 1;
        } else if (backwards == teleportDir.getOpposite()) {
            x2x = -1;
            z2z = -1;
        } else if (backwards == teleportDir.clockwiseY()) {
            x2z = 1;
            z2x = -1;
        } else {
            x2z = -1;
            z2x = 1;
        }

        double motionX = entity.velocityX;
        double motionZ = entity.velocityZ;
        entity.velocityX = motionX * (double) x2x + motionZ * (double) z2x;
        entity.velocityZ = motionX * (double) x2z + motionZ * (double) z2z;
        entity.yaw = rotationYaw - (float) (teleportDir.getOpposite().getIdHorizontal() * 90) + (float) (pattern.getForward().getIdHorizontal() * 90);

        if (entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) entity).networkHandler.teleport(outX, outY, outZ, entity.yaw, entity.pitch);
            // Resets the players position after move to fix a bug created in the teleportation. CARPET-XCOM
            if (CarpetSettings.portalTeleportationFix) {
                ((ServerPlayNetworkHandlerAccessor) ((ServerPlayerEntity) entity).networkHandler).invokeCaptureCurrentPosition();
            }
        } else {
            entity.refreshPositionAndAngles(outX, outY, outZ, entity.yaw, entity.pitch);
        }

        return true;
    }

    /**
     * @author skyrising
     * @reason carpet mod
     */
    @Overwrite
    public void tick(long worldTime)
    {
        if (worldTime % 100 != 0) return;
        long uncachingTime = worldTime - 300L;
        ObjectIterator<PortalForcer.PortalPos> it = this.portalCache.values().iterator();
        ArrayList<Vec3d> uncachings = new ArrayList<>();
        while (it.hasNext()) {
            PortalForcer.PortalPos pos = it.next();

            if (pos == null || pos.lastUseTime < uncachingTime) {
                uncachings.add(((ExtendedPortalPosition) pos).getCachingCoords());
                it.remove();
            }
        }

        // Carpet Mod
        //failsafe - arbitrary, but will never happen in normal circumstances,
        //but who knows these freekin players.
        if (CarpetSettings.portalCaching && this.destinationHistoryCache.size() > 65000) {
            removeAllCachedEntries();
        }

        // Log portal uncaching CARPET-XCOM
        if(LoggerRegistry.__portalCaching) {
            PortalCaching.portalCachingCleared(world, portalCache.size(), uncachings);
        }
    }

    @Inject(
            method = "generateNetherPortal",
            at = @At("RETURN")
    )
    private void onMakePortal(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.portalCaching) clearHistoryCache();
    }

    @Override
    public void clearHistoryCache() {
        MinecraftServer server = this.world.getServer();
        for (ServerWorld world : server.worlds) {
            ((PortalForcerMixin) (Object) world.getPortalForcer()).removeAllCachedEntries();
        }
    }

    public void removeAllCachedEntries() {
        this.destinationHistoryCache.clear();
    }

    private static MethodHandle portalPositionConstructor;

    private PortalForcer.PortalPos createPortalPosition(BlockPos pos, long lastUpdate, Vec3d cachingCoords) {
        if (portalPositionConstructor == null) {
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                Constructor<PortalForcer.PortalPos> constructor = PortalForcer.PortalPos.class.getDeclaredConstructor(PortalForcer.class, BlockPos.class,
                        long.class);
                portalPositionConstructor = lookup.unreflectConstructor(constructor);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            PortalForcer.PortalPos portalPos = (PortalForcer.PortalPos) portalPositionConstructor.invokeExact((PortalForcer) (Object) this, pos, lastUpdate);
            ((ExtendedPortalPosition) portalPos).setCachingCoords(cachingCoords);
            return portalPos;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
