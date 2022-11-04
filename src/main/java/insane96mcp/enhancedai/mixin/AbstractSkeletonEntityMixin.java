package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.Modules;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeletonEntity.class)
public class AbstractSkeletonEntityMixin {
	@Inject(at = @At("TAIL"), method = "reassessWeaponGoal()V")
	public void reassessWeaponGoal(CallbackInfo callbackInfo) {
		Modules.skeleton.skeletonAI.reassessWeaponGoal((AbstractSkeletonEntity) (Object) this);
	}
}