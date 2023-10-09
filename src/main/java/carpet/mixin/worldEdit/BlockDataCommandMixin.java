package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.command.BlockDataCommand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockDataCommand.class)
public class BlockDataCommandMixin {
    @Inject(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/BlockEntity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void recordBlockEdit(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci,
                                 BlockPos pos, World world, BlockState state, BlockEntity blockEntity, NbtCompound nbt) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, pos, state, nbt);
    }
}
