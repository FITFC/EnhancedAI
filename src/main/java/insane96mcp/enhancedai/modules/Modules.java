package insane96mcp.enhancedai.modules;

import insane96mcp.enhancedai.modules.animal.AnimalModule;
import insane96mcp.enhancedai.modules.base.BaseModule;
import insane96mcp.enhancedai.modules.blaze.BlazeModule;
import insane96mcp.enhancedai.modules.creeper.CreeperModule;
import insane96mcp.enhancedai.modules.ghast.GhastModule;
import insane96mcp.enhancedai.modules.skeleton.SkeletonModule;
import insane96mcp.enhancedai.modules.spider.SpiderModule;
import insane96mcp.enhancedai.modules.witch.WitchModule;
import insane96mcp.enhancedai.modules.zombie.ZombieModule;

public class Modules {

	public static BaseModule base;
	public static AnimalModule animal;
	public static BlazeModule blaze;
	public static CreeperModule creeper;
	public static GhastModule ghast;
	//public static EndermanModule enderman;
	public static SkeletonModule skeleton;
	public static SpiderModule spider;
	public static WitchModule witch;
	public static ZombieModule zombie;

	public static void init() {
		base = new BaseModule();
		animal = new AnimalModule();
		blaze = new BlazeModule();
		creeper = new CreeperModule();
		ghast = new GhastModule();
		//enderman = new EndermanModule();
		skeleton = new SkeletonModule();
		spider = new SpiderModule();
		witch = new WitchModule();
		zombie = new ZombieModule();
	}

	public static void loadConfig() {
		base.loadConfig();
		animal.loadConfig();
		blaze.loadConfig();
		creeper.loadConfig();
		ghast.loadConfig();
		//enderman.loadConfig();
		skeleton.loadConfig();
		spider.loadConfig();
		witch.loadConfig();
		zombie.loadConfig();
	}
}
