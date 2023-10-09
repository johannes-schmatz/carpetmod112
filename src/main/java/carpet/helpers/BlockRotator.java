package carpet.helpers;

import carpet.CarpetSettings;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.*;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.World;

public class BlockRotator {
	public static boolean flipBlockWithCactus(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, InteractionHand hand, Direction facing,
			float hitX, float hitY, float hitZ) {
		if (!playerIn.abilities.canModifyWorld || !CarpetSettings.flippinCactus || !player_holds_cactus_mainhand(playerIn)) {
			return false;
		}
		return flip_block(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}

	public static @Nullable BlockState alternativeBlockPlacement(Block block, World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta,
			LivingEntity placer) {
		//actual alternative block placement code
		//
		if (block instanceof GlazedTerracottaBlock) {
			facing = Direction.byId((int) hitX - 2);
			if (facing == Direction.UP || facing == Direction.DOWN) {
				facing = placer.getHorizontalFacing().getOpposite();
			}
			return block.defaultState().set(HorizontalFacingBlock.FACING, facing);
		} else if (block instanceof ObserverBlock) {
			return block.defaultState()
					.set(FacingBlock.FACING, Direction.byId((int) hitX - 2))
					.set(ObserverBlock.POWERED, CarpetSettings.observersDoNonUpdate);
		} else if (block instanceof RepeaterBlock) {
			facing = Direction.byId((((int) hitX) % 10) - 2);
			if (facing == Direction.UP || facing == Direction.DOWN) {
				facing = placer.getHorizontalFacing().getOpposite();
			}
			return block.defaultState()
					.set(HorizontalFacingBlock.FACING, facing)
					.set(RepeaterBlock.DELAY, MathHelper.clamp((((int) hitX) / 10) + 1, 1, 4))
					.set(RepeaterBlock.LOCKED, Boolean.FALSE);
		} else if (block instanceof TrapdoorBlock) {
			return block.defaultState()
					.set(TrapdoorBlock.FACING, Direction.byId((((int) hitX) % 10) - 2))
					.set(TrapdoorBlock.OPEN, Boolean.FALSE)
					.set(TrapdoorBlock.HALF, (hitX > 10) ? TrapdoorBlock.Half.TOP : TrapdoorBlock.Half.BOTTOM)
					.set(TrapdoorBlock.OPEN, worldIn.hasNeighborSignal(pos));
		} else if (block instanceof ComparatorBlock) {
			facing = Direction.byId((((int) hitX) % 10) - 2);
			if ((facing == Direction.UP) || (facing == Direction.DOWN)) {
				facing = placer.getHorizontalFacing().getOpposite();
			}
			ComparatorBlock.Mode m = (hitX > 10) ? ComparatorBlock.Mode.SUBTRACT : ComparatorBlock.Mode.COMPARE;
			return block.defaultState()
					.set(HorizontalFacingBlock.FACING, facing)
					.set(ComparatorBlock.POWERED, Boolean.FALSE)
					.set(ComparatorBlock.MODE, m);
		} else if (block instanceof DispenserBlock) {
			return block.defaultState()
					.set(DispenserBlock.FACING, Direction.byId((int) hitX - 2))
					.set(DispenserBlock.TRIGGERED, Boolean.FALSE);
		} else if (block instanceof PistonBaseBlock) {
			return block.defaultState()
					.set(FacingBlock.FACING, Direction.byId((int) hitX - 2))
					.set(PistonBaseBlock.EXTENDED, Boolean.FALSE);
		} else if (block instanceof StairsBlock) {
			return block.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
					.set(StairsBlock.FACING, Direction.byId((((int) hitX) % 10) - 2))
					.set(StairsBlock.HALF, (hitX > 10) ? StairsBlock.Half.TOP : StairsBlock.Half.BOTTOM);
		} else if (block instanceof FenceGateBlock) {
			return block.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
					.set(HorizontalFacingBlock.FACING, Direction.byId((((int) hitX) % 10) - 2))
					.set(FenceGateBlock.OPEN, hitX > 10);
		} else if (block instanceof PumpkinBlock) {
			return block.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
					.set(HorizontalFacingBlock.FACING, Direction.byId((((int) hitX) % 10) - 2));
		} else if (block instanceof ChestBlock) {
			return block.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
					.set(HorizontalFacingBlock.FACING, Direction.byId((((int) hitX) % 10) - 2));
		} else if (block instanceof EnderChestBlock) {
			return block.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
					.set(HorizontalFacingBlock.FACING, Direction.byId((((int) hitX) % 10) - 2));
		} else if (block instanceof DoorBlock) {
			return block.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
					.set(DoorBlock.FACING, Direction.byId((((int) hitX) % 10) - 2))
					.set(DoorBlock.HINGE, hitX % 100 < 10 ? DoorBlock.Hinge.LEFT : DoorBlock.Hinge.RIGHT)
					.set(DoorBlock.OPEN, hitX > 100);
		}
		return null;
	}


	private static boolean is_rail_legal(AbstractRailBlock.Shape type, World world, BlockPos pos) {
		if (!world.getBlockState(pos.down()).isFullBlock()) {
			return false;
		} else if (type == AbstractRailBlock.Shape.ASCENDING_EAST && !world.getBlockState(pos.east()).isFullBlock()) {
			return false;
		} else if (type == AbstractRailBlock.Shape.ASCENDING_WEST && !world.getBlockState(pos.west()).isFullBlock()) {
			return false;
		} else if (type == AbstractRailBlock.Shape.ASCENDING_NORTH && !world.getBlockState(pos.north()).isFullBlock()) {
			return false;
		} else if (type == AbstractRailBlock.Shape.ASCENDING_SOUTH && !world.getBlockState(pos.south()).isFullBlock()) {
			return false;
		}

		return true;
	}


	public static boolean flip_block(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, InteractionHand hand, Direction facing, float hitX,
			float hitY, float hitZ) {
		// TODO: rewrite with a new method to get the state, if that is null then don't setBlockState
		Block block = state.getBlock();
		if (block instanceof RailBlock) {
			AbstractRailBlock.Shape type = state.get(RailBlock.SHAPE);
			AbstractRailBlock.Shape[] values = AbstractRailBlock.Shape.values();

			int index = type.getId();
			AbstractRailBlock.Shape newType = type;
			for (int i = 0; i < values.length; i++) {
				newType = values[(index + 1 + i) % values.length];

				if (is_rail_legal(newType, worldIn, pos)) {
					break;
				}
			}

			BlockState newState = state.set(RailBlock.SHAPE, newType);
			worldIn.setBlockState(pos, newState, 2 | CarpetSettings.NO_UPDATES);
		} else if (
				(block instanceof GlazedTerracottaBlock) || (block instanceof DiodeBlock) || (block instanceof AbstractRailBlock) ||
						(block instanceof TrapdoorBlock) || (block instanceof LeverBlock) || (block instanceof FenceGateBlock)) {
			worldIn.setBlockState(pos, state.rotate(BlockRotation.CLOCKWISE_90), 2 | CarpetSettings.NO_UPDATES);
		} else if ((block instanceof ObserverBlock) || (block instanceof EndRodBlock)) {
			worldIn.setBlockState(pos, state.set(FacingBlock.FACING, state.get(FacingBlock.FACING).getOpposite()), 2 | CarpetSettings.NO_UPDATES);
		} else if (block instanceof DispenserBlock) {
			worldIn.setBlockState(pos, state.set(DispenserBlock.FACING, state.get(DispenserBlock.FACING).getOpposite()), 2 | CarpetSettings.NO_UPDATES);
		} else if (block instanceof PistonBaseBlock) {
			if (!state.get(PistonBaseBlock.EXTENDED))
				worldIn.setBlockState(pos, state.set(FacingBlock.FACING, state.get(FacingBlock.FACING).getOpposite()), 2 | CarpetSettings.NO_UPDATES);
		} else if (block instanceof SlabBlock) {
			if (!((SlabBlock) block).isDouble()) {
				if (state.get(SlabBlock.HALF) == SlabBlock.Half.TOP) {
					worldIn.setBlockState(pos, state.set(SlabBlock.HALF, SlabBlock.Half.BOTTOM), 2 | CarpetSettings.NO_UPDATES);
				} else {
					worldIn.setBlockState(pos, state.set(SlabBlock.HALF, SlabBlock.Half.TOP), 2 | CarpetSettings.NO_UPDATES);
				}
			}
		} else if (block instanceof HopperBlock) {
			if (state.get(HopperBlock.FACING) != Direction.DOWN) {
				worldIn.setBlockState(pos, state.set(HopperBlock.FACING, state.get(HopperBlock.FACING).clockwiseY()), 2 | CarpetSettings.NO_UPDATES);
			}
		} else if (block instanceof StairsBlock) {
			//LOG.error(String.format("hit with facing: %s, at side %.1fX, X %.1fY, Y %.1fZ",facing, hitX, hitY, hitZ));
			if ((facing == Direction.UP && hitY == 1.0f) || (facing == Direction.DOWN && hitY == 0.0f)) {
				if (state.get(StairsBlock.HALF) == StairsBlock.Half.TOP) {
					worldIn.setBlockState(pos, state.set(StairsBlock.HALF, StairsBlock.Half.BOTTOM), 2 | CarpetSettings.NO_UPDATES);
				} else {
					worldIn.setBlockState(pos, state.set(StairsBlock.HALF, StairsBlock.Half.TOP), 2 | CarpetSettings.NO_UPDATES);
				}
			} else {
				boolean turn_right = true;
				if (facing == Direction.NORTH) {
					turn_right = (hitX <= 0.5);
				} else if (facing == Direction.SOUTH) {
					turn_right = !(hitX <= 0.5);
				} else if (facing == Direction.EAST) {
					turn_right = (hitZ <= 0.5);
				} else if (facing == Direction.WEST) {
					turn_right = !(hitZ <= 0.5);
				} else {
					return false;
				}
				if (turn_right) {
					worldIn.setBlockState(pos, state.rotate(BlockRotation.COUNTERCLOCKWISE_90), 2 | CarpetSettings.NO_UPDATES);
				} else {
					worldIn.setBlockState(pos, state.rotate(BlockRotation.CLOCKWISE_90), 2 | CarpetSettings.NO_UPDATES);
				}
			}
		} else {
			return false;
		}
		worldIn.onRegionChanged(pos, pos);
		return true;
	}

	private static boolean player_holds_cactus_mainhand(PlayerEntity playerIn) {
		return (!playerIn.getMainHandStack().isEmpty()
				&& playerIn.getMainHandStack().getItem() instanceof BlockItem &&
				((BlockItem) (playerIn.getMainHandStack().getItem())).getBlock() == Blocks.CACTUS);
	}

	public static boolean flippinEligibility(Entity entity) {
		if (CarpetSettings.flippinCactus
				&& (entity instanceof PlayerEntity)) {
			PlayerEntity player = (PlayerEntity) entity;
			return (!player.getOffHandStack().isEmpty()
					&& player.getOffHandStack().getItem() instanceof BlockItem &&
					((BlockItem) (player.getOffHandStack().getItem())).getBlock() == Blocks.CACTUS);
		}
		return false;
	}
}
