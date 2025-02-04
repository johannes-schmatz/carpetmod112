package carpet.mixin.craftingWindowDuplication;

import carpet.utils.extensions.DupingPlayer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements DupingPlayer {
    // Adding this dupe feature to make it compatible with carpet 12.0 as per community request. CARPET-XCOM
    private int dupe;
    private boolean scanForDuping;

    @Inject(method = "tick", at = @At("RETURN"))
    private void clearDupeItem(CallbackInfo ci) {
        clearDupeItem();
    }

    @Override
    public void clearDupeItem() {
        dupe = Integer.MIN_VALUE;
    }

    @Override
    public void dupeItem(int slot) {
        if(scanForDuping){
            dupe = slot;
        }
    }

    @Override
    public int getDupeItem() {
        return dupe;
    }

    @Override
    public void dupeItemScan(boolean s){
        scanForDuping = s;
    }
}
