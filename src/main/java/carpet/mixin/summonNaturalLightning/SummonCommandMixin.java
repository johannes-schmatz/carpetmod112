package carpet.mixin.summonNaturalLightning;

import carpet.CarpetSettings;
import carpet.mixin.accessors.ServerWorldAccessor;
import carpet.worldedit.WorldEditBridge;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.LightningBoltEntity;
import net.minecraft.entity.SkeletonHorseEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.SummonCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SummonCommand.class)
public class SummonCommandMixin {
    @Redirect(
            method = "method_3279",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/entity/LightningBoltEntity"
            )
    )
    private LightningBoltEntity summonNaturalLightning(World world, double x, double y, double z, boolean effectOnly, MinecraftServer server, CommandSource sender) {
        if (!CarpetSettings.summonNaturalLightning) return new LightningBoltEntity(world, x, y,z, effectOnly);
        BlockPos bp = ((ServerWorldAccessor)world).invokeAdjustPosToNearbyEntity(new BlockPos(x, 0, z));
        if (!world.hasRain(bp)) return new LightningBoltEntity(world, x, y,z, effectOnly);

        LocalDifficulty difficulty = world.getLocalDifficulty(bp);
        if (world.getGameRules().getBoolean("doMobSpawning") && world.random.nextDouble() < (double)difficulty.getLocalDifficulty() * 0.01D) {
            SkeletonHorseEntity horse = new SkeletonHorseEntity(world);
            horse.method_14041(true);
            horse.setAge(0);
            horse.updatePosition(bp.getX(), bp.getY(), bp.getZ());
            world.spawnEntity(horse);

            WorldEditBridge.recordEntityCreation(sender instanceof ServerPlayerEntity ? (ServerPlayerEntity) sender : null, world, horse);

            return new LightningBoltEntity(world, bp.getX(), bp.getY(), bp.getZ(), true);
        } else {
            return new LightningBoltEntity(world, bp.getX(), bp.getY(), bp.getZ(), false);
        }
    }
}
