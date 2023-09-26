package carpet.mixin.structureBlockLimit;

import carpet.CarpetSettings;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.text.TranslatableText;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.StructureTemplate;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow @Final private MinecraftServer server;

    @ModifyConstant(
            method = "handleCustomPayload",
            constant = {
                    @Constant(intValue = -32),
                    @Constant(intValue = 32)
            },
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/block/entity/StructureBlockEntity;setStructureName(Ljava/lang/String;)V"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/block/entity/StructureBlockEntity;setSize(Lnet/minecraft/util/math/BlockPos;)V"
                    )
            )
    )
    private int structureBlockLimit(int limit) {
        return limit < 0 ? -CarpetSettings.structureBlockLimit : CarpetSettings.structureBlockLimit;
    }

    // structure_block.load_prepare
    @Redirect(
            method = "handleCustomPayload",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/text/TranslatableText",
                    ordinal = 10
            )
    )
    private TranslatableText errorMessage(String message, Object[] args) {
        String structureName = (String) args[0];
        StructureTemplate template = server.worlds[0].getStructureManager().get(server, new Identifier(structureName));
        if (template != null) {
            int sbl = CarpetSettings.structureBlockLimit;
            BlockPos size = template.getSize();
            if (size.getX() > sbl || size.getY() > sbl || size.getZ() > sbl) {
                return new TranslatableText("Structure is too big for structure limit");
            }
        }
        return new TranslatableText(message, args);
    }
}
