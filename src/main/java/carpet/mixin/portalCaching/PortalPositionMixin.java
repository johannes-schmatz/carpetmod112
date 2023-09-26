package carpet.mixin.portalCaching;

import carpet.utils.extensions.ExtendedPortalPosition;

import net.minecraft.server.world.PortalForcer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PortalForcer.PortalPos.class)
public class PortalPositionMixin extends BlockPos implements ExtendedPortalPosition {
    @Shadow public long lastUseTime;
    private Vec3d cachingCoords;

    public PortalPositionMixin(BlockPos pos, long lastUpdate) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.lastUseTime = lastUpdate;
    }

    @Override
    public Vec3d getCachingCoords() {
        return cachingCoords;
    }

    @Override
    public void setCachingCoords(Vec3d coords) {
        this.cachingCoords = coords;
    }
}
