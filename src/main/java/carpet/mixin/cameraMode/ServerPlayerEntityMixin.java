package carpet.mixin.cameraMode;

import carpet.CarpetSettings;
import carpet.helpers.CameraData;
import carpet.logging.logHelpers.DebugLogHelper;
import carpet.utils.extensions.CameraPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements CameraPlayer {
    private CameraData cameraData = new CameraData();

    @Shadow @Final public MinecraftServer server;
    @Shadow public ServerPlayNetworkHandler networkHandler;
    @Shadow @Final public ServerPlayerInteractionManager interactionManager;
    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow public abstract void teleport(double x, double y, double z);

    public ServerPlayerEntityMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(
            method = "copyFrom",
            at = @At("RETURN")
    )
    private void onCopyFrom(ServerPlayerEntity other, boolean keepEverything, CallbackInfo ci) {
        cameraData = ((ServerPlayerEntityMixin) (Object) other).cameraData;
    }

    @Inject(
            method = "setGameMode",
            at = @At("RETURN")
    )
    private void onGameModeChange(GameMode gameType, CallbackInfo ci) {
        if (gameType != GameMode.SPECTATOR) {
            // Rule to prevent /c camera mode to spectate other players, disable after exiting spectator mode CARPET-XCOM
            cameraData.disableSpectatePlayers = false;
        }
        cameraData.gameModeCamera = false;
    }

    @Override
    public void storeCameraData(boolean hasNightvision) {
        cameraData = new CameraData(asPlayer(), hasNightvision, cameraData.gameModeCamera);
    }

    @Override
    public void setGamemodeCamera() {
        cameraData.gameModeCamera = true;
    }

    @Override
    public boolean getGamemodeCamera() {
        return cameraData.gameModeCamera;
    }

    @Override
    public boolean hadNightvision() {
        return cameraData.nightVision;
    }

    @Override
    public boolean isDisableSpectatePlayers() {
        return cameraData.disableSpectatePlayers;
    }

    private ServerPlayerEntity asPlayer() {
        return (ServerPlayerEntity) (Object) this;
    }

    @Override
    public boolean moveToStoredCameraData() {
        if (CarpetSettings.cameraModeRestoreLocation) {
            if (cameraData.storedDim != dimensionId) {
                ServerWorld worldserver1 = getServerWorld();
                ServerWorld worldserver2 = server.getWorld(cameraData.storedDim);
                dimensionId = cameraData.storedDim;
                networkHandler.sendPacket(new PlayerRespawnS2CPacket(dimensionId, worldserver1.getDifficulty(), worldserver1.getData().getGeneratorType(),
                        this.interactionManager.getGameMode()));
                this.server.getPlayerManager().updatePermissions(asPlayer());
                DebugLogHelper.invisDebug(() -> "s2: " + worldserver1.entities.contains(this) + " " + worldserver2.entities.contains(this));
                worldserver1.removeEntity(this);
                removed = false;
                worldserver1.getChunkAt(chunkX, chunkZ).removeEntity(this, chunkY);

                if (isAlive()) {
                    refreshPositionAndAngles(cameraData.storeX, cameraData.storeY, cameraData.storeZ, cameraData.storeYaw, cameraData.storePitch);
                    worldserver2.addEntity(this);
                    worldserver2.tickEntity(this, false);
                }
                DebugLogHelper.invisDebug(() -> "s3: " + worldserver1.entities.contains(this) + " " + worldserver2.entities.contains(this));
                setWorld(worldserver2);
                this.server.getPlayerManager().onChangedDimension(asPlayer(), worldserver1);
                teleport(cameraData.storeX, cameraData.storeY, cameraData.storeZ);
                interactionManager.setWorld(worldserver2);
                this.server.getPlayerManager().sendWorldInfo(asPlayer(), worldserver2);
                this.server.getPlayerManager().sendPlayerInfo(asPlayer());
                DebugLogHelper.invisDebug(() -> "s4: " + worldserver1.entities.contains(this) + " " + worldserver2.entities.contains(this));
                return true;
            } else {
                if (cameraData.storeX == 0 && cameraData.storeY == 0 && cameraData.storeZ == 0)
                    cameraData.storeY = 256.0f;
                double dist = Math.sqrt(new BlockPos(cameraData.storeX, cameraData.storeY, cameraData.storeZ).squaredDistanceTo(x, y, z));
                networkHandler.teleport(cameraData.storeX, cameraData.storeY, cameraData.storeZ, cameraData.storeYaw, cameraData.storePitch);
                return dist > (this.server.getPlayerManager().getViewDistance() - 2) * 16;
            }
        }
        return false;
    }
}
