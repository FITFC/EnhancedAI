package insane96mcp.enhancedai.modules.animal.feature;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;

@Label(name = "Animal Attacking", description = "Make animals fight back and no longer flee when attacked")
public class AnimalAttacking extends Feature {

    private final ForgeConfigSpec.ConfigValue<Boolean> animalsFightBackConfig;
    private final ForgeConfigSpec.ConfigValue<Boolean> noMoreFleeConfig;
    private final ForgeConfigSpec.ConfigValue<Double> speedMultiplierConfig;
    private final Blacklist.Config entityBlacklistConfig;

    public boolean animalsFightBack = true;
    public boolean noMoreFlee = true;
    public double speedMultiplier = 1.35d;
    public Blacklist entityBlacklist;

    private static final double BASE_ATTACK_DAMAGE = 4d;

    public AnimalAttacking(Module module) {
        super(Config.builder, module, true, false);
        super.pushConfig(Config.builder);
        animalsFightBackConfig = Config.builder
                .comment("If true, when attacked, animals will call other animals for help and attack back. Animals have a slightly bigger range to attack. Attack damage can't be changed via config due to limitation so use mods like Mobs Properties Randomness to increase the damage. Base damage is " + String.format("%.1f", BASE_ATTACK_DAMAGE))
                .define("Animals Fight back", this.animalsFightBack);
        noMoreFleeConfig = Config.builder
                .comment("If true, when attacked, animals will no longer flee.")
                .define("Animals No Longer Flee", this.noMoreFlee);
        speedMultiplierConfig = Config.builder
                .comment("Movement speed multiplier when attacking.")
                .defineInRange("Movement Speed Multiplier", this.speedMultiplier, 0d, 4d);
        entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't be affected by this feature")
                .setDefaultList(Collections.emptyList())
                .setIsDefaultWhitelist(false)
                .build();
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.animalsFightBack = this.animalsFightBackConfig.get();
        this.noMoreFlee = this.noMoreFleeConfig.get();
        this.speedMultiplier = this.speedMultiplierConfig.get();
        this.entityBlacklist = this.entityBlacklistConfig.get();
    }

    public static void attribute(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (event.has(entityType, Attributes.ATTACK_DAMAGE))
                continue;

            event.add(entityType, Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE);
        }
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled()
                || event.getEntity() instanceof Enemy
                || !(event.getEntity() instanceof Animal animal)
                || this.entityBlacklist.isEntityBlackOrNotWhitelist(animal))
            return;

        CompoundTag persistentData = animal.getPersistentData();

        double movementSpeedMultiplier = NBTUtils.getDoubleOrPutDefault(persistentData, EAStrings.Tags.Passive.SPEED_MULTIPLIER_WHEN_AGGROED, this.speedMultiplier);

        if (this.animalsFightBack) {
            animal.targetSelector.addGoal(1, (new HurtByTargetGoal(animal)).setAlertOthers());
            animal.goalSelector.addGoal(1, new MeleeAttackGoal(animal, movementSpeedMultiplier, false));
            AttributeInstance kbAttribute = animal.getAttribute(Attributes.ATTACK_KNOCKBACK);
            if (kbAttribute != null)
                kbAttribute.addPermanentModifier(new AttributeModifier("Animal knockback", 3.5d, AttributeModifier.Operation.ADDITION));
        }

        if (this.noMoreFlee) {
            ArrayList<Goal> goalsToRemove = new ArrayList<>();
            for (WrappedGoal prioritizedGoal : animal.goalSelector.availableGoals) {
                if (!(prioritizedGoal.getGoal() instanceof PanicGoal goal))
                    continue;

                goalsToRemove.add(goal);
            }

            goalsToRemove.forEach(animal.goalSelector::removeGoal);
        }
    }
}