package carpet.mixin.tnt;

import carpet.CarpetSettings;
import carpet.commands.CommandTNT;
import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.TNTLogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.PrimedTntEntity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PrimedTntEntity.class)
public abstract class TntEntityMixin extends Entity {
    @Shadow private int f_7722649;
    private TNTLogHelper logHelper = null;
    private int mergedTNT = 1;
    private boolean mergeBool;
    private static final double[] cache = new double[12];
    private static final boolean[] cacheBool = new boolean[2];
    private static long cacheTime = 0;
    public TntEntityMixin(World worldIn) {
        super(worldIn);
    }

    @ModifyConstant(
            method = "<init>*",
            constant = @Constant(intValue = 80)
    )
    private int tntFuseLength(int ignored) {
        return CarpetSettings.tntFuseLength;
    }

    @Redirect(
            method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/living/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;random()D",
                    remap = false
            )
    )
    private double getRandomAngle() {
        if (CarpetSettings.hardcodeTNTangle >= 0) return CarpetSettings.hardcodeTNTangle / (Math.PI * 2);
        if (CarpetSettings.TNTAdjustableRandomAngle) return CommandTNT.rand.nextDouble();
        if (CarpetSettings.tntPrimerMomentumRemoved) return 0;
        return Math.random();
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/living/LivingEntity;)V",
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onInit(World worldIn, double x, double y, double z, LivingEntity igniter, CallbackInfo ci, float angle) {
        if (CarpetSettings.tntPrimerMomentumRemoved) {
            this.velocityX = 0;
            this.velocityY = 0;
            this.velocityZ = 0;
        }
        if (LoggerRegistry.__tnt) {
            if (logHelper == null) logHelper = new TNTLogHelper(); // don't replace if already there
            logHelper.onPrimed(x, y, z, angle);
        }
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;)V",
            at = @At("RETURN")
    )
    private void onInitLogger(World arg, CallbackInfo ci) {
        if (LoggerRegistry.__tnt) {
            //logHelper = new TNTLogHelper();
            //logHelper.onPrimed(x, y, z, 0);
        }
    }

    @Unique private boolean cacheMatching() {
        return cache[0] == x && cache[1] == y && cache[2] == z && cache[3] == velocityX && cache[4] == velocityY && cache[5] == velocityZ && cacheTime == getServer().getTicks();
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/PrimedTntEntity;move(Lnet/minecraft/entity/MoverType;DDD)V"
            )
    )
    private void movementOptimization(PrimedTntEntity tnt, MoverType type, double x, double y, double z) {
        if (!CarpetSettings.TNTmovementOptimization) {
            tnt.move(type, x, y, z);
            return;
        }
        // Optimized TNT movement skipping the move code given its expensive if identical tnt movement is done. CARPET-XCOM
        if(!cacheMatching()) {
            cache[0] = x;
            cache[1] = y;
            cache[2] = z;
            cache[3] = velocityX;
            cache[4] = velocityY;
            cache[5] = velocityZ;
            cacheTime = getServer().getTicks();
            this.move(MoverType.SELF, this.velocityX, this.velocityY, this.velocityZ);
            if (!removed) {
                cache[6] = x;
                cache[7] = y;
                cache[8] = z;
                cache[9] = velocityX;
                cache[10] = velocityY;
                cache[11] = velocityZ;
                cacheBool[0] = inCobweb;
                cacheBool[1] = onGround;
            } else {
                cache[0] = Integer.MAX_VALUE;
            }
        } else {
            this.setPosition(cache[6], cache[7], cache[8]);
            velocityX = cache[9];
            velocityY = cache[10];
            velocityZ = cache[11];
            inCobweb = cacheBool[0];
            onGround = cacheBool[1];
        }
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/PrimedTntEntity;velocityX:D",
                    ordinal = 0
            ),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/entity/PrimedTntEntity;onGround:Z"
                    )
            )
    )
    private void mergeTNT(CallbackInfo ci) {
        // Merge code for combining tnt into a single entity if they happen to exist in the same spot, same fuse, no motion CARPET-XCOM
        if(CarpetSettings.mergeTNT){
            if(!world.isClient && mergeBool && this.velocityX == 0 && this.velocityY == 0 && this.velocityZ == 0){
                mergeBool = false;
                for(Entity entity : world.getEntities(this, this.getShape())){
                    if(entity instanceof TntEntityMixin && !entity.removed){
                        TntEntityMixin entityTNTPrimed = (TntEntityMixin) entity;
                        if(entityTNTPrimed.velocityX == 0 && entityTNTPrimed.velocityY == 0 && entityTNTPrimed.velocityZ == 0
                                && this.x == entityTNTPrimed.x && this.y == entityTNTPrimed.y && this.z == entityTNTPrimed.z
                                && this.f_7722649 == entityTNTPrimed.f_7722649){
                            mergedTNT += entityTNTPrimed.mergedTNT;
                            entityTNTPrimed.remove();
                        }
                    }
                }
            }
        }
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/PrimedTntEntity;f_7722649:I",
                    ordinal = 0
            )
    )
    private void checkMergeAllowed(CallbackInfo ci) {
        // Merge only tnt that have had a chance to move CARPET-XCOM
        if(!world.isClient && (this.velocityX != 0 || this.velocityY != 0 || this.velocityZ != 0)){
            mergeBool = true;
        }
    }

    @Inject(
            method = "explode",
            at = @At("HEAD")
    )
    private void onExplode(CallbackInfo ci) {
        if (logHelper != null) logHelper.onExploded(x, y, z);
    }

    @Redirect(
            method = "explode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;explode(Lnet/minecraft/entity/Entity;DDDFZ)Lnet/minecraft/world/explosion/Explosion;"
            )
    )
    private Explosion explodeMerged(World world, Entity entity, double x, double y, double z, float strength, boolean damagesTerrain) {
        // Multi explode the amount of merged TNT CARPET-XCOM
        for (int i = 0; i < mergedTNT; i++) {
            world.explode(entity, x, y, z, strength, damagesTerrain);
        }
        return null;
    }
}
