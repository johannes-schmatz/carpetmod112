package carpet.mixin.worldEdit;

import carpet.helpers.CapturedDrops;
import carpet.worldedit.WorldEditBridge;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.FillCommand;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(FillCommand.class)
public class FillCommandMixin {
    @Inject(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;breakBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void beforeDestroy(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci, BlockPos blockpos, BlockPos blockpos1,
            Block block, BlockState iblockstate, BlockPos blockpos2, BlockPos blockpos3, int i, World world, NbtCompound tag, boolean flag, List<?> list, int l,
            int i1, int j1, BlockPos currentPos) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        NbtCompound worldEditTag = flag ? tag : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, currentPos, Blocks.AIR.defaultState(), worldEditTag);
        CapturedDrops.setCapturingDrops(true);
    }

    @Inject(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;breakBlock(Lnet/minecraft/util/math/BlockPos;Z)Z",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void afterDestroy(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci, BlockPos blockpos, BlockPos blockpos1, Block block, BlockState iblockstate, BlockPos blockpos2, BlockPos blockpos3, int i, World world) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        CapturedDrops.setCapturingDrops(false);
        for (ItemEntity drop : CapturedDrops.getCapturedDrops())
            WorldEditBridge.recordEntityCreation(worldEditPlayer, world, drop);
        CapturedDrops.clearCapturedDrops();
    }

    @Inject(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;I)Z",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void hollowSetBlock(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci, BlockPos blockpos, BlockPos blockpos1,
            Block block, BlockState iblockstate, BlockPos blockpos2, BlockPos blockpos3, int i, World world, NbtCompound tag, boolean flag, List<?> list, int l,
            int i1, int j1, BlockPos currentPos) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        NbtCompound worldEditTag = flag ? tag : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, currentPos, Blocks.AIR.defaultState(), worldEditTag);
    }

    @Inject(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void normalSetBlock(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci, BlockPos blockpos, BlockPos blockpos1,
            Block block, BlockState blockState, BlockPos blockpos2, BlockPos blockpos3, int i, World world, NbtCompound tag, boolean flag, List<?> list, int l, int i1
            , int j1, BlockPos currentPos) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        NbtCompound worldEditTag = flag ? tag : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, currentPos, blockState, worldEditTag);
    }
}
