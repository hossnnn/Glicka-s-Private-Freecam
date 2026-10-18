package com.privatefreecam.client.mixin;

import com.privatefreecam.client.FreecamState;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {
	/** While in freecam the real player sees movement keys as released and sneak as held, so it stays put, crouched. */
	@Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
	private void privatefreecam$overrideMovementKeys(CallbackInfoReturnable<Boolean> cir) {
		Boolean forced = FreecamState.forcedKeyState((KeyMapping) (Object) this);
		if (forced != null) {
			cir.setReturnValue(forced);
		}
	}
}
