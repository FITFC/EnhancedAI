package insane96mcp.enhancedai.modules.witch.ai;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class WitchThrowPotionGoal extends Goal {
    private final Witch witch;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;
    private final double lingeringChance;
    private final double anotherThrowChance;

    private LivingEntity target;

    private int attackTime = -1;
    private boolean randomPotion = false;

    public WitchThrowPotionGoal(Witch witch, int attackIntervalMin, int attackIntervalMax, float attackRadius, double lingeringChance, double anotherThrowChance) {
        this.witch = witch;
        this.attackIntervalMin = attackIntervalMin;
        this.attackIntervalMax = attackIntervalMax;
        this.attackRadius = attackRadius;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.attackTime = Mth.floor(Mth.nextInt(witch.getRandom(), this.attackIntervalMin, this.attackIntervalMax)) / 2;
        this.lingeringChance = lingeringChance;
        this.anotherThrowChance = anotherThrowChance;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.witch.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.target = livingentity;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        double distanceToTarget = this.witch.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean canSee = this.witch.getSensing().hasLineOfSight(this.target);

        if (distanceToTarget > (double) this.attackRadiusSqr && !canSee) {
            this.witch.getNavigation().moveTo(this.target, 1d);
            return;
        }

        this.witch.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (--this.attackTime <= 0) {
            if (!canSee) {
                return;
            }
            this.witch.getNavigation().stop();

            this.attackTime = Mth.floor(Mth.nextInt(witch.getRandom(), this.attackIntervalMin, this.attackIntervalMax));
            this.throwPotionAtTarget();
        }
    }

    private void throwPotionAtTarget() {
        if (witch.isDrinkingPotion())
            return;

        Collection<MobEffectInstance> mobEffectInstances = new ArrayList<>();
        List<MobEffectInstance> listToLoop = this.target instanceof Raider ? Modules.witch.witchPotionThrowing.goodPotionsList : Modules.witch.witchPotionThrowing.badPotionsList;
        if (this.randomPotion) {
            mobEffectInstances.add(listToLoop.get(witch.getRandom().nextInt(listToLoop.size())));
        }
        else {
            for (MobEffectInstance mobEffectInstance : listToLoop) {
                if (this.target.hasEffect(mobEffectInstance.getEffect()))
                    continue;

                mobEffectInstances.add(new MobEffectInstance(mobEffectInstance));
                break;
            }
        }

        this.randomPotion = false;

        ThrownPotion thrownpotion = new ThrownPotion(witch.level, this.witch);
        Item potionType = witch.level.random.nextDouble() < this.lingeringChance ? Items.LINGERING_POTION : Items.SPLASH_POTION;
        thrownpotion.setItem(MCUtils.setCustomEffects(new ItemStack(potionType), mobEffectInstances));
        thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
        double distance = this.witch.distanceTo(target);
        double dirX = this.target.getX() - this.witch.getX();
        double distanceY = this.target.getY() - this.witch.getY();
        double dirZ = this.target.getZ() - this.witch.getZ();
        double distanceXZ = Math.sqrt(dirX * dirX + dirZ * dirZ);
        double yPos = this.target.getY(0d);
        yPos += this.target.getEyeHeight() * 0.1 + (distanceY / distanceXZ);
        double dirY = yPos - thrownpotion.getY();
        thrownpotion.shoot(dirX, dirY + distanceXZ * 0.18d, dirZ, 1.1f + ((float)distance / 32f) + (float)Math.max(distanceY / 48d, 0f), 1f);
        if (!witch.isSilent()) {
            witch.level.playSound(null, witch.getX(), witch.getY(), witch.getZ(), SoundEvents.WITCH_THROW, witch.getSoundSource(), 1.0F, 0.8F + witch.getRandom().nextFloat() * 0.4F);
        }

        witch.level.addFreshEntity(thrownpotion);

        if (witch.level.random.nextDouble() < this.anotherThrowChance) {
            this.attackTime = 7;
            this.randomPotion = true;
        }
    }
}
