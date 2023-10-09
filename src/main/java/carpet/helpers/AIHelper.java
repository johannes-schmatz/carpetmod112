package carpet.helpers;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.mixin.accessors.GoalSelectorAccessor;
import carpet.mixin.accessors.MobEntityAccessor;
import carpet.utils.extensions.AccessibleGoalSelectorEntry;
import net.minecraft.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entities;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.living.mob.MobEntity;
import net.minecraft.entity.living.mob.passive.animal.SkeletonTrapGoal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.locale.I18n;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AIHelper {
    private static final WeakHashMap<GoalSelector, MobEntity> TASK_TO_ENTITY_MAP = new WeakHashMap<>();
    private static final WeakHashMap<MobEntity, WeakHashMap<Goal, Supplier<String>>> DETAILED_INFO = new WeakHashMap<>();
    private static final Map<Class<? extends Goal>, String> TASK_NAME_MAP = new HashMap<>();

    static {
        // order is the same as the .patch files in the original carpet
        TASK_NAME_MAP.put(MeleeAttackGoal.class, "Melee attack");
        TASK_NAME_MAP.put(ProjectileAttackGoal.class, "Ranged attack");
        TASK_NAME_MAP.put(BowAttackGoal.class, "Ranged bow attack");
        TASK_NAME_MAP.put(FleeEntityGoal.class, "Avoiding other entity");
        TASK_NAME_MAP.put(WolfBegGoal.class, "Beg");
        TASK_NAME_MAP.put(BreakDoorGoal.class, "Breaking door");
        TASK_NAME_MAP.put(CreeperIgniteGoal.class, "Creeper swelling");
        TASK_NAME_MAP.put(TrackIronGolemTargetGoal.class, "Defending village");
        TASK_NAME_MAP.put(AbstractDoorInteractGoal.class, "Interacting with door");
        TASK_NAME_MAP.put(EatGrassGoal.class, "Eating grass");
        TASK_NAME_MAP.put(MobEntityActiveTargetGoal.class, "Looking for nearest other entity");
        TASK_NAME_MAP.put(MobEntityPlayerTargetGoal.class, "Looking for nearest player");
        TASK_NAME_MAP.put(EscapeSunlightGoal.class, "Seeking shelter from the sun");
        TASK_NAME_MAP.put(FollowMobGoal.class, "Following other entity");
        TASK_NAME_MAP.put(FollowGolemGoal.class, "Following golem");
        TASK_NAME_MAP.put(FollowOwnerGoal.class, "Following owner");
        TASK_NAME_MAP.put(FlyingFollowOwnerGoal.class, "Following owner while flying");
        TASK_NAME_MAP.put(FollowParentGoal.class, "Following parent");
        TASK_NAME_MAP.put(VillagerFarmGoal.class, "Farming");
        TASK_NAME_MAP.put(RevengeGoal.class, "Hurt by another entity");
        TASK_NAME_MAP.put(PerchOnShoulderGoal.class, "Land on owners shoulder");
        TASK_NAME_MAP.put(PounceAtTargetGoal.class, "Leaping at target");
        TASK_NAME_MAP.put(LlamaFollowCaravanGoal.class, "Llama following caravan");
        TASK_NAME_MAP.put(LookAtCustomerGoal.class, "Looking at player");
        TASK_NAME_MAP.put(IronGolemLookGoal.class, "Looking at villager");
        TASK_NAME_MAP.put(LookAroundGoal.class, "Idle, looking around");
        TASK_NAME_MAP.put(AnimalBreedGoal.class, "Mating (Animals)");
        TASK_NAME_MAP.put(StayIndoorsGoal.class, "Moving indoors");
        TASK_NAME_MAP.put(WanderThroughVillageAtNightGoal.class, "Moving through village");
        TASK_NAME_MAP.put(GoToBlockGoal.class, "Moving to block");
        TASK_NAME_MAP.put(WanderThroughVillageGoal.class, "Moving towards restriction");
        TASK_NAME_MAP.put(GoToEntityTargetGoal.class, "Moving towards target");
        TASK_NAME_MAP.put(ActiveTargetGoal.class, "Looking for nearest target");
        TASK_NAME_MAP.put(AttackGoal.class, "Ocelot attacking");
        TASK_NAME_MAP.put(OcelotSitOnBlockGoal.class, "Ocelot sitting");
        TASK_NAME_MAP.put(LongDoorInteractGoal.class, "Opening door");
        TASK_NAME_MAP.put(AttackWithOwnerGoal.class, "Owner hurt by target");
        TASK_NAME_MAP.put(OwnerHurtGoal.class, "Owner hurts target");
        TASK_NAME_MAP.put(EscapeDangerGoal.class, "Panicking");
        TASK_NAME_MAP.put(FormCaravanGoal.class, "Playing");
        TASK_NAME_MAP.put(RestrictOpenDoorGoal.class, "Prevented from opening door");
        TASK_NAME_MAP.put(AvoidSunlightGoal.class, "Avoiding sun");
        TASK_NAME_MAP.put(HorseBondWithPlayerGoal.class, "Running around like crazy");
        TASK_NAME_MAP.put(SitGoal.class, "Sitting");
        TASK_NAME_MAP.put(SkeletonTrapGoal.class, "Riding");
        TASK_NAME_MAP.put(SwimGoal.class, "Swimming");
        TASK_NAME_MAP.put(TrackTargetGoal.class, "Targeting");
        TASK_NAME_MAP.put(UntamedActiveTargetGoal.class, "Targeting untamed animal");
        TASK_NAME_MAP.put(TemptGoal.class, "Tempted by player");
        TASK_NAME_MAP.put(StopFollowingCustomerGoal.class, "Trading with player");
        TASK_NAME_MAP.put(TradeWithVillagerGoal.class, "Interacting with villager");
        TASK_NAME_MAP.put(VillagerMatingGoal.class, "Mating (Villagers)");
        TASK_NAME_MAP.put(WanderAroundGoal.class, "Wandering");
        TASK_NAME_MAP.put(WaterAvoidingWanderAroundGoal.class, "Wandering (Land)");
        TASK_NAME_MAP.put(FlyingWaterAvoidingWanderAroundGoal.class, "Wandering (Air)");
        TASK_NAME_MAP.put(LookAtEntityGoal.class, "Looking at closest entity");
        TASK_NAME_MAP.put(ZombieAttackGoal.class, "Zombie attacking");
    }

    public static Stream<Goal> getCurrentTasks(MobEntity e) {
        return ((GoalSelectorAccessor) getTasks(e)).getExecutingTaskEntries().stream()
                .sorted(Comparator.comparingInt(AccessibleGoalSelectorEntry::getPriority).reversed())
                .map(AccessibleGoalSelectorEntry::getAction);
    }

    public static Stream<String> getCurrentTaskNames(MobEntity e, Map<Goal, Supplier<String>> details) {
        return getCurrentTasks(e).map(task -> getTaskName(task, details));
    }

    public static String getTaskName(Goal task) {
        return getTaskName(task, null);
    }

    public static String getTaskName(Goal task, Map<Goal, Supplier<String>> details) {
        String detailsInfo = details != null && details.containsKey(task) ? ": " + details.get(task).get() : "";
        String taskName = "Unknown";
        for (Class<? extends Goal> cls = task.getClass(); cls != Goal.class; cls = (Class<? extends Goal>) cls.getSuperclass()) {
            String name = TASK_NAME_MAP.get(cls);
            if (name != null) {
                taskName = name;
                break;
            }
        }
        return taskName + detailsInfo;
    }

    public static String getInfo(GoalSelector tasks, Goal task) {
        return "Entity: " + getName(getOwner(tasks)) + ", Task: " + getTaskName(task);
    }

    public static String getName(@Nullable Entity entity) {
        if (entity == null) return "unknown";
        if (!entity.hasCustomName()) return entity.getName();
        String id = Entities.getName(entity);
        if (id == null) id = "generic";
        return I18n.translate("entity." + id + ".name");
    }

    public static String formatCurrentTasks(MobEntity e, Map<Goal, Supplier<String>> details) {
        return getCurrentTaskNames(e, details).collect(Collectors.joining(","));
    }

    @Nullable
    public static MobEntity getOwner(GoalSelector tasks) {
        return TASK_TO_ENTITY_MAP.computeIfAbsent(tasks, t -> {
            for (ServerWorld world : CarpetServer.getMinecraftServer().worlds) {
                for (MobEntity e : world.getEntities(MobEntity.class, x -> true)) {
                    if (((MobEntityAccessor) e).getGoalSelector() == tasks) return e;
                }
            }
            return null;
        });
    }

    public static void update(GoalSelector tasks) {
        if (!CarpetSettings.displayMobAI) return;
        MobEntity owner = getOwner(tasks);
        if (owner == null) return;
        Map<Goal, Supplier<String>> details = DETAILED_INFO.get(owner);
        String formatted = formatCurrentTasks(owner, details);
        owner.setCustomName(formatted);
    }
    public static void setDetailedInfo(MobEntity owner, Goal task, String info) {
        setDetailedInfo(owner, task, () -> info);
    }

    public static void setDetailedInfo(MobEntity owner, Goal task, Supplier<String> info) {
        DETAILED_INFO.computeIfAbsent(owner, x -> new WeakHashMap<>()).put(task, info);
        TASK_TO_ENTITY_MAP.put(getTasks(owner), owner);
        update(getTasks(owner));
    }

    public static GoalSelector getTasks(MobEntity e) {
        return ((MobEntityAccessor) e).getGoalSelector();
    }
}
