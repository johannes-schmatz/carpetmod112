package carpet.helpers;

import carpet.CarpetSettings;
import carpet.mixin.accessors.EntityAccessor;
import carpet.mixin.accessors.PlayerActionC2SPacketAccessor;
import com.google.common.base.Predicate;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFilter;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerHandActionC2SPacket;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

import java.util.List;

public class EntityPlayerActionPack {
	private final ServerPlayerEntity player;

	private boolean doesAttack;
	private int attackInterval;
	private int attackCooldown;

	private boolean doesUse;
	private int useInterval;
	private int useCooldown;

	private boolean doesJump;
	private int jumpInterval;
	private int jumpCooldown;

	private BlockPos currentBlock = new BlockPos(-1, -1, -1);
	private int blockHitDelay;
	private boolean isHittingBlock;
	private float curBlockDamageMP;

	private boolean sneaking;
	private boolean sprinting;
	private float forward;
	private float strafing;

	public EntityPlayerActionPack(ServerPlayerEntity playerIn) {
		player = playerIn;
		stop();
	}

	public void copyFrom(EntityPlayerActionPack other) {
		doesAttack = other.doesAttack;
		attackInterval = other.attackInterval;
		attackCooldown = other.attackCooldown;

		doesUse = other.doesUse;
		useInterval = other.useInterval;
		useCooldown = other.useCooldown;

		doesJump = other.doesJump;
		jumpInterval = other.jumpInterval;
		jumpCooldown = other.jumpCooldown;


		currentBlock = other.currentBlock;
		blockHitDelay = other.blockHitDelay;
		isHittingBlock = other.isHittingBlock;
		curBlockDamageMP = other.curBlockDamageMP;

		sneaking = other.sneaking;
		sprinting = other.sprinting;
		forward = other.forward;
		strafing = other.strafing;
	}

	@Override
	public String toString() {
		return (doesAttack ? "t" : "f")
				+ ":" + attackInterval
				+ ":" + attackCooldown
				+ ":" + (doesUse ? "t" : "f")
				+ ":" + useInterval
				+ ":" + useCooldown
				+ ":" + (doesJump ? "t" : "f")
				+ ":" + jumpInterval
				+ ":" + jumpCooldown
				+ ":" + (sneaking ? "t" : "f")
				+ ":" + (sprinting ? "t" : "f")
				+ ":" + forward
				+ ":" + strafing;
	}

	public void fromString(String s) {
		String[] list = s.split(":");
		doesAttack = list[0].equals("t");
		attackInterval = Integer.parseInt(list[1]);
		attackCooldown = Integer.parseInt(list[2]);
		doesUse = list[3].equals("t");
		useInterval = Integer.parseInt(list[4]);
		useCooldown = Integer.parseInt(list[5]);
		doesJump = list[6].equals("t");
		jumpInterval = Integer.parseInt(list[7]);
		jumpCooldown = Integer.parseInt(list[8]);
		sneaking = list[9].equals("t");
		sprinting = list[10].equals("t");
		forward = Float.parseFloat(list[11]);
		strafing = Float.parseFloat(list[12]);
	}

	public EntityPlayerActionPack setAttack(int interval, int offset) {
		if (interval < 1) {
			CarpetSettings.LOG.error("attack interval needs to be positive");
			return this;
		}
		this.doesAttack = true;
		this.attackInterval = interval;
		this.attackCooldown = interval + offset;
		return this;
	}

	public EntityPlayerActionPack setUse(int interval, int offset) {
		if (interval < 1) {
			CarpetSettings.LOG.error("use interval needs to be positive");
			return this;
		}
		this.doesUse = true;
		this.useInterval = interval;
		this.useCooldown = interval + offset;
		return this;
	}

	public EntityPlayerActionPack setUseForever() {
		this.doesUse = true;
		this.useInterval = 1;
		this.useCooldown = 1;
		return this;
	}

	public EntityPlayerActionPack setAttackForever() {
		this.doesAttack = true;
		this.attackInterval = 1;
		this.attackCooldown = 1;
		return this;
	}

	public EntityPlayerActionPack setJump(int interval, int offset) {
		if (interval < 1) {
			CarpetSettings.LOG.error("jump interval needs to be positive");
			return this;
		}
		this.doesJump = true;
		this.jumpInterval = interval;
		this.jumpCooldown = interval + offset;
		return this;
	}

	public EntityPlayerActionPack setJumpForever() {
		this.doesJump = true;
		this.jumpInterval = 1;
		this.jumpCooldown = 1;
		return this;
	}

	public EntityPlayerActionPack setSneaking(boolean doSneak) {
		sneaking = doSneak;
		player.setSneaking(doSneak);
		if (sprinting && sneaking) setSprinting(false);
		return this;
	}

	public EntityPlayerActionPack setSprinting(boolean doSprint) {
		sprinting = doSprint;
		player.setSprinting(doSprint);
		if (sneaking && sprinting) setSneaking(false);
		return this;
	}

	public EntityPlayerActionPack setForward(float value) {
		forward = value;
		return this;
	}

	public EntityPlayerActionPack setStrafing(float value) {
		strafing = value;
		return this;
	}

	public boolean look(String where) {
		switch (where) {
			case "north":
				look(180.0f, 0.0F);
				return true;
			case "south":
				look(0.0F, 0.0F);
				return true;
			case "east":
				look(-90.0F, 0.0F);
				return true;
			case "west":
				look(90.0F, 0.0F);
				return true;
			case "up":
				look(player.yaw, -90.0F);
				return true;
			case "down":
				look(player.yaw, 90.0F);
				return true;
			case "left":
			case "right":
				return turn(where);
		}
		return false;
	}

	public EntityPlayerActionPack look(float yaw, float pitch) {
		((EntityAccessor) player).invokeSetRotation(yaw, MathHelper.clamp(pitch, -90.0F, 90.0F));
		return this;
	}

	public boolean turn(String where) {
		switch (where) {
			case "left":
				turn(-90.0F, 0.0F);
				return true;
			case "right":
				turn(90.0F, 0.0F);
				return true;
			case "up":
				turn(0.0F, -5.0F);
				return true;
			case "down":
				turn(0.0F, 5.0F);
				return true;
		}
		return false;
	}

	public EntityPlayerActionPack turn(float yaw, float pitch) {
		((EntityAccessor) player).invokeSetRotation(player.yaw + yaw, MathHelper.clamp(player.pitch + pitch, -90.0F, 90.0F));
		return this;
	}


	public EntityPlayerActionPack stop() {
		this.doesUse = false;
		this.doesAttack = false;
		this.doesJump = false;
		resetBlockRemoving();
		setSneaking(false);
		setSprinting(false);
		forward = 0.0F;
		strafing = 0.0F;
		player.setJumping(false);


		return this;
	}

	public void swapHands() {
		player.networkHandler.handlePlayerHandAction(createDiggingPacket(PlayerHandActionC2SPacket.Action.SWAP_HELD_ITEMS, null, null));
	}

	public void dropItem() {
		player.networkHandler.handlePlayerHandAction(createDiggingPacket(PlayerHandActionC2SPacket.Action.DROP_ITEM, null, null));
	}

	public void mount() {
		List<Entity> entities = player.world.getEntities(player, player.getShape().grow(3.0D, 1.0D, 3.0D), other -> !(other instanceof PlayerEntity));
		if (entities.isEmpty()) {
			return;
		}
		Entity closest = entities.get(0);
		double distance = player.getSquaredDistanceTo(closest);
		for (Entity e : entities) {
			double dd = player.getSquaredDistanceTo(e);
			if (dd < distance) {
				distance = dd;
				closest = e;
			}
		}
		player.startRiding(closest, true);
	}

	public void dismount() {
		player.stopRiding();
	}

	public void onUpdate() {
		if (doesJump) {
			if (--jumpCooldown == 0) {
				jumpCooldown = jumpInterval;
				//jumpOnce();
				player.setJumping(true);
			} else {
				player.setJumping(false);
			}
		}

		boolean used = false;

		if (doesUse && (--useCooldown) == 0) {
			useCooldown = useInterval;
			used = useOnce();
		}
		if (doesAttack) {
			if ((--attackCooldown) == 0) {
				attackCooldown = attackInterval;
				if (!(used)) attackOnce();
			} else {
				resetBlockRemoving();
			}
		}
		if (forward != 0.0F) {
			//CarpetSettings.LOG.error("moving it forward");
			player.forwardSpeed = forward * (sneaking ? 0.3F : 1.0F);
		}
		if (strafing != 0.0F) {
			player.sidewaysSpeed = strafing * (sneaking ? 0.3F : 1.0F);
		}
	}

	public void jumpOnce() {
		if (player.onGround) {
			player.jump();
		}
	}

	public void attackOnce() {
		HitResult raytraceresult = mouseOver();
		if (raytraceresult == null) return;

		switch (raytraceresult.type) {
			case ENTITY:
				player.attack(raytraceresult.entity);
				this.player.swingHand(InteractionHand.MAIN_HAND);
				break;
			case MISS:
				break;
			case BLOCK:
				BlockPos blockpos = raytraceresult.getPos();
				if (player.getSourceWorld().getBlockState(blockpos).getMaterial() != Material.AIR) {
					onPlayerDamageBlock(blockpos, raytraceresult.face.getOpposite());
					this.player.swingHand(InteractionHand.MAIN_HAND);
					break;
				}
		}
	}

	public boolean useOnce() {
		HitResult hitResult = mouseOver();
		for (InteractionHand enumhand : InteractionHand.values()) {
			ItemStack itemstack = this.player.getHandStack(enumhand);
			if (hitResult != null) {
				switch (hitResult.type) {
					case ENTITY:
						Entity target = hitResult.entity;
						Vec3d vec3d = new Vec3d(hitResult.offset.x - target.x, hitResult.offset.y - target.y, hitResult.offset.z - target.z);

						double d0 = 36.0D;

						if (!player.canSee(target)) {
							d0 = 9.0D;
						}

						if (player.getSquaredDistanceTo(target) < d0) {
							InteractionResult res = player.interact(target, enumhand);
							if (res == InteractionResult.SUCCESS) {
								return true;
							}
							InteractionResult interact = target.interact(player, vec3d, enumhand);
							if (interact == InteractionResult.SUCCESS) {
								return true;
							}
						}
						break;
					case MISS:
						break;
					case BLOCK:
						BlockPos blockpos = hitResult.getPos();

						if (player.getSourceWorld().getBlockState(blockpos).getMaterial() != Material.AIR) {
							if (itemstack.isEmpty()) continue;
							float x = (float) hitResult.offset.x;
							float y = (float) hitResult.offset.y;
							float z = (float) hitResult.offset.z;

							InteractionResult res = player.interactionManager.useBlock(player,
									player.getSourceWorld(),
									itemstack,
									enumhand,
									blockpos,
									hitResult.face,
									x,
									y,
									z
							);
							if (res == InteractionResult.SUCCESS) {
								this.player.swingHand(enumhand);
								return true;
							}
						}
				}
			}
			InteractionResult res = player.interactionManager.useItem(player, player.getSourceWorld(), itemstack, enumhand);
			if (res == InteractionResult.SUCCESS) {
				return true;
			}
		}
		return false;
	}

	private HitResult rayTraceBlocks(double blockReachDistance) {
		Vec3d eyeVec = player.getEyePosition(1.0F);
		Vec3d lookVec = player.getRotationVec(1.0F);
		Vec3d pointVec = eyeVec.add(lookVec.x * blockReachDistance, lookVec.y * blockReachDistance, lookVec.z * blockReachDistance);
		return player.getSourceWorld().rayTrace(eyeVec, pointVec, false, false, true);
	}

	public @Nullable HitResult mouseOver() {
		World world = player.getSourceWorld();
		if (world == null) return null;

		Entity pointedEntity = null;
		double reach = player.isCreative() ? 5.0D : 4.5D;
		HitResult hitResult = rayTraceBlocks(reach);
		Vec3d eyeVec = player.getEyePosition(1.0F);
		boolean flag = !player.isCreative();
		if (player.isCreative()) reach = 6.0D;
		double extendedReach = reach;

		if (hitResult != null) {
			extendedReach = hitResult.offset.distanceTo(eyeVec);
			if (world.getBlockState(hitResult.getPos()).getMaterial() == Material.AIR) hitResult = null;
		}

		Vec3d lookVec = player.getRotationVec(1.0F);
		Vec3d pointVec = eyeVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
		Vec3d field_26675 = null;
		List<Entity> list = world.getEntities(player,
				player.getShape().grow(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach).expand(1.0D, 1.0D, 1.0D),
				(Predicate<? super Entity>) EntityFilter.NOT_SPECTATOR.and(entity -> entity != null && entity.hasCollision())
		);
		double d2 = extendedReach;

		for (int j = 0; j < list.size(); ++j) {
			Entity entity1 = list.get(j);
			Box box = entity1.getShape().expand(entity1.getExtraHitboxSize());
			HitResult hitResult1 = box.clip(eyeVec, pointVec);

			if (box.contains(eyeVec)) {
				if (d2 >= 0.0D) {
					pointedEntity = entity1;
					field_26675 = hitResult1 == null ? eyeVec : hitResult1.offset;
					d2 = 0.0D;
				}
			} else if (hitResult1 != null) {
				double d3 = eyeVec.distanceTo(hitResult1.offset);

				if (d3 < d2 || d2 == 0.0D) {
					if (entity1.getVehicle() == player.getVehicle()) {
						if (d2 == 0.0D) {
							pointedEntity = entity1;
							field_26675 = hitResult1.offset;
						}
					} else {
						pointedEntity = entity1;
						field_26675 = hitResult1.offset;
						d2 = d3;
					}
				}
			}
		}

		if (pointedEntity != null && flag && eyeVec.distanceTo(field_26675) > 3.0D) {
			pointedEntity = null;
			hitResult = new HitResult(HitResult.Type.MISS, field_26675, null, new BlockPos(field_26675));
		}

		if (pointedEntity != null && (d2 < extendedReach || hitResult == null)) {
			hitResult = new HitResult(pointedEntity, field_26675);
		}

		return hitResult;
	}

	public boolean clickBlock(BlockPos loc, Direction face) { // don't call this one
		World world = player.getSourceWorld();
		if (player.interactionManager.getGameMode() != GameMode.ADVENTURE) {
			if (player.interactionManager.getGameMode() == GameMode.SPECTATOR) {
				return false;
			}

			if (!player.abilities.canModifyWorld) {
				ItemStack itemstack = player.getMainHandStack();

				if (itemstack.isEmpty()) {
					return false;
				}

				if (!itemstack.hasMineBlockOverride(world.getBlockState(loc).getBlock())) {
					return false;
				}
			}
		}

		if (world.getWorldBorder().contains(loc)) {
			if (player.interactionManager.getGameMode() == GameMode.CREATIVE) {
				player.networkHandler.handlePlayerHandAction(createDiggingPacket(PlayerHandActionC2SPacket.Action.START_DESTROY_BLOCK, loc, face));
				clickBlockCreative(world, loc, face);
				this.blockHitDelay = 5;
			} else if (!this.isHittingBlock || !(currentBlock.equals(loc))) {
				if (this.isHittingBlock) {
					player.networkHandler.handlePlayerHandAction(createDiggingPacket(PlayerHandActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
							this.currentBlock,
							face
					));
				}

				BlockState iblockstate = world.getBlockState(loc);
				player.networkHandler.handlePlayerHandAction(createDiggingPacket(PlayerHandActionC2SPacket.Action.START_DESTROY_BLOCK, loc, face));
				boolean flag = iblockstate.getMaterial() != Material.AIR;

				if (flag && this.curBlockDamageMP == 0.0F) {
					iblockstate.getBlock().startMining(world, loc, player);
				}

				if (flag && iblockstate.getMiningSpeed(player, world, loc) >= 1.0F) {
					this.onPlayerDestroyBlock(loc);
				} else {
					this.isHittingBlock = true;
					this.currentBlock = loc;
					this.curBlockDamageMP = 0.0F;
					world.updateBlockMiningProgress(player.getNetworkId(), this.currentBlock, (int) (this.curBlockDamageMP * 10.0F) - 1);
				}
			}

			return true;
		} else {
			return false;
		}
	}

	private void clickBlockCreative(World world, BlockPos pos, Direction facing) {
		if (!world.extinguishFire(player, pos, facing)) {
			onPlayerDestroyBlock(pos);
		}
	}

	public boolean onPlayerDamageBlock(BlockPos posBlock, Direction directionFacing) { //continue clicking - one to call
		if (this.blockHitDelay > 0) {
			--this.blockHitDelay;
			return true;
		}
		World world = player.getSourceWorld();
		if (player.interactionManager.getGameMode() == GameMode.CREATIVE && world.getWorldBorder().contains(posBlock)) {
			this.blockHitDelay = 5;
			player.networkHandler.handlePlayerHandAction(createDiggingPacket(PlayerHandActionC2SPacket.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
			clickBlockCreative(world, posBlock, directionFacing);
			return true;
		} else if (posBlock.equals(currentBlock)) {
			BlockState iblockstate = world.getBlockState(posBlock);

			if (iblockstate.getMaterial() == Material.AIR) {
				this.isHittingBlock = false;
				return false;
			} else {
				this.curBlockDamageMP += iblockstate.getMiningSpeed(player, world, posBlock);

				if (this.curBlockDamageMP >= 1.0F) {
					this.isHittingBlock = false;
					player.networkHandler.handlePlayerHandAction(createDiggingPacket(PlayerHandActionC2SPacket.Action.STOP_DESTROY_BLOCK,
							posBlock,
							directionFacing
					));
					this.onPlayerDestroyBlock(posBlock);
					this.curBlockDamageMP = 0.0F;
					this.blockHitDelay = 5;
				}
				//player.getEntityId()
				//send to all, even the breaker
				world.updateBlockMiningProgress(-1, this.currentBlock, (int) (this.curBlockDamageMP * 10.0F) - 1);
				return true;
			}
		} else {
			return this.clickBlock(posBlock, directionFacing);
		}
	}

	private boolean onPlayerDestroyBlock(BlockPos pos) {
		World world = player.getSourceWorld();
		if (player.interactionManager.getGameMode() != GameMode.ADVENTURE) {
			if (player.interactionManager.getGameMode() == GameMode.SPECTATOR) {
				return false;
			}

			if (player.abilities.canModifyWorld) {
				ItemStack itemstack = player.getMainHandStack();

				if (itemstack.isEmpty()) {
					return false;
				}

				if (!itemstack.hasMineBlockOverride(world.getBlockState(pos).getBlock())) {
					return false;
				}
			}
		}

		if (player.interactionManager.getGameMode() == GameMode.CREATIVE && !player.getMainHandStack().isEmpty() &&
				player.getMainHandStack().getItem() instanceof SwordItem) {
			return false;
		} else {
			BlockState iblockstate = world.getBlockState(pos);
			Block block = iblockstate.getBlock();

			if ((block instanceof CommandBlock || block instanceof StructureBlock) && !player.isInTeleportationState()) {
				return false;
			} else if (iblockstate.getMaterial() == Material.AIR) {
				return false;
			} else {
				world.doGlobalEvent(2001, pos, Block.serialize(iblockstate));
				block.beforeMinedByPlayer(world, pos, iblockstate, player);
				boolean flag = world.setBlockState(pos, Blocks.AIR.defaultState(), 11);

				if (flag) {
					block.onBroken(world, pos, iblockstate);
				}

				this.currentBlock = new BlockPos(this.currentBlock.getX(), -1, this.currentBlock.getZ());

				if (player.interactionManager.getGameMode() != GameMode.CREATIVE) {
					ItemStack itemstack1 = player.getMainHandStack();

					if (!itemstack1.isEmpty()) {
						itemstack1.mineBlock(world, iblockstate, pos, player);

						if (itemstack1.isEmpty()) {
							player.setHandStack(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
						}
					}
				}

				return flag;
			}
		}
	}

	public void resetBlockRemoving() {
		if (this.isHittingBlock) {
			player.networkHandler.handlePlayerHandAction(createDiggingPacket(PlayerHandActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
					this.currentBlock,
					Direction.DOWN
			));
			this.isHittingBlock = false;
			this.curBlockDamageMP = 0.0F;
			player.getSourceWorld().updateBlockMiningProgress(player.getNetworkId(), this.currentBlock, -1);
			player.resetLastAttackedTicks();
			this.currentBlock = new BlockPos(-1, -1, -1);
		}
	}

	private static PlayerHandActionC2SPacket createDiggingPacket(PlayerHandActionC2SPacket.Action action, BlockPos pos, Direction facing) {
		PlayerHandActionC2SPacket p = new PlayerHandActionC2SPacket();
		((PlayerActionC2SPacketAccessor) p).setAction(action);
		((PlayerActionC2SPacketAccessor) p).setPos(pos);
		((PlayerActionC2SPacketAccessor) p).setDirection(facing);
		return p;
	}
}
