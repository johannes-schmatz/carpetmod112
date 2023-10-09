package carpet.helpers;

import carpet.CarpetSettings;
import carpet.mixin.accessors.LivingEntityAccessor;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.control.MovementControl;
import net.minecraft.entity.ai.goal.MobEntityPlayerTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.living.mob.GhastEntity;
import net.minecraft.entity.living.mob.MobEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GhastHelper {
	/*target selector to make sure no player with names is chosen
	 */
	public static class GhastEntityAIFindEntityNearestPlayer extends MobEntityPlayerTargetGoal {
		private final MobEntity entity;

		public GhastEntityAIFindEntityNearestPlayer(MobEntity entityLivingIn) {
			super(entityLivingIn);
			this.entity = entityLivingIn;
		}

		@Override
		public boolean canStart() {
			if (CarpetSettings.rideableGhasts && entity.hasCustomName()) {
				return false;
			}
			return super.canStart();
		}

		@Override
		public boolean shouldContinue() {
			if (CarpetSettings.rideableGhasts && entity.hasCustomName()) {
				return false;
			}
			return super.shouldContinue();
		}

	}

	public static boolean is_yo_bro(GhastEntity ghast, PlayerEntity player) {
		return (ghast.hasCustomName() && player.getGameProfile().getName().equals(ghast.getCustomName()));
	}

	public static boolean holds_yo_tear(PlayerEntity player) {
		return ((!player.getMainHandStack().isEmpty() && player.getMainHandStack().getItem() == Items.GHAST_TEAR) ||
				(!player.getOffHandStack().isEmpty() && player.getOffHandStack().getItem() == Items.GHAST_TEAR));
	}

	/*sets off fireball on demand
	 */
	public static void setOffFireBall(GhastEntity ghast, World world, PlayerEntity player) {
		world.doEvent(null, 1015, new BlockPos(ghast), 0);
		Vec3d vec3d = player.getRotationVec(1.0F);
		world.doEvent(null, 1016, new BlockPos(ghast), 0);
		FireballEntity fireball = new FireballEntity(world, player, 30.0 * vec3d.x, 30.0 * vec3d.y, 30.0 * vec3d.z);
		fireball.explosionPower = ghast.m_2353420();
		fireball.x = ghast.x + vec3d.x * 4.0D;
		fireball.y = ghast.y + (ghast.height / 2.0F) + vec3d.y * 4.0D + 0.5D;
		fireball.z = ghast.z + vec3d.z * 4.0D;
		world.addEntity(fireball);
	}

	/*rided ghast follows rider's tear clues
	 */
	public static class AIFollowClues extends Goal {
		private final GhastEntity parentEntity;
		private PlayerEntity rider = null;

		public AIFollowClues(GhastEntity ghast) {
			this.parentEntity = ghast;
			this.setControls(1);
		}

		public boolean canStart() {
			if (!CarpetSettings.rideableGhasts) {
				return false;
			}
			if (this.parentEntity.hasPassengers()) {
				Entity p = this.parentEntity.getControllingPassenger();
				if (p instanceof PlayerEntity) {
					if (holds_yo_tear((PlayerEntity) p)) {
						return true;
					}
				}
			}
			return false;
		}

		public void start() {
			rider = (PlayerEntity) this.parentEntity.getControllingPassenger();
		}

		public void stop() {
			this.rider = null;
		}

		public void tick() {
			float strafe = rider.sidewaysSpeed;
			float forward = rider.forwardSpeed;
			if (forward <= 0.0F) {
				forward *= 0.5F;
			}
			Vec3d vec3d = Vec3d.ZERO;
			if (forward != 0.0f) {
				vec3d = rider.getRotationVec(1.0F);
				if (forward < 0.0f) {
					vec3d = vec3d.subtractFrom(Vec3d.ZERO);
				}
			}
			if (strafe != 0.0f) {
				float c = MathHelper.cos(rider.yaw * 0.017453292F);
				float s = MathHelper.sin(rider.yaw * 0.017453292F);
				vec3d = new Vec3d(vec3d.x + c * strafe, vec3d.y, vec3d.z + s * strafe);
			}
			if (((LivingEntityAccessor) rider).isJumping()) {
				vec3d = new Vec3d(vec3d.x, vec3d.y + 1.0D, vec3d.z);
			}
			if (vec3d.equals(Vec3d.ZERO)) {
				this.parentEntity.getMovementControl().operation = MovementControl.Operation.WAIT;
			} else {
				this.parentEntity.getMovementControl()
						.update(this.parentEntity.x + vec3d.x, this.parentEntity.y + vec3d.y, this.parentEntity.z + vec3d.z, 1.0D);
			}
		}
	}

	/* homing abilities to find the player
	 */
	public static class AIFindOwner extends Goal {
		private final GhastEntity parentEntity;
		private PlayerEntity owner = null;

		public AIFindOwner(GhastEntity ghast) {
			this.parentEntity = ghast;
			this.setControls(1);
		}

		private @Nullable PlayerEntity findOwner() {
			if (!this.parentEntity.hasPassengers() && this.parentEntity.hasCustomName()) {
				PlayerEntity player = this.parentEntity.getSourceWorld().getServer().getPlayerManager().get(this.parentEntity.getCustomName());
				if (player != null && player.dimensionId == this.parentEntity.dimensionId && this.parentEntity.getSquaredDistanceTo(player) < 300.0D * 300.0D) {
					if (!(player.hasVehicle() && player.getVehicle() instanceof GhastEntity)) {
						if (this.parentEntity.getSquaredDistanceTo(player) > 10.0D * 10.0D && holds_yo_tear(player)) {
							return player;
						}
					}
				}
			}
			return null;
		}

		public boolean canStart() {
			if (!CarpetSettings.rideableGhasts) {
				return false;
			}
			if (owner != null) {
				owner = null;
				return false;
			}
			if (this.parentEntity.getRandom().nextInt(5) != 0) {
				return false;
			}

			owner = findOwner();
			return owner != null;
		}

		public void start() {
			continueExecuting();
		}

		public void stop() {
			this.owner = null;
		}

		public boolean continueExecuting() {
			if (owner != null && owner.dimensionId == this.parentEntity.dimensionId) {
				if (this.parentEntity.getSquaredDistanceTo(owner) > 50D && holds_yo_tear(owner)) {
					Vec3d target =
							new Vec3d(this.owner.x - this.parentEntity.x, this.owner.y - this.parentEntity.y, this.owner.z - this.parentEntity.z).normalize();
					this.parentEntity.getMovementControl()
							.update(this.parentEntity.x + target.x, this.parentEntity.y + target.y, this.parentEntity.z + target.z, 1.0D);
					return true;
				}
			}
			return false;
		}
	}
}
