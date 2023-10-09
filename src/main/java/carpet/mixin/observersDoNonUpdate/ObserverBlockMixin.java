package carpet.mixin.observersDoNonUpdate;

import carpet.CarpetSettings;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.state.property.BooleanProperty;
import net.minecraft.block.state.property.Property;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ObserverBlock.class)
public class ObserverBlockMixin {
    @Redirect(
            method = "getPlacementState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/state/BlockState;set(Lnet/minecraft/block/state/property/Property;Ljava/lang/Comparable;)Lnet/minecraft/block/state/BlockState;"
            )
    )
    private <T extends Comparable<T>, V extends T> BlockState noUpdateOnPlace(BlockState state, Property<T> property, V value) {
        return state.set(property, value).set(ObserverBlock.POWERED, CarpetSettings.observersDoNonUpdate);
    }
}
