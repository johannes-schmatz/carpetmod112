package carpet.mixin.ridingPlayerUpdateFix;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.mob.passive.animal.LlamaEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Shadow @Final public MinecraftServer server;

    public ServerPlayerEntityMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancement/criterion/TickTrigger;run(Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;)V"
            )
    )
    private void ridingPlayerUpdateFix(CallbackInfo ci) {
        if (CarpetSettings.ridingPlayerUpdateFix) {
            Entity riding = getVehicle();
            if (riding instanceof MinecartEntity || riding instanceof LlamaEntity){
                this.server.getPlayerManager().move((ServerPlayerEntity) (Object) this);
            }
        }
    }
}
