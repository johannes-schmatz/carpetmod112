package carpet.helpers;

import java.util.List;

import carpet.mixin.accessors.EntityAccessor;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

/**
 * @author Xcom
 */
public class BabyGrowingUp {
	public static void carpetSetSize(Entity entity, float width, float height) {
		float f = entity.width;
		entity.width = width;
		entity.height = height;
		Box oldAABB = entity.getShape();

		double d0 = width / 2.0D;
		entity.setShape(new Box(entity.x - d0, entity.y, entity.z - d0, entity.x + d0, entity.y + entity.height, entity.z + d0));

		if (entity.width > f && !((EntityAccessor) entity).isFirstUpdate() && !entity.world.isClient) {
			pushEntityOutOfBlocks(entity, oldAABB);
		}
	}

	private static void pushEntityOutOfBlocks(Entity entity, Box oldHitbox) {
		// Pass "null" in first argument to only get _possible_ block collisions
		List<Box> list1 = entity.world.getCollisions(null, entity.getShape());
		Box axisalignedbb = entity.getShape();

		for (Box aabb : list1) {
			if (!oldHitbox.intersects(aabb) && axisalignedbb.intersects(aabb)) {
				double minX = axisalignedbb.minX;
				double maxX = axisalignedbb.maxX;
				double minZ = axisalignedbb.minZ;
				double maxZ = axisalignedbb.maxZ;

				// Check for collisions on the X and Z axis, and only push the
				// new AABB if the colliding blocks AABB
				// is completely to the opposite side of the original AABB
				if (aabb.maxX > axisalignedbb.minX && aabb.minX < axisalignedbb.maxX) {
					if (aabb.maxX >= oldHitbox.minX && aabb.minX >= oldHitbox.maxX) {
						minX = aabb.minX - entity.width;
						maxX = aabb.minX;
					} else if (aabb.maxX <= oldHitbox.minX && aabb.minX <= oldHitbox.minX) {
						minX = aabb.maxX;
						maxX = aabb.maxX + entity.width;
					}
				}

				if (aabb.maxZ > axisalignedbb.minZ && aabb.minZ < axisalignedbb.maxZ) {
					if (aabb.minZ >= oldHitbox.maxZ && aabb.maxZ >= oldHitbox.maxZ) {
						minZ = aabb.minZ - entity.width;
						maxZ = aabb.minZ;
					} else if (aabb.maxZ <= oldHitbox.minZ && aabb.minZ <= oldHitbox.minZ) {
						minZ = aabb.maxZ;
						maxZ = aabb.maxZ + entity.width;
					}
				}

				axisalignedbb = new Box(minX, axisalignedbb.minY, minZ, maxX, axisalignedbb.maxY, maxZ);
			}
		}

		entity.setShape(axisalignedbb);
	}
}
