package carpet.utils.extensions;

public interface CameraPlayer {
    void storeCameraData(boolean hasNightVision);
    void setGameModeCamera();
    boolean getGameModeCamera();
    boolean hadNightVision();
    boolean moveToStoredCameraData();
    boolean isDisableSpectatePlayers();
}
