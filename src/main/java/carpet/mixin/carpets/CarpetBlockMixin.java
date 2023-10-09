package carpet.mixin.carpets;

import carpet.utils.WoolTool;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.property.EnumProperty;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CarpetBlock.class)
public class CarpetBlockMixin extends Block {
    protected CarpetBlockMixin(Material materialIn) {
        super(materialIn);
    }

    @Override
    public BlockState getPlacementState(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        BlockState state = super.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
        if (placer instanceof PlayerEntity) {
            WoolTool.carpetPlacedAction(state.get(CarpetBlock.COLOR), (PlayerEntity) placer, pos, worldIn);
        }
        return state;
    }
}
