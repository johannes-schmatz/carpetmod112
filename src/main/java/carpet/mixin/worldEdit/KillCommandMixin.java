package carpet.mixin.worldEdit;

import carpet.worldedit.WorldEditBridge;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.KillCommand;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KillCommand.class)
public class KillCommandMixin {
    @Redirect(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;m_3468489()V"
            )
    )
    private void recordEntityRemoval(Entity entity, MinecraftServer server, CommandSource sender, String[] args) {
        entity.m_3468489();
        if (!(entity instanceof ServerPlayerEntity)) {
            ServerPlayerEntity worldEditPlayer = sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null;
            WorldEditBridge.recordEntityRemoval(worldEditPlayer, sender.getSourceWorld(), entity);
        }
    }
}
