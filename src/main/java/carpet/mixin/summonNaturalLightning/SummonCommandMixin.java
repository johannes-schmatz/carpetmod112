package carpet.mixin.summonNaturalLightning;

import carpet.CarpetSettings;
import carpet.mixin.accessors.ServerWorldAccessor;
import carpet.worldedit.WorldEditBridge;

import net.minecraft.entity.living.mob.passive.animal.SkeletonHorse;
import net.minecraft.entity.weather.LightningBoltEntity;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.SummonCommand;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SummonCommand.class)
public class SummonCommandMixin {
    @Redirect(
            method = "run",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/World;DDDZ)Lnet/minecraft/entity/weather/LightningBoltEntity;"
            )
    )
    private LightningBoltEntity summonNaturalLightning(World world, double x, double y, double z, boolean effectOnly, MinecraftServer server, CommandSource sender) {
        if (!CarpetSettings.summonNaturalLightning) return new LightningBoltEntity(world, x, y,z, effectOnly);
        BlockPos bp = ((ServerWorldAccessor)world).invokeAdjustPosToNearbyEntity(new BlockPos(x, 0, z));
        if (!world.isRaining(bp)) return new LightningBoltEntity(world, x, y,z, effectOnly);

        LocalDifficulty difficulty = world.getLocalDifficulty(bp);
        if (world.getGameRules().getBoolean("doMobSpawning") && world.random.nextDouble() < (double)difficulty.get() * 0.01D) {
            SkeletonHorse horse = new SkeletonHorse(world);
            horse.setIsTrap(true);
            horse.setBreedingAge(0);
            horse.setPosition(bp.getX(), bp.getY(), bp.getZ());
            world.addEntity(horse);

            WorldEditBridge.recordEntityCreation(sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null, world, horse);

            return new LightningBoltEntity(world, bp.getX(), bp.getY(), bp.getZ(), true);
        } else {
            return new LightningBoltEntity(world, bp.getX(), bp.getY(), bp.getZ(), false);
        }
    }
}
