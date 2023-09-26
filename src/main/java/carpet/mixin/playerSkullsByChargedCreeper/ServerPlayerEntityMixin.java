package carpet.mixin.playerSkullsByChargedCreeper;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.mob.hostile.CreeperEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    // Allows players to drop their skulls when blown up by charged creeper CARPET-XCOM
    @Inject(
            method = "onKilled",
            at = @At("RETURN")
    )
    private void dropSkullByChargedCreeper(DamageSource cause, CallbackInfo ci) {
        if (!CarpetSettings.playerSkullsByChargedCreeper) return;
        Entity entity = cause.getAttacker();
        if (!(entity instanceof CreeperEntity)) return;
        CreeperEntity creeper = (CreeperEntity) entity;
        if (!creeper.isCharged() || !creeper.shouldDropMobHead()) return;
        creeper.addMobHeadDrop();
        try {
            ItemStack skull = new ItemStack(Items.SKULL, 1, 3);
            skull.setNbt(StringNbtReader.parse(String.format("{SkullOwner:\"%s\"}", getName().toLowerCase())));
            this.dropItem(skull, 0.0F);
        } catch (NbtException e) {
            e.printStackTrace();
        }
    }
}
