package carpet.mixin.core;

import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.dedicated.command.SaveAllCommand;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SaveAllCommand.class)
public abstract class SaveAllCommandMixin extends AbstractCommand {
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
