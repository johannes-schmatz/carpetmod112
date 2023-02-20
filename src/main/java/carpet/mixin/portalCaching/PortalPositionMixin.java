package carpet.mixin.portalCaching;

import carpet.utils.extensions.ExtendedPortalPosition;

import net.minecraft.entity.PortalTeleporter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PortalTeleporter.Position.class)
public class PortalPositionMixin extends BlockPos implements ExtendedPortalPosition {
    @Shadow public long pos;
    private Vec3d cachingCoords;

    public PortalPositionMixin(BlockPos pos, long lastUpdate) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.pos = lastUpdate;
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
