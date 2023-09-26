package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Redirect(
            method = "handlePlayerUseBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ServerPlayerInteractionManager;useBlock(Lnet/minecraft/entity/living/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;FFF)Lnet/minecraft/world/InteractionResult;"
            )
    )
    private InteractionResult processRightClickBlock(ServerPlayerInteractionManager manager, PlayerEntity player, World worldIn, ItemStack stack, InteractionHand hand, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ) {
        try {
            CarpetClientChunkLogger.setReason("Player interacting with right click");
            return manager.useBlock(player, worldIn, stack, hand, pos, facing, hitX, hitY, hitZ);
        } finally {
            CarpetClientChunkLogger.resetReason();
        }
    }
}
