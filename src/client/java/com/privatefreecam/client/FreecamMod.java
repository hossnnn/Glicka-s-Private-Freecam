package com.privatefreecam.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

public class FreecamMod implements ClientModInitializer {
	private static KeyMapping toggleKey;

	@Override
	public void onInitializeClient() {
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.privatefreecam.toggle",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_F6,
			KeyMapping.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (toggleKey.consumeClick()) {
				FreecamState.toggle(mc);
			}
			FreecamState.tick(mc);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> FreecamState.reset());

		// The camera is not your body, so don't let it interact with the world.
		AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> blockWhileActive());
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> blockWhileActive());
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> blockWhileActive());
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> blockWhileActive());
		UseItemCallback.EVENT.register((player, level, hand) -> blockWhileActive());
	}

	private static InteractionResult blockWhileActive() {
		return FreecamState.isActive() ? InteractionResult.FAIL : InteractionResult.PASS;
	}
}
