package carpet.commands;

import net.minecraft.server.command.handler.CommandRegistry;

public class CarpetCommands {
    public static void register(CommandRegistry handler) {
        // Sorted alphabetically to make merge conflicts less likely
        // For Xcom: A B C D E F G H I J K L M N O P Q R S T U V W X Y Z

        handler.register(new CommandAutosave());
        handler.register(new CommandBlockInfo());
        handler.register(new CommandCarpet());
        handler.register(new CommandChunk());
        handler.register(new CommandColon());
        handler.register(new CommandCounter());
        handler.register(new CommandDebugCarpet());
        handler.register(new CommandDebuglogger());
        handler.register(new CommandDistance());
        handler.register(new CommandEntityInfo());
        handler.register(new CommandFeel());
        handler.register(new CommandFillBiome());
        handler.register(new CommandGMC());
        handler.register(new CommandGMS());
        handler.register(new CommandGrow());
        handler.register(new CommandLagSpike());
        handler.register(new CommandLazyChunkBehavior());
        handler.register(new CommandLight());
        handler.register(new CommandLoadedChunks());
        handler.register(new CommandLog());
        handler.register(new CommandPalette());
        handler.register(new CommandPerimeter());
        handler.register(new CommandPing());
        handler.register(new CommandPlayer());
        handler.register(new CommandProfile());
        handler.register(new CommandRemoveEntity());
        handler.register(new CommandRepopulate());
        handler.register(new CommandRNG());
        handler.register(new CommandScoreboardPublic());
        handler.register(new CommandSpawn());
        handler.register(new CommandStructure());
        handler.register(new CommandSubscribe());
        handler.register(new CommandTick());
        handler.register(new CommandTickingArea());
        handler.register(new CommandTNT());
        handler.register(new CommandUnload());
        handler.register(new CommandUnload13());
        handler.register(new CommandWaypoint());
        handler.register(new CommandZetBlock());

        handler.register(new FallingBlockHelperCommand());
    }
}
