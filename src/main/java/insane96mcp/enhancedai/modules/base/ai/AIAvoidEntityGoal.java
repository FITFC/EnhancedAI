package insane96mcp.enhancedai.modules.base.ai;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumSet;
import java.util.function.Predicate;

public class AIAvoidEntityGoal<T extends LivingEntity> extends Goal {
	protected final CreatureEntity entity;
	private final double farSpeed;
	private final double nearSpeed;
	protected T avoidTarget;
	protected final float avoidDistance;
	protected Path path;
	/** Class of entity this behavior seeks to avoid */
	protected final Class<T> classToAvoid;
	protected final Predicate<LivingEntity> avoidTargetSelector;
	protected final Predicate<LivingEntity> field_203784_k;
	private final EntityPredicate builtTargetSelector;

	public AIAvoidEntityGoal(CreatureEntity entityIn, Class<T> classToAvoidIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
		this(entityIn, classToAvoidIn, (p_200828_0_) -> {
			return true;
		}, avoidDistanceIn, farSpeedIn, nearSpeedIn, EntityPredicates.NO_CREATIVE_OR_SPECTATOR::test);
	}

	public AIAvoidEntityGoal(CreatureEntity entityIn, Class<T> avoidClass, Predicate<LivingEntity> targetPredicate, float distance, double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> p_i48859_9_) {
		this.entity = entityIn;
		this.classToAvoid = avoidClass;
		this.avoidTargetSelector = targetPredicate;
		this.avoidDistance = distance;
		this.farSpeed = farSpeedIn;
		this.nearSpeed = nearSpeedIn;
		this.field_203784_k = p_i48859_9_;
		this.builtTargetSelector = (new EntityPredicate()).range(distance).selector(p_i48859_9_.and(targetPredicate));
	}

	public AIAvoidEntityGoal(CreatureEntity entityIn, Class<T> avoidClass, float distance, double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> targetPredicate) {
		this(entityIn, avoidClass, (p_203782_0_) -> {
			return true;
		}, distance, nearSpeedIn, farSpeedIn, targetPredicate);
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		this.avoidTarget = this.entity.level.getNearestLoadedEntity(this.classToAvoid, this.builtTargetSelector, this.entity, this.entity.getX(), this.entity.getY(), this.entity.getZ(), this.entity.getBoundingBox().inflate(this.avoidDistance, 3.0D, this.avoidDistance));
		if (this.avoidTarget == null) {
			return false;
		} else {
			Vector3d vector3d = RandomPositionGenerator.getPosAvoid(this.entity, 16, 7, this.avoidTarget.position());
			if (vector3d == null) {
				return false;
			} else if (this.avoidTarget.distanceToSqr(vector3d.x, vector3d.y, vector3d.z) < this.avoidTarget.distanceToSqr(this.entity)) {
				return false;
			} else {
				this.path = this.entity.getNavigation().createPath(vector3d.x, vector3d.y, vector3d.z, 0);
				return this.path != null;
			}
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean canContinueToUse() {
		return !this.entity.getNavigation().isDone();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		this.entity.getNavigation().moveTo(this.path, this.farSpeed);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void stop() {
		this.avoidTarget = null;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		if (this.entity.distanceToSqr(this.avoidTarget) < 49.0D) {
			this.entity.getNavigation().setSpeedModifier(this.nearSpeed);
		} else {
			this.entity.getNavigation().setSpeedModifier(this.farSpeed);
		}

	}

	public void setAttackWhenRunning(boolean attackWhenRunning) {
		if (attackWhenRunning)
			this.setFlags(EnumSet.noneOf(Flag.class));
		else
			this.setFlags(EnumSet.of(Flag.LOOK));
	}
}