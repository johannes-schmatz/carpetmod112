package carpet.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandGrow extends CommandCarpetBase {
    private static final ItemStack STACK = new ItemStack(Items.DYE, 1, DyeColor.WHITE.getId());

    @Override
    public String getCommandName() {
        return "grow";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender) {
        return this.getCommandName() + " <x> <y> <z> [times]";
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if(!command_enabled("commandGrow", sender)) return;

        if (args.length < 3) {
            throw new IncorrectUsageException(this.getUsageTranslationKey(sender));
        }

        final int amount = args.length > 3 ? parseInt(args[3]) : 1;
        final BlockPos pos = getBlockPos(sender, args, 0, false);
        final World world = sender.getWorld();
        if (!world.blockExists(pos)) {
//            throw new class_6175("Position is not loaded!");
        } else {
            for (int i = 0; i < amount; ++i) {
                DyeItem.fertilize(STACK, world, pos);
            }
        }
    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos) {
        if (args.length > 0 && args.length <= 3) {
            return method_10707(args, 0, pos);
        }

        return Collections.emptyList();
    }
}