package carpet.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;

public class VoidContainer extends ScreenHandler
{
    public VoidContainer()
    {
        super();
    }
    
    @Override
    public boolean canUse(PlayerEntity player)
    {
        return false;
    }
    
    @Override
    public void onContentChanged(Inventory inv)
    {
    
    }
}