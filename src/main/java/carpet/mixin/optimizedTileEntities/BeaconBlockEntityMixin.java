package carpet.mixin.optimizedTileEntities;

import carpet.CarpetSettings;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends LockableContainerBlockEntity {
    // If optimized and it canSeeSky, skip segment color calculations server-side by increasing the initial value of the loop counter
    @ModifyConstant(
            method = "updateBeam",
            constant = @Constant(
                    intValue = 1,
                    ordinal = 2
            )
    )
    private int optimizedTileEntitiesOffset(int origValue) {
        if (!CarpetSettings.optimizedTileEntities || world.isClient) return origValue;
        return world.dimension.isOverworld() && world.hasDirectSunlight(pos) ? 256 : origValue;
    }
}
