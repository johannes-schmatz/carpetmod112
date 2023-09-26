package carpet.mixin.accessors;

import net.minecraft.client.gui.overlay.PlayerTabOverlay;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerTabOverlay.class)
public interface PlayerListHudAccessor {
    @Accessor("header")
    Text getHeader();
    @Accessor("footer")
    Text getFooter();
}
