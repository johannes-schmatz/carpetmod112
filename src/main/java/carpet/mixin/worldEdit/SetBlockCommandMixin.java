package carpet.mixin.worldEdit;

import carpet.helpers.CapturedDrops;
import carpet.worldedit.WorldEditBridge;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.SetBlockCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SetBlockCommand.class)
public class SetBlockCommandMixin {
    @Inject(
            method = "method_3279",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void beforeDestroy(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci, BlockPos pos, Block block,
            BlockState iblockstate, World world, NbtCompound tag, boolean flag) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        NbtCompound worldEditTag = flag ? tag : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, pos, Blocks.AIR.getDefaultState(), worldEditTag);
        CapturedDrops.setCapturingDrops(true);
    }

    @Inject(
            method = "method_3279",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void afterDestroy(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci, BlockPos blockpos, Block block, BlockState iblockstate, World world) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        CapturedDrops.setCapturingDrops(false);
        for (ItemEntity drop : CapturedDrops.getCapturedDrops())
            WorldEditBridge.recordEntityCreation(worldEditPlayer, world, drop);
        CapturedDrops.clearCapturedDrops();
    }

    @Inject(
            method = "method_3279",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void normalSetBlock(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci, BlockPos pos, Block block,
            BlockState blockState, World world, NbtCompound tag, boolean flag) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        NbtCompound worldEditTag = flag ? tag : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, pos, blockState, worldEditTag);
    }
}
