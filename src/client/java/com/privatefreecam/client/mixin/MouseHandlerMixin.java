package com.privatefreecam.client.mixin;

import com.privatefreecam.client.FreecamState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	/** Scroll wheel changes freecam speed instead of cycling the hotbar. */
	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void privatefreecam$scrollSpeed(long window, double xOffset, double yOffset, CallbackInfo ci) {
		if (FreecamState.isActive() && Minecraft.getInstance().screen == null) {
			FreecamState.adjustSpeed(yOffset);
			ci.cancel();
		}
	}
}
