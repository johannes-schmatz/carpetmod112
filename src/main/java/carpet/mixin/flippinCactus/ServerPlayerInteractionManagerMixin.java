package carpet.mixin.flippinCactus;

import carpet.helpers.BlockRotator;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow public ServerPlayerEntity player;

    @Shadow public World world;

    @Inject(
            method = "useBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/state/BlockState;getBlock()Lnet/minecraft/block/Block;",
                    ordinal = 1
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void tryFlipWithCactus(PlayerEntity player, World world, ItemStack stack, InteractionHand hand, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<InteractionResult> cir, BlockState blockState) {
        //flip method will check for flippinCactus setting
        if (BlockRotator.flipBlockWithCactus(world, pos, blockState, player, hand, facing, hitX, hitY, hitZ)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
