package carpet.mixin.core;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.Bootstrap;
import net.minecraft.util.crash.CrashReport;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
	/**
	 * @implNote Text about this fix written by Myren:
	 * <p>
	 * It fixes the crash reports of stack overflows.
	 * <p>
	 * Usually in 1.12.2 the CrashReport class is not pre-loaded.
	 * Also, every single block update has its own try-catch clause.
	 * <p>
	 * So when you get a StackOverflow from a long block update chain,
	 * then the catch clause from the last block update will catch it, and try to make a crash report.
	 * Now when we are in the catch clause from the last block update,
	 * then the stack is still almost completely full.
	 * <p>
	 * If the CrashReport class is already pre-loaded, like in the fix, then this is no problem,
	 * and you get a correct crash report for your block update chain.
	 * <p>
	 * But if the CrashReport class is not pre-loaded, like in vanilla,
	 * then the game has to load that class, which increases stacksize and leads to another stack overflow.
	 * <p>
	 * The game will then iterate through the catch clauses of all of the last block updates,
	 * and in each of those catch clauses, it will try to load in the CrashReport class
	 * but fail and cause another stack overflow while doing so.
	 * This continues, until so much stack space has been freed up,
	 * that some catch clause manages to finally load in the CrashReport class successfully, and create a crash report about the last stack overflow.
	 * <p>
	 * The end result is that you do not get a crash report for the first stack overflow which you are interested in,
	 * but instead you get a crash report simply telling you that the game crashed while trying to make a crash report,
	 * which is not very informative.
	 * <p>
	 * <a href="https://discord.com/channels/961702940102504518/961702940102504521/1078400009961226331">See this message in the Threadstone Archive for more information </a>
	 */
	@Inject(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/Bootstrap;wrapPrintStreams()V",
					shift = At.Shift.BEFORE
			)
	)
	private static void onInitialize(CallbackInfo ci) {
		// Fix the crash report without carpet rule. DON'T PUT A GOD-DAMN CARPET RULE FOR THIS OR I WILL GET MAD!
		// Mojang even added this in 16. CARPET-XCOM
		CrashReport.of(new Throwable("Dummy"), "Dummy");
	}
}
