package carpet.helpers;

import carpet.mixin.accessors.ExperienceOrbEntityAccessor;
import carpet.utils.extensions.ExtendedExperienceOrbEntity;

import net.minecraft.entity.XpOrbEntity;
import net.minecraft.world.World;

/**
 * @author Xcom
 */
public class XpCombine {
	public static void searchForOtherXPNearbyCarpet(XpOrbEntity first) {
		for (XpOrbEntity orb : first.world.getEntities(XpOrbEntity.class, first.getShape().grow(0.5D, 0.0D, 0.5D))) {
			combineItems(first, orb);
		}
	}

	private static boolean canCombine(XpOrbEntity first, XpOrbEntity other) {
		return first.isAlive()
				&& other.isAlive()
				&& first.pickupDelay != 32767
				&& other.pickupDelay != 32767
				&& first.orbAge != -32768
				&& other.orbAge != -32768
				&& ((ExtendedExperienceOrbEntity) first).getDelayBeforeCombine() == 0
				&& ((ExtendedExperienceOrbEntity) other).getDelayBeforeCombine() == 0;
	}

	private static boolean combineItems(XpOrbEntity first, XpOrbEntity other) {
		if (first == other) {
			return false;
		} else if (canCombine(first, other)) {
			int size = getTextureByXP(other.getXp());
			((ExperienceOrbEntityAccessor) other).setAmount(other.getXp() + first.getXp());
			other.pickupDelay = Math.max(other.pickupDelay, first.pickupDelay);
			other.orbAge = Math.min(other.orbAge, first.orbAge);
			if (getTextureByXP(other.getXp()) == size) {
				((ExtendedExperienceOrbEntity) other).setDelayBeforeCombine(50);
			} else {
				other.remove();
				first.world.addEntity(newXPOrb(other.world, other.getXp(), other));
			}
			first.remove();
			return true;
		} else {
			return false;
		}
	}

	private static XpOrbEntity newXPOrb(World world, int expValue, XpOrbEntity old) {
		XpOrbEntity orb = new XpOrbEntity(world, old.x, old.y, old.z, expValue);
		orb.yaw = old.yaw;
		orb.velocityX = old.velocityX;
		orb.velocityY = old.velocityY;
		orb.velocityZ = old.velocityZ;
		return orb;
	}

	public static int getTextureByXP(int value) {
		if (value >= 2477) {
			return 10;
		} else if (value >= 1237) {
			return 9;
		} else if (value >= 617) {
			return 8;
		} else if (value >= 307) {
			return 7;
		} else if (value >= 149) {
			return 6;
		} else if (value >= 73) {
			return 5;
		} else if (value >= 37) {
			return 4;
		} else if (value >= 17) {
			return 3;
		} else if (value >= 7) {
			return 2;
		} else {
			return value >= 3 ? 1 : 0;
		}
	}
}
