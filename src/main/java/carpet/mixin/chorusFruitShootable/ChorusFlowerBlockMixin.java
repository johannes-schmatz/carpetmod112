package carpet.mixin.chorusFruitShootable;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChorusFlowerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChorusFlowerBlock.class)
public class ChorusFlowerBlockMixin extends Block {

    protected ChorusFlowerBlockMixin(Material materialIn) {
        super(materialIn);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, BlockState state, Entity entityIn) {
        if(!CarpetSettings.chorusFruitShootable || !(entityIn instanceof net.minecraft.entity.projectile.AbstractArrowEntity)) return;
        worldIn.setBlockState(pos, Blocks.AIR.defaultState(), 2);
        dropItems(worldIn, pos, new ItemStack(Item.byBlock(this)));
    }
}
