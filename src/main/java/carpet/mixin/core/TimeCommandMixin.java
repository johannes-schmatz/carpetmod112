package carpet.mixin.core;

import net.minecraft.command.AbstractCommand;
import net.minecraft.command.CommandSource;
import net.minecraft.command.CommandStats;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.TimeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(TimeCommand.class)
public abstract class TimeCommandMixin extends AbstractCommand {
    @Inject(
            method = "method_3279",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/command/IncorrectUsageException"
            ),
            cancellable = true
    )
    private void queryServerTime(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci) {
        if (args.length >= 2 && "query".equals(args[0]) && "servertime".equals(args[1])) {
            int time = (int) (sender.getWorld().getLastUpdateTime() % Integer.MAX_VALUE);
            sender.setStat(CommandStats.Type.QUERY_RESULT, time);
            run(sender, this, "commands.time.query", time);
            ci.cancel();
        }
    }

    @Redirect(
            method = "method_10738",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/command/TimeCommand;method_2894([Ljava/lang/String;[Ljava/lang/String;)Ljava/util/List;",
                    ordinal = 2
            )
    )
    private List<String> tabCompleteServerTime(String[] args, String... possibilities) {
        String[] poss = Arrays.copyOfRange(possibilities, 0, possibilities.length + 1);
        poss[poss.length - 1] = "servertime";
        return method_2894(args, poss);
    }
}
