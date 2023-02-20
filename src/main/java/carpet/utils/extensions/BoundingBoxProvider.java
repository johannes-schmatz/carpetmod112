package carpet.utils.extensions;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtList;

public interface BoundingBoxProvider {
    NbtList getBoundingBoxes(Entity entity);
}
