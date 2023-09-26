package carpet.mixin.publicKick;

import carpet.CarpetSettings;
import net.minecraft.server.command.Command;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.command.KickCommand;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KickCommand.class)
public abstract class KickCommandMixin extends Command {
    @Override
    public boolean canUse(MinecraftServer server, CommandSource sender) {
        return CarpetSettings.publicKick || super.canUse(server, sender);
    }
}
