package carpet.commands;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import carpet.mixin.accessors.StructureManagerAccessor;
import org.apache.logging.log4j.LogManager;

import net.minecraft.class_2765;
import net.minecraft.util.SharedConstants;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.text.LiteralText;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.class_2763;

public class CommandStructure extends CommandCarpetBase
{

    private static final String USAGE = "/structure <load|save|list> ...";
    private static final String USAGE_LOAD = "/structure load <name> [pos: x y z] [mirror] [rotation] [ignoreEntities] [integrity] [seed]";
    private static final String USAGE_SAVE = "/structure save <name> <from: x y z> <to: x y z> [ignoreEntities]";
    
    @Override
    public String getCommandName()
    {
        return "structure";
    }

    @Override
    public String getUsageTranslationKey(CommandSource sender)
    {
        return USAGE;
    }

    @Override
    public void method_3279(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (!command_enabled("commandStructure", sender))
            return;
        
        if (args.length < 1)
            throw new IncorrectUsageException(USAGE);
        
        switch (args[0])
        {
        case "load":
            loadStructure(server, sender, args);
            break;
        case "save":
            saveStructure(server, sender, args);
            break;
        case "list":
            listStructure(server, sender, args);
            break;
        default:
            throw new IncorrectUsageException(USAGE);
        }
    }
    
    private void loadStructure(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw new IncorrectUsageException(USAGE_LOAD);
        
        args = replaceQuotes(args);
        
        String structureName = args[1];

        for (char illegal : SharedConstants.field_14996)
            structureName = structureName.replace(illegal, '_');
        class_2763 manager = server.worlds[0].method_12783();
        class_2765 template = manager.method_11861(server, new Identifier(structureName));
        if (template == null)
            throw new CommandException("Template \"" + args[1] + "\" doesn't exist");
        
        BlockPos origin = sender.getBlockPos();
        if (args.length >= 5)
        {
            origin = getBlockPos(sender, args, 2, false);
        }
        
        BlockMirror mirror = BlockMirror.NONE;
        if (args.length >= 6)
        {
            switch (args[5])
            {
            case "no_mirror":
                mirror = BlockMirror.NONE;
                break;
            case "mirror_left_right":
                mirror = BlockMirror.LEFT_RIGHT;
                break;
            case "mirror_front_back":
                mirror = BlockMirror.FRONT_BACK;
                break;
            default:
                throw new CommandException("Unknown mirror: " + args[5]);
            }
        }
        
        BlockRotation rotation = BlockRotation.NONE;
        if (args.length >= 7)
        {
            switch (args[6])
            {
            case "rotate_0":
                rotation = BlockRotation.NONE;
                break;
            case "rotate_90":
                rotation = BlockRotation.CLOCKWISE_90;
                break;
            case "rotate_180":
                rotation = BlockRotation.CLOCKWISE_180;
                break;
            case "rotate_270":
                rotation = BlockRotation.COUNTERCLOCKWISE_90;
                break;
            default:
                throw new CommandException("Unknown rotation: " + args[6]);
            }
        }
        
        boolean ignoreEntities = true;
        if (args.length >= 8)
        {
            ignoreEntities = parseBoolean(args[7]);
        }
        
        float integrity = 1;
        if (args.length >= 9)
        {
            integrity = (float) parseClampedDouble(args[8], 0, 1);
        }
        
        long seed;
        if (args.length >= 10)
        {
            seed = parseLong(args[9]);
        }
        else
        {
            seed = new Random().nextLong();
        }
        
        StructurePlacementData settings = new StructurePlacementData().method_11867(mirror).method_11868(rotation).method_11870(ignoreEntities).method_11866(null).method_11865(null).method_11873(false);
        if (integrity < 1)
        {
            settings.method_13385(integrity).method_13387(seed);
        }
        
        template.method_11882(sender.getWorld(), origin, settings);
        
        run(sender, this, "Successfully loaded structure " + args[1]);
    }
    
    private void saveStructure(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        if (args.length < 8)
            throw new IncorrectUsageException(USAGE_SAVE);
        
        args = replaceQuotes(args);
        
        BlockPos pos1 = getBlockPos(sender, args, 2, false);
        BlockPos pos2 = getBlockPos(sender, args, 5, false);
        
        BlockBox bb = new BlockBox(pos1, pos2);
        BlockPos origin = new BlockPos(bb.minX, bb.minY, bb.minZ);
        BlockPos size = new BlockPos(bb.getBlockCountX(), bb.getBlockCountY(), bb.getBlockCountZ());
        
        boolean ignoreEntities = true;
        if (args.length >= 9)
        {
            ignoreEntities = parseBoolean(args[8]);
        }
        
        String structureName = args[1];
        for (char illegal : SharedConstants.field_14996)
            structureName = structureName.replace(illegal, '_');
        class_2763 manager = server.worlds[0].method_12783();
        class_2765 template = manager.method_11861(server, new Identifier(structureName));
        template.method_11884(sender.getWorld(), origin, size, !ignoreEntities, Blocks.STRUCTURE_VOID);
        template.method_11892(sender.getTranslationKey());
        manager.method_11863(server, new Identifier(structureName));
        
        run(sender, this, "Successfully saved structure " + structureName);
    }
    
    private void listStructure(MinecraftServer server, CommandSource sender, String[] args) throws CommandException
    {
        class_2763 manager = server.worlds[0].method_12783();
        List<String> templates = listStructures(manager);
        
        if (templates.isEmpty())
        {
            sender.sendMessage(new LiteralText("There are no saved structures yet"));
        }
        else
        {
            final int PAGE_SIZE = 9;
            int pageCount = (templates.size() + PAGE_SIZE - 1) / PAGE_SIZE;
            int page = args.length >= 2 ? parseInt(args[1]) - 1 : 0;
            page = MathHelper.clamp(page, 0, pageCount - 1);
            
            sender.sendMessage(new LiteralText(Formatting.GREEN + "Structure list page " + (page + 1) + " of " + pageCount + " (/structure list <page>)"));
            for (int offset = 0; offset < PAGE_SIZE && page * PAGE_SIZE + offset < templates.size(); offset++)
            {
                String template = templates.get(page * PAGE_SIZE + offset);
                sender.sendMessage(new LiteralText("- " + template + " by " + manager.method_11861(server, new Identifier(template)).method_11895()));
            }
        }
    }
    
    private static List<String> listStructures(class_2763 manager)
    {
        List<String> templates = new ArrayList<>();
        
        Path baseFolder = Paths.get(((StructureManagerAccessor) manager).getBaseFolder());
        try
        {
            if (Files.exists(baseFolder))
            {
                Files.find(baseFolder, Integer.MAX_VALUE,
                        (path, attr) -> attr.isRegularFile() && path.getFileName().toString().endsWith(".nbt"))
                .forEach(path -> templates.add(baseFolder.relativize(path).toString().replace(File.separator, "/").replace(".nbt", "")));
            }
        }
        catch (IOException e)
        {
            LogManager.getLogger().error("Unable to list custom structures", e);
        }

        Pattern pattern = Pattern.compile(".*assets/(\\w+)/structures/([\\w/]+)\\.nbt");
        try
        {
            try
            {
                // try zip file first
                ZipFile zip = new ZipFile(new File(MinecraftServer.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements())
                {
                    String entryName = entries.nextElement().getName();
                    Matcher matcher = pattern.matcher(entryName);
                    if (matcher.matches())
                    {
                        templates.add(matcher.group(1) + ":" + matcher.group(2));
                    }
                }
                zip.close();
            }
            catch (Exception e)
            {
                // try folder
                Path root = Paths.get(MinecraftServer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                Files.find(root, Integer.MAX_VALUE,
                        (path, attr) -> attr.isRegularFile() && pattern.matcher(path.toString().replace(File.separator, "/")).matches())
                .forEach(path -> {
                    Matcher matcher = pattern.matcher(path.toString().replace(File.separator, "/"));
                    matcher.matches(); // == true
                    templates.add(matcher.group(1) + ":" + matcher.group(2));
                });
            }
        }
        catch (IOException | URISyntaxException e)
        {
            LogManager.getLogger().error("Unable to list built in structures", e);
        }
        
        Collections.sort(templates);
        
        return templates;
    }
    
    @Override
    public List<String> method_10738(MinecraftServer server, CommandSource sender, String[] args, BlockPos targetPos)
    {
        if (args.length == 0)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1)
        {
            return method_2894(args, "load", "save", "list");
        }
        else if ("load".equals(args[0]) || "save".equals(args[0]))
        {
            if (args[1].startsWith("\""))
            {
                boolean replaced = false;
                try
                {
                    if (!args[args.length - 1].endsWith("\""))
                    {
                        args = replaceQuotes(args);
                        replaced = true;
                    }
                }
                catch (CommandException e)
                {
                }
                if (!replaced)
                {
                    String commonPrefix = Arrays.stream(args).skip(1).limit(args.length - 2).collect(Collectors.joining(" "));
                    List<String> structs = listStructures(server.worlds[0].method_12783());
                    structs = structs.stream().map(s -> "\"" + s + "\"").collect(Collectors.toList());
                    if (!commonPrefix.isEmpty())
                        structs = structs.stream().filter(s -> s.startsWith(commonPrefix + " ")).map(s -> s.substring(commonPrefix.length() + 1)).collect(Collectors.toList());
                    structs = structs.stream().map(s -> s.split(" ")[0]).collect(Collectors.toList());
                    return method_10708(args, structs);
                }
            }
            else if (args.length == 2)
            {
                return method_10708(args, listStructures(server.worlds[0].method_12783()).stream().filter(s -> !s.contains(" ")).collect(Collectors.toList()));
            }
            
            if (args.length >= 3 && args.length <= 5)
            {
                return method_10707(args, 2, targetPos);
            }
            else if ("load".equals(args[0]))
            {
                if (args.length == 6)
                {
                    return method_2894(args, "no_mirror", "mirror_left_right", "mirror_front_back");
                }
                else if (args.length == 7)
                {
                    return method_2894(args, "rotate_0", "rotate_90", "rotate_180", "rotate_270");
                }
                else if (args.length == 8)
                {
                    return method_2894(args, "true", "false");
                }
                else
                {
                    return Collections.emptyList();
                }
            }
            else
            {
                if (args.length >= 6 && args.length <= 8)
                {
                    return method_10707(args, 5, targetPos);
                }
                else if (args.length == 9)
                {
                    return method_2894(args, "true", "false");
                }
                else
                {
                    return Collections.emptyList();
                }
            }
        }
        else
        {
            return Collections.emptyList();
        }
    }
    
    private static String[] replaceQuotes(String[] args) throws CommandException
    {
        String structureName = args[1];
        if (structureName.startsWith("\""))
        {
            int i = 2;
            while (!structureName.endsWith("\"") && i < args.length) {
                structureName += " " + args[i++];
            }
            if (!structureName.endsWith("\""))
                throw new CommandException("Unbalanced \"\" quotes");
            structureName = structureName.substring(1, structureName.length() - 1);
            String[] newArgs = new String[args.length - (i - 2)];
            newArgs[0] = args[0];
            newArgs[1] = structureName;
            System.arraycopy(args, i, newArgs, 2, newArgs.length - 2);
            return newArgs;
        }
        else
        {
            return args;
        }
    }

}
