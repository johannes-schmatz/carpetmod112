package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ReplaceItemCommand;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ReplaceItemCommand.class)
public class ReplaceItemCommandMixin {
    @Inject(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void recordBlockEdit(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci, boolean flag, int i, String s, int j, Item item, int k, int l, ItemStack itemstack, BlockPos blockpos, World world, BlockEntity tileentity) {
        ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos, world.getBlockState(blockpos), tileentity.writeNbt(new NbtCompound()));
    }
}
