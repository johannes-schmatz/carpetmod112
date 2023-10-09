package carpet.helpers;

import carpet.CarpetSettings;

import net.minecraft.server.entity.living.player.ServerPlayerEntity;

public class CameraData {
	public double storeX;
	public double storeY;
	public double storeZ;
	public float storeYaw;
	public float storePitch;
	public int storedDim;
	public boolean disableSpectatePlayers;
	public boolean gameModeCamera;
	public boolean nightVision;

	public CameraData() {
	}

	public CameraData(ServerPlayerEntity player, boolean hasNightVision, boolean gameModeCamera) {
		storeX = player.x;
		storeY = player.y;
		storeZ = player.z;
		storeYaw = player.yaw;
		storePitch = player.pitch;
		storedDim = player.dimensionId;
		disableSpectatePlayers = CarpetSettings.cameraModeDisableSpectatePlayers;
		nightVision = hasNightVision;
		this.gameModeCamera = gameModeCamera;
	}
}
