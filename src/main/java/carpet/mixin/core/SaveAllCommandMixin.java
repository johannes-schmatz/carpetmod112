package carpet.mixin.core;

import net.minecraft.command.AbstractCommand;
import net.minecraft.server.dedicated.command.SaveAllCommand;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SaveAllCommand.class)
public abstract class SaveAllCommandMixin extends AbstractCommand {
    @Override
    public int getPermissionLevel() {
        return 2;
    }
}
