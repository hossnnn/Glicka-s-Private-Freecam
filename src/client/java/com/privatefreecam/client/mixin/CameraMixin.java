package com.privatefreecam.client.mixin;

import com.privatefreecam.client.FreecamState;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	protected abstract void setPosition(double x, double y, double z);

	@Shadow
	protected abstract void setRotation(float yRot, float xRot);

	/** Runs after vanilla has placed the camera for this frame, then overrides it with the freecam view. */
	@Inject(method = "setup", at = @At("RETURN"))
	private void privatefreecam$applyFreecam(CallbackInfo ci) {
		if (!FreecamState.isActive()) {
			return;
		}
		FreecamState.advance();
		this.setRotation(FreecamState.yaw(), FreecamState.pitch());
		this.setPosition(FreecamState.x(), FreecamState.y(), FreecamState.z());
	}

	/** Makes the game draw your real player model, the way it does in third person. */
	@Inject(method = "isDetached", at = @At("HEAD"), cancellable = true)
	private void privatefreecam$detached(CallbackInfoReturnable<Boolean> cir) {
		if (FreecamState.isActive()) {
			cir.setReturnValue(true);
		}
	}
}
