package carpet.utils;

import carpet.mixin.accessors.*;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.XpOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.PaintingEntity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.living.attribute.EntityAttributes;
import net.minecraft.entity.living.effect.StatusEffectInstance;
import net.minecraft.entity.living.mob.PathAwareEntity;
import net.minecraft.entity.living.mob.hostile.HostileEntity;
import net.minecraft.entity.living.mob.MobEntity;
import net.minecraft.entity.living.mob.SlimeEntity;
import net.minecraft.entity.living.mob.hostile.ZombieVillagerEntity;
import net.minecraft.entity.living.mob.hostile.boss.WitherEntity;
import net.minecraft.entity.living.mob.passive.animal.HorseBaseEntity;
import net.minecraft.entity.living.mob.passive.PassiveEntity;
import net.minecraft.entity.living.mob.passive.VillagerEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EntityInfo {
	private static String makeTime(long ticks) {
		long secs = ticks / 20;
		if (secs < 60) {
			return String.format("%d\"", secs);
		}
		if (secs < 60 * 60) {
			return String.format("%d'%d\"", secs / 60, secs % 60);
		}

		return String.format("%dh%d'%d\"", secs / 60 / 60, (secs % (60 * 60)) / 60, (secs % (60 * 60)) % 60);
	}

	private static String display_item(ItemStack item) {
		if (item == null || item.isEmpty()) {
			return null;
		}

		String stackname = item.getSize() > 1 ? String.format("%dx%s", item.getSize(), item.getHoverName()) : item.getHoverName();
		StringBuilder sb = new StringBuilder(stackname);
		if (item.isDamaged()) {
			sb.append(String.format(" %d/%d", item.getMaxDamage() - item.getDamage(), item.getMaxDamage()));
		}
		if (item.hasEnchantments()) {
			sb.append(" ( ");
			Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(item);
			for (Enchantment e : enchants.keySet()) {
				int level = enchants.get(e);
				String enstring = e.getName(level);
				sb.append(enstring).append(" ");
			}
			sb.append(")");
		}
		return sb.toString();
	}

	public static String entity_short_string(Entity e) {
		if (e == null) {
			return "None";
		}
		return String.format("%s at [%.1f, %.1f, %.1f]", e.getDisplayName().buildString(), e.x, e.y, e.z);
	}

	private static double get_speed(double internal) {
		return 43.1 * internal;
	}

	private static double get_horse_speed_percent(double internal) {
		double min = 0.45 * 0.25;
		double max = (0.45 + 0.9) * 0.25;
		return 100 * (internal - min) / (max - min);
	}

	private static double get_horse_jump(double x) {
		return -0.1817584952 * x * x * x + 3.689713992 * x * x + 2.128599134 * x - 0.343930367;
	}

	private static double get_horse_jump_percent(double internal) {
		double min = 0.4;
		double max = 1.0;
		return 100 * (internal - min) / (max - min);
	}

	public static List<String> entityInfo(Entity e, World ws) {
		List<String> lst = new ArrayList<String>();
		World world = e.getSourceWorld();
		lst.add(entity_short_string(e));
		if (e.hasVehicle()) {
			lst.add(String.format(" - Rides: %s", e.getVehicle().getDisplayName().buildString()));
		}
		if (e.hasPassengers()) {
			List<Entity> passengers = e.getPassengers();
			if (passengers.size() == 1) {
				lst.add(String.format(" - Is being ridden by: %s", passengers.get(0).getDisplayName().buildString()));
			} else {
				lst.add(" - Is being ridden by:");
				for (Entity ei : passengers) {
					lst.add(String.format("   * %s", ei.getDisplayName().buildString()));
				}
			}
		}
		lst.add(String.format(" - Height: %.2f, Width: %.2f, Eye height: %.2f", e.height, e.width, e.getEyeHeight()));
		lst.add(String.format(" - Age: %s", makeTime(e.time)));
		if (ws.dimension.getType().getId() != e.dimensionId) {
			lst.add(String.format(" - Dimension: %s", (e.dimensionId > 0) ? "The End" : ((e.dimensionId < 0) ? "Nether" : "Overworld")));
		}
		int fire = ((EntityAccessor) e).getFireTicks();
		if (fire > 0) {
			lst.add(String.format(" - Fire for %d ticks", fire));
		}
		if (e.isImmuneToFire()) {
			lst.add(" - Immune to fire");
		}
		if (e.netherPortalCooldown > 0) {
			lst.add(String.format(" - Portal cooldown for %d ticks", e.netherPortalCooldown));
		}
		if (e.isInvulnerable()) {
			lst.add(" - Invulnerable");
		} //  func_190530_aW()
		if (e.isImmuneToExplosions()) {
			lst.add(" - Immune to explosions");
		}

		if (e instanceof ItemEntity) {
			ItemEntity ei = (ItemEntity) e;
			ItemStack stack = ei.getItemStack();// getEntityItem();
			String stackname = stack.getSize() > 1 ? String.format("%dx%s", stack.getSize(), stack.getHoverName()) : stack.getHoverName();
			lst.add(String.format(" - Content: %s", stackname));
			lst.add(String.format(" - Despawn Timer: %s", makeTime(((ItemEntityAccessor) ei).getAge())));
		}
		if (e instanceof XpOrbEntity) {
			XpOrbEntity exp = (XpOrbEntity) e;
			lst.add(String.format(" - Despawn Timer: %s", makeTime(exp.orbAge)));
			lst.add(String.format(" - Xp Value: %s", exp.getXp()));
		}
		if (e instanceof ItemFrameEntity) {
			ItemFrameEntity eif = (ItemFrameEntity) e;
			lst.add(String.format(" - Content: %s", eif.getItemStackInItemFrame().getHoverName()));
			lst.add(String.format(" - Rotation: %d", eif.rotation()));
		}
		if (e instanceof PaintingEntity) {
			PaintingEntity ep = (PaintingEntity) e;
			lst.add(String.format(" - Art: %s", ep.motive.name));
		}


		if (e instanceof LivingEntity) {
			LivingEntity elb = (LivingEntity) e;
			lst.add(String.format(" - Despawn timer: %s", makeTime(elb.getDespawnTimer())));

			lst.add(String.format(" - Health: %.2f/%.2f", elb.getHealth(), elb.getMaxHealth()));
			if (elb.getAttribute(EntityAttributes.ARMOR).get() > 0.0) {
				lst.add(String.format(" - Armour: %.1f", elb.getAttribute(EntityAttributes.ARMOR).get()));
			}
			if (elb.getAttribute(EntityAttributes.ARMOR_TOUGHNESS).get() > 0.0) {
				lst.add(String.format(" - Toughness: %.1f", elb.getAttribute(EntityAttributes.ARMOR_TOUGHNESS).get()));
			}
			//lst.add(String.format(" - Base speed: %.1fb/s",get_speed(elb.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue())));


			Collection<StatusEffectInstance> potions = elb.getStatusEffects();
			if (!potions.isEmpty()) {
				lst.add(" - Potion effects:");
				for (StatusEffectInstance pe : potions) {
					lst.add(String.format("   * %s%s %s",
							pe.getName().substring(7),
							(pe.getAmplifier() > 1) ? String.format("x%d", pe.getAmplifier()) : "",
							makeTime(pe.getDuration())
					));
				}
			}
			ItemStack mainhand = elb.getMainHandStack();
			if (!(mainhand.isEmpty())) {
				lst.add(String.format(" - Main hand: %s", display_item(mainhand)));
			}
			ItemStack offhand = elb.getOffHandStack();
			if (!(offhand.isEmpty())) {
				lst.add(String.format(" - Off hand: %s", display_item(offhand)));
			}
			String armour = "";
			for (ItemStack armourpiece : elb.getArmorSlots()) {
				if (!(armourpiece.isEmpty())) {
					armour += String.format("\n   * %s", display_item(armourpiece));
				}
			}
			if (!(armour.isEmpty())) {
				lst.add(String.format(" - Armour:%s", armour));
			}
			if (e instanceof MobEntity) {
				MobEntity el = (MobEntity) elb;
				lst.add(String.format(" - Follow range: %.1f", el.getAttribute(EntityAttributes.FOLLOW_RANGE).get()));

				lst.add(String.format(" - Movement speed factor: %.2f", el.getMovementControl().getSpeed()));


				LivingEntity target_elb = el.getTargetEntity();
				if (target_elb != null) {
					lst.add(String.format(" - Attack target: %s", entity_short_string(target_elb)));
				}
				if (el.canPickupLoot()) {
					lst.add(" - Can pick up loot");
				}
				if (el.isPersistent()) {
					lst.add(" - Won't despawn");
				}

				if (e instanceof WitherEntity) {
					WitherEntity ew = (WitherEntity) e;
					Entity etarget0 = world.getEntity(ew.getTrackedEntityId(0));
					lst.add(String.format(" - Head 1 target: %s", entity_short_string(etarget0)));
					Entity etarget1 = world.getEntity(ew.getTrackedEntityId(1));
					lst.add(String.format(" - Head 2 target: %s", entity_short_string(etarget1)));
					Entity etarget2 = world.getEntity(ew.getTrackedEntityId(2));
					lst.add(String.format(" - Head 3 target: %s", entity_short_string(etarget2)));
				}
				if (e instanceof PathAwareEntity) {
					PathAwareEntity ec = (PathAwareEntity) e;
					if (ec.inVillage()) {
						BlockPos pos = ec.getPos();
						lst.add(String.format(" - Home position: %d blocks around [%d, %d, %d]",
								(int) ec.getVillageRadius(),
								pos.getX(),
								pos.getY(),
								pos.getZ()
						));
					}
					if (e instanceof PassiveEntity) {
						PassiveEntity eage = (PassiveEntity) e;
						if (eage.getBreedingAge() < 0) {
							lst.add(String.format(" - Time till adulthood: %s", makeTime(-eage.getBreedingAge())));
						}
						if (eage.getBreedingAge() > 0) {
							lst.add(String.format(" - Mating cooldown: %s", makeTime(eage.getBreedingAge())));
						}
						if (e instanceof VillagerEntity) {
							VillagerEntity ev = (VillagerEntity) e;

							SimpleInventory vinv = ev.getVillagerInventory();
							String inventory_content = "";
							for (int i = 0; i < vinv.getSize(); ++i) {
								ItemStack vstack = vinv.getStack(i);
								if (!vstack.isEmpty()) {
									inventory_content += String.format("\n   * %d: %s", i, display_item(vstack));
								}
							}
							if (!("".equals(inventory_content))) {
								lst.add(String.format(" - Inventory:%s", inventory_content));
							}
							if (((VillagerEntityAccessor) ev).getRiches() > 0) {
								lst.add(String.format(" - Wealth: %d emeralds", ((VillagerEntityAccessor) ev).getRiches()));
							}
						}
						if (e instanceof HorseBaseEntity) {
							HorseBaseEntity ah = (HorseBaseEntity) e;
							lst.add(String.format(" - Horse Speed: %.2f b/s (%.1f%%%%)",
									get_speed(elb.getAttribute(EntityAttributes.MOVEMENT_SPEED).get()),
									get_horse_speed_percent(elb.getAttribute(EntityAttributes.MOVEMENT_SPEED).get())
							));
							lst.add(String.format(" - Horse Jump: %.2f b/s (%.1f%%%%)",
									get_horse_jump(ah.getJumpStrength()),
									get_horse_jump_percent(ah.getJumpStrength())
							));
						}
					}
					if (e instanceof HostileEntity) {
						lst.add(String.format(" - Base attack: %.1f", elb.getAttribute(EntityAttributes.ATTACK_DAMAGE).get()));
						if (e instanceof ZombieVillagerEntity) {
							ZombieVillagerEntity ezv = (ZombieVillagerEntity) e;
							int conversionTime = ((ZombieVillagerEntityAccessor) ezv).getConversionTimer();
							if (conversionTime > 0) {
								lst.add(String.format(" - Convert to villager in: %s", makeTime(conversionTime)));
							}
						}
					}
				}
				if (e instanceof SlimeEntity) {
					lst.add(String.format(" - Base attack: %.1f", (float) ((SlimeEntityAccessor) e).invokeGetDamageAmount()));
				}
			}
		}

		return lst;
	}

	static void issue_entity_info(PlayerEntity player) {
		MinecraftServer server = player.getServer();
		if (server != null) {
			try {
				server.getCommandHandler().run(player, "entityinfo @e[r=5,c=5,type=!player]");
			} catch (Throwable ignored) {
			}
		}
	}
}
