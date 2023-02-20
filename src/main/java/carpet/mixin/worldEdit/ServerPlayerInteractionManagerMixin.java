package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow public ServerPlayerEntity player;
    @Shadow public World world;

    @Inject(
            method = "processBlockBreakingAction",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onWorldEditLeftClick(BlockPos pos, Direction side, CallbackInfo ci) {
        if (!WorldEditBridge.onLeftClickBlock(world, pos, player)) {
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, pos));
            ci.cancel();
        }
    }

    @Inject(
            method = "method_12792",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isSneaking()Z"
            ),
            cancellable = true
    )
    private void onWorldEditRightClick(PlayerEntity player, World worldIn, ItemStack stack, Hand hand, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<ActionResult> cir) {
        if (!WorldEditBridge.onRightClickBlock(world, pos, this.player)) {
            this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, pos));
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
