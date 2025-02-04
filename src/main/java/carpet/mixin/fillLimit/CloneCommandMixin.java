package carpet.mixin.fillLimit;

import carpet.CarpetSettings;
import net.minecraft.server.command.CloneCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CloneCommand.class)
public class CloneCommandMixin {
    @ModifyConstant(
            method = "run",
            constant = @Constant(intValue = 32768)
    )
    private int fillLimit(int orig) {
        return CarpetSettings.fillLimit;
    }
}
