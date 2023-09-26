package carpet.mixin.carefulBreak;

import carpet.helpers.CarefulBreakHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow public ServerPlayerEntity player;

    @Redirect(
            method = "tryMineBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;afterMinedByPlayer(Lnet/minecraft/world/World;Lnet/minecraft/entity/living/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/item/ItemStack;)V"
            )
    )
    private void harvestBlock(Block block, World worldIn, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity te, ItemStack stack) {
        try {
            CarefulBreakHelper.miningPlayer = this.player;
            block.afterMinedByPlayer(worldIn, player, pos, state, te, stack);
        } finally {
            CarefulBreakHelper.miningPlayer = null;
        }
    }
}
