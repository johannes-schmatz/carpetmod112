package carpet.mixin.accessors;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("mount") void setVehicle(Entity entity);
    @Accessor("inFirstTick") boolean isFirstUpdate();
    @Accessor("onFireTimer") int getFireTicks();
    @Invoker("setRotation") void invokeSetRotation(float yaw, float pitch);
    @Invoker("removePassenger") void invokeRemovePassenger(Entity passenger);
}
