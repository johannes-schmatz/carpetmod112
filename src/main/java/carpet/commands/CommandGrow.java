package carpet.commands;

import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandGrow extends CommandCarpetBase {
	private static final ItemStack STACK = new ItemStack(Items.DYE, 1, DyeColor.WHITE.getId());

	@Override
	public String getName() {
		return "grow";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return this.getName() + " <x> <y> <z> [times]";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!command_enabled("commandGrow", sender)) return;

		if (args.length < 3) {
			throw new IncorrectUsageException(this.getUsage(sender));
		}

		int amount = args.length > 3 ? parseInt(args[3]) : 1;
		BlockPos pos = parseBlockPos(sender, args, 0, false);
		World world = sender.getSourceWorld();

        if (world.isChunkLoaded(pos)) {
            for (int i = 0; i < amount; ++i) {
                DyeItem.fertilize(STACK, world, pos);
            }
        } else {
//            throw new class_6175("Position is not loaded!");
        }
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos) {
		if (args.length > 0 && args.length <= 3) {
			return suggestCoordinate(args, 0, pos);
		}

		return Collections.emptyList();
	}
}