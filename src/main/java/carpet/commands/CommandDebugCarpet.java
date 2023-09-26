package carpet.commands;

import org.jetbrains.annotations.Nullable;

import carpet.mixin.accessors.EntityTrackerAccessor;
import carpet.mixin.accessors.ServerWorldAccessor;
import carpet.utils.Messenger;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.command.Command;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.EntityTrackerEntry;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ScheduledTick;
import java.util.Collections;
import java.util.List;

public class CommandDebugCarpet extends CommandCarpetBase {
    @Override
    public String getName() {
        return "debugCarpet";
    }

    @Override
    public String getUsage(CommandSource sender) {
        return "Usage: debugCarpet <debug option>";
    }

    @Override
    public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
        if("tracker".equalsIgnoreCase(args[0])) {
            for(EntityTrackerEntry e : ((EntityTrackerAccessor) ((ServerWorld) sender.getSourceWorld()).getEntityTracker()).getEntries()){
                sender.sendMessage(Messenger.s(sender, e.m_8045853().toString()));
            }
        }
        if("trackedToMe".equalsIgnoreCase(args[0])) {
            for(EntityTrackerEntry e : ((EntityTrackerAccessor) ((ServerWorld) sender.getSourceWorld()).getEntityTracker()).getEntries()){
                if(e.m_1373828((ServerPlayerEntity) sender)){
                    sender.sendMessage(Messenger.s(sender, e.m_8045853().toString()));
                }
            }
        }
        if("entitys".equalsIgnoreCase(args[0])) {
            for(Entity e : sender.getSourceWorld().entities){
                sender.sendMessage(Messenger.s(sender, e.toString()));
            }
        }
        if("tileEntitys1".equalsIgnoreCase(args[0])) {
            for(BlockEntity e : sender.getSourceWorld().blockEntities){
                sender.sendMessage(Messenger.s(sender, e.toString() + " " + e.getPos()));
            }
        }
        if("tileEntitys2".equalsIgnoreCase(args[0])) {
            for(BlockEntity e : sender.getSourceWorld().tickingBlockEntities){
                sender.sendMessage(Messenger.s(sender, e.toString() + " " + e.getPos()));
            }
        }
        if("playerEntities".equalsIgnoreCase(args[0])) {
            for(PlayerEntity e : sender.getSourceWorld().players){
                sender.sendMessage(Messenger.s(sender, e.toString()));
            }
        }
        if("pendingTickListEntriesTreeSet".equalsIgnoreCase(args[0])) {
            for(ScheduledTick e : ((ServerWorldAccessor)sender.getSourceWorld()).getPendingTickListEntriesTreeSet()){
                sender.sendMessage(Messenger.s(sender, e.toString()));
            }
        }
    }

    @Override
    public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos)
    {
        if(args.length == 1) {
            return suggestMatching(args, "tracker", "entitys", "trackedToMe", "tileEntitys1", "tileEntitys2", "playerEntities", "pendingTickListEntriesTreeSet");
        }
        return Collections.emptyList();
    }
}
