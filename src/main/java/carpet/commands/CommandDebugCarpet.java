package carpet.commands;

import org.jetbrains.annotations.Nullable;

import carpet.mixin.accessors.EntityTrackerAccessor;
import carpet.mixin.accessors.ServerWorldAccessor;
import carpet.utils.Messenger;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TrackedEntityInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ScheduledTick;
import java.util.Collections;
import java.util.List;

public class CommandDebugCarpet extends CommandCarpetBase {
    @Override
    public String getCommandName() {
        return "debugCarpet";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender) {
        return "Usage: debugCarpet <debug option>";
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if("tracker".equalsIgnoreCase(args[0])) {
            for(TrackedEntityInstance e : ((EntityTrackerAccessor) ((ServerWorld) sender.getWorld()).getEntityTracker()).getEntries()){
                sender.sendMessage(Messenger.s(sender, e.method_12794().toString()));
            }
        }
        if("trackedToMe".equalsIgnoreCase(args[0])) {
            for(TrackedEntityInstance e : ((EntityTrackerAccessor) ((ServerWorld) sender.getWorld()).getEntityTracker()).getEntries()){
                if(e.method_10770((ServerPlayerEntity) sender)){
                    sender.sendMessage(Messenger.s(sender, e.method_12794().toString()));
                }
            }
        }
        if("entitys".equalsIgnoreCase(args[0])) {
            for(Entity e : sender.getWorld().entities){
                sender.sendMessage(Messenger.s(sender, e.toString()));
            }
        }
        if("tileEntitys1".equalsIgnoreCase(args[0])) {
            for(BlockEntity e : sender.getWorld().blockEntities){
                sender.sendMessage(Messenger.s(sender, e.toString() + " " + e.getPos()));
            }
        }
        if("tileEntitys2".equalsIgnoreCase(args[0])) {
            for(BlockEntity e : sender.getWorld().tickingBlockEntities){
                sender.sendMessage(Messenger.s(sender, e.toString() + " " + e.getPos()));
            }
        }
        if("playerEntities".equalsIgnoreCase(args[0])) {
            for(PlayerEntity e : sender.getWorld().playerEntities){
                sender.sendMessage(Messenger.s(sender, e.toString()));
            }
        }
        if("pendingTickListEntriesTreeSet".equalsIgnoreCase(args[0])) {
            for(ScheduledTick e : ((ServerWorldAccessor)sender.getWorld()).getPendingTickListEntriesTreeSet()){
                sender.sendMessage(Messenger.s(sender, e.toString()));
            }
        }
    }

    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos)
    {
        if(args.length == 1) {
            return method_2894(args, "tracker", "entitys", "trackedToMe", "tileEntitys1", "tileEntitys2", "playerEntities", "pendingTickListEntriesTreeSet");
        }
        return Collections.emptyList();
    }
}
