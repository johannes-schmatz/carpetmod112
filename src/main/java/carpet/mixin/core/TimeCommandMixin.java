package carpet.mixin.core;

import net.minecraft.server.command.Command;
import net.minecraft.server.command.source.CommandResults;
import net.minecraft.server.command.source.CommandSource;
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
public abstract class TimeCommandMixin extends Command {
    @Inject(
            method = "run",
            at = @At(
                    value = "NEW",
                    target = "(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/server/command/exception/IncorrectUsageException;"
            ),
            cancellable = true
    )
    private void queryServerTime(MinecraftServer server, CommandSource sender, String[] args, CallbackInfo ci) {
        if (args.length >= 2 && "query".equals(args[0]) && "servertime".equals(args[1])) {
            int time = (int) (sender.getSourceWorld().getTime() % Integer.MAX_VALUE);
            sender.addResult(CommandResults.Type.QUERY_RESULT, time);
            sendSuccess(sender, this, "commands.time.query", time);
            ci.cancel();
        }
    }

    @Redirect(
            method = "getSuggestions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/command/TimeCommand;suggestMatching([Ljava/lang/String;[Ljava/lang/String;)Ljava/util/List;",
                    ordinal = 2
            )
    )
    private List<String> tabCompleteServerTime(String[] args, String... possibilities) {
        String[] poss = Arrays.copyOfRange(possibilities, 0, possibilities.length + 1);
        poss[poss.length - 1] = "servertime";
        return suggestMatching(args, poss);
    }
}
