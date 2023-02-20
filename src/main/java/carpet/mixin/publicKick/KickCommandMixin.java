package carpet.mixin.publicKick;

import carpet.CarpetSettings;
import net.minecraft.command.AbstractCommand;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.KickCommand;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KickCommand.class)
public abstract class KickCommandMixin extends AbstractCommand {
    @Override
    public boolean method_3278(MinecraftServer server, CommandSource sender) {
        return CarpetSettings.publicKick || super.method_3278(server, sender);
    }
}
