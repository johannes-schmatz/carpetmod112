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
import net.minecraft.entity.PortalTeleporter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.ServerPlayerEntity;
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

@Mixin(PortalTeleporter.class)
public class PortalForcerMixin implements ExtendedPortalForcer {
    @Shadow @Final private Long2ObjectMap<PortalTeleporter.Position> cache;
    @Shadow @Final private ServerWorld world;
    private final Long2ObjectMap<PortalTeleporter.Position> destinationHistoryCache = new Long2ObjectOpenHashMap<>(4096);

    /**
     *
     * @author skyrising
     * @reason carpet mod
     */
    @Overwrite
    public boolean method_8584(Entity entity, float rotationYaw) {
        int range = 128;
        double distance = -1.0D;
        int x = MathHelper.floor(entity.x);
        int z = MathHelper.floor(entity.z);
        boolean flag = true;
        boolean flag_cm = true;
        BlockPos outPos = BlockPos.ORIGIN;
        long posKey = ChunkPos.getIdFromCoords(x, z);

        if (this.cache.containsKey(posKey)) {
            PortalTeleporter.Position pos = this.cache.get(posKey);
            distance = 0.0D;
            outPos = pos;
            pos.pos = this.world.getLastUpdateTime();
            flag = false;
        } else if (CarpetSettings.portalCaching && this.destinationHistoryCache.containsKey(posKey)) {
            // potential best candidate for linkage.
            PortalTeleporter.Position pos = this.destinationHistoryCache.get(posKey);
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
                    for (BlockPos currentPos = entityBlockPos.add(offX, this.world.getEffectiveHeight() - 1 - entityBlockPos.getY(), offZ); currentPos.getY() >= 0; currentPos = blockpos2) {
                        blockpos2 = currentPos.down();

                        if (this.world.getBlockState(currentPos).getBlock() == Blocks.NETHER_PORTAL) {
                            for (blockpos2 = currentPos.down(); this.world.getBlockState(blockpos2).getBlock() == Blocks.NETHER_PORTAL; blockpos2 = blockpos2.down()) {
                                currentPos = blockpos2;
                            }

                            double currentDistance = currentPos.getSquaredDistance(entityBlockPos);

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
            this.cache.put(posKey, createPortalPosition(outPos, this.world.getLastUpdateTime(), new Vec3d(entity.x, entity.y, entity.z)));
        }

        if (CarpetSettings.portalCaching && (flag || flag_cm)) {
            //its timeless
            this.destinationHistoryCache.put(posKey, createPortalPosition(outPos, 0L, new Vec3d(entity.x, entity.y, entity.z)));
        }

        double outX = outPos.getX() + 0.5;
        double outZ = outPos.getZ() + 0.5;
        BlockPattern.Result pattern = Blocks.NETHER_PORTAL.findPortal(this.world, outPos);
        boolean axisNegative = pattern.getForwards().rotateYClockwise().getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        double horizontal = pattern.getForwards().getAxis() == Direction.Axis.X ? pattern.getFrontTopLeft().getZ() : pattern.getFrontTopLeft().getX();
        double outY = pattern.getFrontTopLeft().getY() + 1 - entity.getLastNetherPortalDirectionVector().y * pattern.getHeight();

        if (axisNegative) {
            ++horizontal;
        }

        //CM portalSuffocationFix
        //removed offset calculation outside of the if statement
        double offset = (1.0D - entity.getLastNetherPortalDirectionVector().x) * pattern.getWidth() * pattern.getForwards().rotateYClockwise().getAxisDirection().offset();
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

        if (pattern.getForwards().getAxis() == Direction.Axis.X) {
            outZ = horizontal + offset;
        } else {
            outX = horizontal + offset;
        }

        float x2x = 0.0F;
        float z2z = 0.0F;
        float x2z = 0.0F;
        float z2x = 0.0F;

        Direction backwards = pattern.getForwards().getOpposite();
        Direction teleportDir = entity.getLastNetherPortalDirection();
        if (backwards == teleportDir) {
            x2x = 1;
            z2z = 1;
        } else if (backwards == teleportDir.getOpposite()) {
            x2x = -1;
            z2z = -1;
        } else if (backwards == teleportDir.rotateYClockwise()) {
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
        entity.yaw = rotationYaw - (float) (teleportDir.getOpposite().getHorizontal() * 90) + (float) (pattern.getForwards().getHorizontal() * 90);

        if (entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) entity).networkHandler.requestTeleport(outX, outY, outZ, entity.yaw, entity.pitch);
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
    public void method_4698(long worldTime)
    {
        if (worldTime % 100 != 0) return;
        long uncachingTime = worldTime - 300L;
        ObjectIterator<PortalTeleporter.Position> it = this.cache.values().iterator();
        ArrayList<Vec3d> uncachings = new ArrayList<>();
        while (it.hasNext()) {
            PortalTeleporter.Position pos = it.next();

            if (pos == null || pos.pos < uncachingTime) {
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
            PortalCaching.portalCachingCleared(world, cache.size(), uncachings);
        }
    }

    @Inject(
            method = "method_3803",
            at = @At("RETURN")
    )
    private void onMakePortal(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.portalCaching) clearHistoryCache();
    }

    @Override
    public void clearHistoryCache() {
        MinecraftServer server = this.world.getServer();
        for (ServerWorld world : server.worlds) {
            ((PortalForcerMixin) (Object) world.getPortalTeleporter()).removeAllCachedEntries();
        }
    }

    public void removeAllCachedEntries() {
        this.destinationHistoryCache.clear();
    }

    private static MethodHandle portalPositionConstructor;

    private PortalTeleporter.Position createPortalPosition(BlockPos pos, long lastUpdate, Vec3d cachingCoords) {
        if (portalPositionConstructor == null) {
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                Constructor<PortalTeleporter.Position> constructor = PortalTeleporter.Position.class.getDeclaredConstructor(PortalTeleporter.class, BlockPos.class,
                        long.class);
                portalPositionConstructor = lookup.unreflectConstructor(constructor);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            PortalTeleporter.Position portalPos = (PortalTeleporter.Position) portalPositionConstructor.invokeExact((PortalTeleporter) (Object) this, pos, lastUpdate);
            ((ExtendedPortalPosition) portalPos).setCachingCoords(cachingCoords);
            return portalPos;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
