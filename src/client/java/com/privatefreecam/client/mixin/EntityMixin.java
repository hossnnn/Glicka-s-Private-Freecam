package com.privatefreecam.client.mixin;

import com.privatefreecam.client.FreecamState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
	/** Mouse look rotates the camera, not your real player, so no rotation packets are ever sent. */
	@Inject(method = "turn", at = @At("HEAD"), cancellable = true)
	private void privatefreecam$turnCamera(double yRot, double xRot, CallbackInfo ci) {
		if (FreecamState.isActive() && (Object) this == Minecraft.getInstance().player) {
			FreecamState.turn(yRot, xRot);
			ci.cancel();
		}
	}
}
