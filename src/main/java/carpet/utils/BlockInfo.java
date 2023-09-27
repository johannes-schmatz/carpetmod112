package carpet.utils;

import net.minecraft.block.Block;
import net.minecraft.block.sound.BlockSounds;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.ai.TargetFinder;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.block.state.property.BooleanProperty;
import net.minecraft.block.state.property.DirectionProperty;
import net.minecraft.block.state.property.IntegerProperty;
import net.minecraft.block.state.property.Property;
import net.minecraft.entity.living.mob.PathAwareEntity;
import net.minecraft.entity.living.mob.hostile.ZombiePigmanEntity;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockInfo
{
    public static String getSoundName(BlockSounds stype)
    {
        if (stype == BlockSounds.WOOD   ) { return "WOOD"  ;   }
        if (stype == BlockSounds.GRAVEL ) { return "GRAVEL";   }
        if (stype == BlockSounds.GRASS  ) { return "GRASS" ;   }
        if (stype == BlockSounds.STONE  ) { return "STONE" ;   }
        if (stype == BlockSounds.METAL  ) { return "METAL" ;   }
        if (stype == BlockSounds.GLASS  ) { return "GLASS" ;   }
        if (stype == BlockSounds.CLOTH  ) { return "WOOL"  ;   }
        if (stype == BlockSounds.SAND   ) { return "SAND"  ;   }
        if (stype == BlockSounds.SNOW   ) { return "SNOW"  ;   }
        if (stype == BlockSounds.LADDER ) { return "LADDER";   }
        if (stype == BlockSounds.ANVIL  ) { return "ANVIL" ;   }
        if (stype == BlockSounds.SLIME  ) { return "SLIME" ;   }
        return "Something new";
    }

    private static String getMapColourName(MaterialColor colour)
    {
        if (colour == MaterialColor.AIR        ) { return "AIR"        ; }
        if (colour == MaterialColor.GRASS      ) { return "GRASS"      ; }
        if (colour == MaterialColor.SAND       ) { return "SAND"       ; }
        if (colour == MaterialColor.WEB        ) { return "WOOL"       ; }
        if (colour == MaterialColor.LAVA       ) { return "TNT"        ; }
        if (colour == MaterialColor.ICE        ) { return "ICE"        ; }
        if (colour == MaterialColor.IRON       ) { return "IRON"       ; }
        if (colour == MaterialColor.FOLIAGE    ) { return "FOLIAGE"    ; }
        if (colour == MaterialColor.WHITE      ) { return "SNOW"       ; }
        if (colour == MaterialColor.CLAY       ) { return "CLAY"       ; }
        if (colour == MaterialColor.DIRT       ) { return "DIRT"       ; }
        if (colour == MaterialColor.STONE      ) { return "STONE"      ; }
        if (colour == MaterialColor.WATER      ) { return "WATER"      ; }
        if (colour == MaterialColor.WOOD       ) { return "WOOD"       ; }
        if (colour == MaterialColor.QUARTZ     ) { return "QUARTZ"     ; }
        if (colour == MaterialColor.ORANGE     ) { return "ADOBE"      ; }
        if (colour == MaterialColor.MAGENTA    ) { return "MAGENTA"    ; }
        if (colour == MaterialColor.LIGHT_BLUE ) { return "LIGHT_BLUE" ; }
        if (colour == MaterialColor.YELLOW     ) { return "YELLOW"     ; }
        if (colour == MaterialColor.LIME       ) { return "LIME"       ; }
        if (colour == MaterialColor.PINK       ) { return "PINK"       ; }
        if (colour == MaterialColor.GRAY       ) { return "GRAY"       ; }
        if (colour == MaterialColor.LIGHT_GRAY ) { return "SILVER"     ; }
        if (colour == MaterialColor.CYAN       ) { return "CYAN"       ; }
        if (colour == MaterialColor.PURPLE     ) { return "PURPLE"     ; }
        if (colour == MaterialColor.BLUE       ) { return "BLUE"       ; }
        if (colour == MaterialColor.BROWN      ) { return "BROWN"      ; }
        if (colour == MaterialColor.GREEN      ) { return "GREEN"      ; }
        if (colour == MaterialColor.RED        ) { return "RED"        ; }
        if (colour == MaterialColor.BLACK      ) { return "BLACK"      ; }
        if (colour == MaterialColor.GOLD       ) { return "GOLD"       ; }
        if (colour == MaterialColor.DIAMOND    ) { return "DIAMOND"    ; }
        if (colour == MaterialColor.LAPIS      ) { return "LAPIS"      ; }
        if (colour == MaterialColor.EMERALD    ) { return "EMERALD"    ; }
        if (colour == MaterialColor.SPRUCE     ) { return "OBSIDIAN"   ; }
        if (colour == MaterialColor.NETHER     ) { return "NETHERRACK" ; }
        return "Something new";
    }

    private static String getMaterialName(Material material)
    {
        if (material == Material.AIR               ) { return "AIR"            ; }
        if (material == Material.GRASS             ) { return "GRASS"          ; }
        if (material == Material.DIRT              ) { return "DIRT"           ; }
        if (material == Material.WOOD              ) { return "WOOD"           ; }
        if (material == Material.STONE             ) { return "STONE"          ; }
        if (material == Material.IRON              ) { return "IRON"           ; }
        if (material == Material.ANVIL             ) { return "ANVIL"          ; }
        if (material == Material.WATER             ) { return "WATER"          ; }
        if (material == Material.LAVA              ) { return "LAVA"           ; }
        if (material == Material.LEAVES            ) { return "LEAVES"         ; }
        if (material == Material.PLANT             ) { return "PLANTS"         ; }
        if (material == Material.REPLACEABLE_PLANT ) { return "VINE"           ; }
        if (material == Material.SPONGE            ) { return "SPONGE"         ; }
        if (material == Material.WOOL              ) { return "WOOL"           ; }
        if (material == Material.FIRE              ) { return "FIRE"           ; }
        if (material == Material.SAND              ) { return "SAND"           ; }
        if (material == Material.DECORATION        ) { return "REDSTONE_COMPONENT"; }
        if (material == Material.CARPET            ) { return "CARPET"         ; }
        if (material == Material.GLASS             ) { return "GLASS"          ; }
        if (material == Material.REDSTONE_LAMP     ) { return "REDSTONE_LAMP"  ; }
        if (material == Material.TNT               ) { return "TNT"            ; }
        if (material == Material.CORAL             ) { return "CORAL"          ; }
        if (material == Material.ICE               ) { return "ICE"            ; }
        if (material == Material.PACKED_ICE        ) { return "PACKED_ICE"     ; }
        if (material == Material.SNOW_LAYER        ) { return "SNOW_LAYER"     ; }
        if (material == Material.SNOW              ) { return "SNOW"           ; }
        if (material == Material.CACTUS            ) { return "CACTUS"         ; }
        if (material == Material.CLAY              ) { return "CLAY"           ; }
        if (material == Material.PUMPKIN           ) { return "GOURD"          ; }
        if (material == Material.EGG               ) { return "DRAGON_EGG"     ; }
        if (material == Material.PORTAL            ) { return "PORTAL"         ; }
        if (material == Material.CAKE              ) { return "CAKE"           ; }
        if (material == Material.COBWEB            ) { return "COBWEB"         ; }
        if (material == Material.PISTON            ) { return "PISTON"         ; }
        if (material == Material.BARRIER           ) { return "BARRIER"        ; }
        if (material == Material.STRUCTURE_VOID    ) { return "STRUCTURE"      ; }
        return "Something new";
    }

    public static <T extends Comparable<T>> Text formatBlockProperty(Property<T> prop, T value) {
        Text name = new LiteralText( prop.getName()+ "=");
        Text valueText = new LiteralText(prop.getName(value));
        if (prop instanceof DirectionProperty) valueText.getStyle().setColor(Formatting.GOLD);
        else if (prop instanceof BooleanProperty) valueText.getStyle().setColor((Boolean) value ? Formatting.GREEN :
                Formatting.RED);
        else if (prop instanceof IntegerProperty) valueText.getStyle().setColor(Formatting.GREEN);
        return name.append(valueText);
    }

    public static Text formatBoolean(boolean value) {
        Text component = new LiteralText(Boolean.toString(value));
        component.getStyle().setColor(value ? Formatting.GREEN : Formatting.RED);
        return component;
    }

    public static List<Text> blockInfo(BlockPos pos, World world) {
        BlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        Block block = state.getBlock();
        String metastring = "";
        if (block.getDropItemMetadata(state) != 0) {
            metastring = ":" + block.getDropItemMetadata(state);
        }
        Text stateInfo = new LiteralText("");
        boolean first = true;
        for (Map.Entry<Property<?>, Comparable<?>> entry : state.values().entrySet()) {
            if (!first) {
                stateInfo.append(", ");
            }
            first = false;
            stateInfo.append(formatBlockProperty((Property) entry.getKey(), (Comparable) entry.getValue()));
        }
        // TODO: rewrite with normal strings
        List<Text> lst = new ArrayList<>();
        lst.add(Messenger.s(null, ""));
        lst.add(Messenger.s(null, "====================================="));
        lst.add(Messenger.s(null, String.format("Block info for %s%s (id %d%s):",Block.REGISTRY.getKey(block),metastring, Block.getId(block), metastring )));
        lst.add(Messenger.m(null, "w  - State: ", stateInfo));
        lst.add(Messenger.s(null, String.format(" - Material: %s", getMaterialName(material))));
        lst.add(Messenger.s(null, String.format(" - Map colour: %s", getMapColourName(state.getMaterialColor(world, pos)))));
        lst.add(Messenger.s(null, String.format(" - Sound type: %s", getSoundName(block.getSounds()))));
        lst.add(Messenger.s(null, ""));
        lst.add(Messenger.m(null, "w  - Full block: ", formatBoolean(state.isFullBlock())));
        lst.add(Messenger.m(null, "w  - Full cube: ", formatBoolean(state.isFullCube())));
        lst.add(Messenger.m(null, "w  - Normal cube: ", formatBoolean(state.isConductor())));
        lst.add(Messenger.m(null, "w  - Block normal cube: ", formatBoolean(state.blocksAmbientLight())));
        lst.add(Messenger.m(null, "w  - Is liquid: ", formatBoolean(material.isLiquid())));
        lst.add(Messenger.m(null, "w  - Is solid: ", formatBoolean(material.isSolid())));
        lst.add(Messenger.s(null, ""));
        lst.add(Messenger.s(null, String.format(" - Light in: %d, above: %d", world.getLight(pos), world.getLight(pos.up()))));
        lst.add(Messenger.s(null, String.format(" - Brightness in: %.2f, above: %.2f", world.getBrightness(pos), world.getBrightness(pos.up()))));
        lst.add(Messenger.m(null, "w  - Is opaque: ", formatBoolean(material.isOpaque())));
        lst.add(Messenger.s(null, String.format(" - Light opacity: %d", state.getOpacity())));
        lst.add(Messenger.m(null, "w  - Blocks light: ", formatBoolean(material.isOpaque())));
        lst.add(Messenger.s(null, String.format(" - Emitted light: %d", state.getLightLevel())));
        lst.add(Messenger.m(null, "w  - Picks neighbour light value: ", formatBoolean(state.usesNeighborLight())));
        lst.add(Messenger.s(null, ""));
        lst.add(Messenger.m(null, "w  - Causes suffocation: ", formatBoolean(state.isViewBlocking())));
        lst.add(Messenger.m(null, "w  - Blocks movement: ", formatBoolean(!block.canWalkThrough(world, pos))));
        lst.add(Messenger.m(null, "w  - Can burn: ", formatBoolean(material.isFlammable())));
        lst.add(Messenger.m(null, "w  - Requires a tool: ", formatBoolean(!material.isToolNotRequired())));
        lst.add(Messenger.s(null, String.format(" - Hardness: %.2f", state.getMiningSpeed(world, pos))));
        lst.add(Messenger.s(null, String.format(" - Blast resistance: %.2f", block.getBlastResistance(null))));
        lst.add(Messenger.m(null, "w  - Ticks randomly: ", formatBoolean(block.ticksRandomly())));
        lst.add(Messenger.s(null, ""));
        lst.add(Messenger.m(null, "w  - Can provide power: ", formatBoolean(state.isSignalSource())));
        lst.add(Messenger.s(null, String.format(" - Strong power level: %d", world.getDirectNeighborSignal(pos))));
        lst.add(Messenger.s(null, String.format(" - Redstone power level: %d", world.getNeighborSignal(pos))));
        lst.add(Messenger.s(null, ""));
        lst.add(wander_chances(pos.up(), world));

        return lst;
    }

    private static Text wander_chances(BlockPos pos, World worldIn)
    {
        PathAwareEntity creature = new ZombiePigmanEntity(worldIn);
        creature.initialize(worldIn.getLocalDifficulty(pos), null);
        creature.refreshPositionAndAngles(pos.getX()+0.5F, pos.getY(), pos.getZ()+0.5F, 0.0F, 0.0F);
        WanderAroundGoal wander = new WanderAroundGoal(creature, 0.8D);
        int success = 0;
        for (int i=0; i<1000; i++)
        {

            Vec3d vec = TargetFinder.getTarget(creature, 10, 7);
            if (vec == null)
            {
                continue;
            }
            success++;
        }
        long total_ticks = 0;
        for (int trie=0; trie<1000; trie++)
        {
            int i;
            for (i=1;i<30*20*60; i++) //*60 used to be 5 hours, limited to 30 mins
            {
                if (wander.canStart())
                {
                    break;
                }
            }
            total_ticks += 3*i;
        }
        creature.remove();
        long total_time = (total_ticks)/1000/20;
        return Messenger.s(null, String.format(" - Wander chance above: %.1f%%\n - Average standby above: %s",
                (100.0F*success)/1000,
                ((total_time>5000)?"INFINITY":(total_time + " s"))
        ));
    }
}
