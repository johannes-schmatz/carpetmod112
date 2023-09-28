package carpet.mixin.accessors;

import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {
    @Accessor int getAge();
    @Accessor void setAge(int age);
    @Accessor("pickUpDelay") int getPickupDelay();
    @Accessor("pickUpDelay") void setPickupDelay(int pickupDelay);
}
