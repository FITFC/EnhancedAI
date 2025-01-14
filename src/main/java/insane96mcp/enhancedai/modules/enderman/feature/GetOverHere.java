package insane96mcp.enhancedai.modules.enderman.feature;

import insane96mcp.enhancedai.modules.enderman.ai.GetOverHereGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "[Experimental] Get Over Here", description = "Endermen teleport the player near him when can't reach him for a while.")
public class GetOverHere extends Feature {

    private final ForgeConfigSpec.DoubleValue getOverHereChanceConfig;

    public double getOverHereChance = 0.15d;

    public GetOverHere(Module module) {
        super(Config.builder, module, false);
        super.pushConfig(Config.builder);
        this.getOverHereChanceConfig = Config.builder
                .comment("Chance for a enderman to get the Get Over Here AI")
                .defineInRange("Get Over Here Chance", this.getOverHereChance, 0d, 1d);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.getOverHereChance = this.getOverHereChanceConfig.get();
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled()
                || event.getWorld().isClientSide
                || !(event.getEntity() instanceof EnderMan enderman))
            return;

        boolean getOverHere = NBTUtils.getBooleanOrPutDefault(enderman.getPersistentData(), EAStrings.Tags.Enderman.GET_OVER_HERE, enderman.level.random.nextDouble() < this.getOverHereChance);

        if (!getOverHere)
            return;

        GetOverHereGoal getOverHereGoal = new GetOverHereGoal(enderman);
        enderman.goalSelector.addGoal(1, getOverHereGoal);
    }
}