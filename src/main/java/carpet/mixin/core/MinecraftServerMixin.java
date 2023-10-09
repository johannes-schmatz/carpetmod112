package carpet.mixin.core;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import carpet.helpers.ScoreboardDelta;
import carpet.helpers.TickSpeed;
import carpet.utils.CarpetProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerStatus;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.gen.WorldGeneratorType;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow @Final private static Logger LOGGER;
    @Shadow private int ticks;
    @Shadow @Final private ServerStatus status;
    @Shadow private boolean running;
    @Shadow private long nextTickTime;
    @Shadow private boolean loading;
    @Shadow private long lastWarnTime;
    @Shadow public ServerWorld[] worlds;
    @Shadow private boolean stopped;

    @Shadow public abstract void stop();
    @Shadow public abstract void exit();
    @Shadow public abstract void onServerCrashed(CrashReport report);
    @Shadow public abstract CrashReport populateCrashReport(CrashReport report);
    @Shadow public abstract File getRunDir();
    @Shadow public abstract void tick();
    @Shadow public abstract void setStatus(ServerStatus response);
    @Shadow public abstract boolean init() throws IOException;
    @Shadow public static long getTimeMillis() { throw new AbstractMethodError(); }

    @Shadow private String motd;

    @Inject(
            method = "loadWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;prepareWorlds()V"
            )
    )
    private void onLoadAllWorlds(String name, String serverName, long seed, WorldGeneratorType generatorType, String generatorOptions, CallbackInfo ci) {
        CarpetServer.getInstance().onLoadAllWorlds();
    }

    @Inject(
            method = "loadWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;prepareWorlds()V",
                    shift = At.Shift.AFTER
            )
    )
    private void loadCarpetBots(String saveName, String worldNameIn, long seed, WorldGeneratorType type, String generatorOptions, CallbackInfo ci) {
        CarpetServer.getInstance().loadBots();
    }

    @Inject(
            method = "saveWorlds",
            at = @At("RETURN")
    )
    private void onWorldsSaved(boolean isSilent, CallbackInfo ci) {
        CarpetServer.getInstance().onWorldsSaved();
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/MinecraftServer;ticks:I",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void startTick(CallbackInfo ci) {
        CarpetServer.getInstance().tick();
        if (CarpetProfiler.tick_health_requested != 0) {
            CarpetProfiler.start_tick_profiling();
        }
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void endTick(CallbackInfo ci) {
        // ChunkLogger - 0x-CARPET
        if(CarpetClientChunkLogger.logger.enabled) {
            CarpetClientChunkLogger.logger.sendAll();
        }

        if (CarpetProfiler.tick_health_requested != 0L)
        {
            CarpetProfiler.end_tick_profiling((MinecraftServer) (Object) this);
        }

        if(CarpetSettings.scoreboardDelta > 0 && this.ticks % 20 == 0){
            ScoreboardDelta.update();
        }
    }

    /**
     * @author gnembon
     * @reason carpet
     */
    @Overwrite
    public String getServerModName() {
        return "carpetmod";
    }

    /**
     * @author gnembon, X-com, skyrising, 0x53ee71ebe11e
     * @reason carpet
     */
    @Overwrite
    public void run() {
        try {
            if (this.init()) {
                this.nextTickTime = getTimeMillis();
                long msGoal = 0L;
                // carpet
                String motd = "_".equals(CarpetSettings.customMOTD) ? this.motd : CarpetSettings.customMOTD;
                // carpet end
                this.status.setDescription(new LiteralText(motd)); // was this.motd
                this.status.setVersion(new ServerStatus.Version("1.12.2", 340));
                this.setStatus(this.status);

                while(this.running) {
                    /* carpet mod commandTick */
                    //todo check if this check is necessary
                    if (TickSpeed.time_warp_start_time != 0) {
                        if (TickSpeed.continueWarp()) {
                            this.tick();
                            this.nextTickTime = getTimeMillis();
                            this.loading = true;
                        }
                        continue;
                    }
                    /* end */
                    long now = getTimeMillis();
                    long timeDelta = now - this.nextTickTime;
                    if (timeDelta > 2000L && this.nextTickTime - this.lastWarnTime >= 15000L) {
                        //LOGGER.warn( /* carpet */
                        //        "Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", timeDelta, timeDelta / 50L
                        //);
                        timeDelta = 2000L;
                        this.lastWarnTime = this.nextTickTime;
                    }

                    if (timeDelta < 0L) {
                        LOGGER.warn("Time ran backwards! Did the system time change?");
                        timeDelta = 0L;
                    }

                    msGoal += timeDelta;
                    this.nextTickTime = now;
                    boolean falling_behind = false; /* carpet mod */
                    if (this.worlds[0].canSkipNight()) {
                        this.tick();
                        msGoal = 0L;
                    } else {
                        //while(msGoal > 50L) { /* carpet */
                        //    msGoal -= 50L;
                        //    this.tick();
                        //}
                        boolean keeping_up = false; /* carpet */
                        while (msGoal > TickSpeed.mspt) /* carpet mod 50L -> TickSpeed.mspt */ {
                            msGoal -= TickSpeed.mspt; /* carpet mod 50L -> TickSpeed.mspt */
                            if (CarpetSettings.watchdogFix && keeping_up) { /* carpet */
                                this.nextTickTime = getTimeMillis();
                                this.loading = true;
                                falling_behind = true;
                            }
                            this.tick();
                            keeping_up = true; /* carpet */
                            if (CarpetSettings.disableVanillaTickWarp) { /* carpet */
                                msGoal = getTimeMillis() - now;
                                break;
                            }
                        }
                    }

                    //Thread.sleep(Math.max(1L, 50L - msGoal)); /* carpet */
                    if (falling_behind) {
                        Thread.sleep(1L); /* carpet mod 50L -> 1L */
                    } else {
                        Thread.sleep(Math.max(1L, TickSpeed.mspt - msGoal)); /* carpet mod 50L -> TickSpeed.mspt */
                    }
                    this.loading = true;
                }
            } else {
                this.onServerCrashed(null);
            }
        } catch (Throwable throwable) {
            LOGGER.error("Encountered an unexpected exception", throwable);
            CrashReport crashReport;
            if (throwable instanceof CrashException) {
                crashReport = this.populateCrashReport(((CrashException)throwable).getReport());
            } else {
                crashReport = this.populateCrashReport(new CrashReport("Exception in server tick loop", throwable));
            }

            File file = new File(
                    new File(this.getRunDir(), "crash-reports"), "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt"
            );
            if (crashReport.writeToFile(file)) {
                LOGGER.error("This crash report has been saved to: {}", file.getAbsolutePath());
            } else {
                LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.onServerCrashed(crashReport);
        } finally {
            try {
                this.stopped = true;
                this.stop();
            } catch (Throwable throwable) {
                LOGGER.error("Exception stopping the server", throwable);
            } finally {
                this.exit();
            }
        }
    }
}
