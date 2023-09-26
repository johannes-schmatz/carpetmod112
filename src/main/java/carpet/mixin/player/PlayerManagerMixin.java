package carpet.mixin.player;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.patches.FakeServerPlayerEntity;
import carpet.patches.FakeServerPlayNetworkHandler;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private List<ServerPlayerEntity> players;

    @Inject(
            method = "onLogin",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ServerPlayerInteractionManager;setWorld(Lnet/minecraft/server/world/ServerWorld;)V"
            )
    )
    private void resetToSetPosition(Connection netManager, ServerPlayerEntity player, CallbackInfo ci) {
        if (player instanceof FakeServerPlayerEntity) {
            // Ignore position from NBT and use the one specified in the command
            ((FakeServerPlayerEntity) player).resetToSetPosition();
        }
    }

    @Redirect(
            method = "onLogin",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/server/network/handler/ServerPlayNetworkHandler"
            )
    )
    private ServerPlayNetworkHandler createNetHandler(MinecraftServer server, Connection netManager, ServerPlayerEntity player) {
        if (player instanceof FakeServerPlayerEntity) {
            return new FakeServerPlayNetworkHandler(server, netManager, player);
        }
        return new ServerPlayNetworkHandler(server, netManager, player);
    }

    @Inject(
            method = "saveAll",
            at = @At("HEAD")
    )
    private void storeFakePlayerData(CallbackInfo ci) {
        ArrayList<String> list = new ArrayList<>();
        if(!CarpetSettings.reloadFakePlayers){
            CarpetServer.getInstance().writeConf(list);
            return;
        }
        for (ServerPlayerEntity p : players) {
            if(p instanceof FakeServerPlayerEntity){
                list.add(FakeServerPlayerEntity.getInfo(p));
            }
        }
        CarpetServer.getInstance().writeConf(list);
    }
}
