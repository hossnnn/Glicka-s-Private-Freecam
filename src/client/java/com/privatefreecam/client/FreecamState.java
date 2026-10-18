package com.privatefreecam.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * All freecam state lives here. Nothing in this class sends packets or touches the
 * real player: it only keeps a camera position/rotation that CameraMixin applies
 * to the rendered view. To the server, you are just a player standing still.
 */
public final class FreecamState {
	private static final double MIN_SPEED = 1.0;
	private static final double MAX_SPEED = 120.0;
	private static final double SPRINT_MULTIPLIER = 2.0;
	/** Your real body crouches while the camera is detached. Set to false to let it stand. */
	private static final boolean KEEP_PLAYER_CROUCHED = true;

	private static boolean active;
	/** True only while this mod reads the raw movement keys, so KeyMappingMixin lets it through. */
	private static boolean polling;

	private static double x, y, z;
	private static float yaw, pitch;
	private static double speed = 11.0; // blocks per second, roughly creative flight speed
	private static long lastNanos;
	/** The game's cave-culling setting before freecam, so we can put it back afterwards. */
	private static boolean savedSmartCull = true;

	private FreecamState() {}

	/** Cave culling hides chunks not connected to the camera by open air, which breaks noclip flight. */
	private static void restoreCulling() {
		Minecraft.getInstance().smartCull = savedSmartCull;
	}

	public static boolean isActive() {
		return active;
	}

	public static double x() { return x; }
	public static double y() { return y; }
	public static double z() { return z; }
	public static float yaw() { return yaw; }
	public static float pitch() { return pitch; }

	public static void toggle(Minecraft mc) {
		if (active) {
			disable(mc, "Freecam off");
		} else {
			enable(mc);
		}
	}

	private static void enable(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null || mc.level == null) {
			return;
		}
		x = player.getX();
		y = player.getEyeY();
		z = player.getZ();
		yaw = player.getYRot();
		pitch = player.getXRot();
		lastNanos = 0L;
		savedSmartCull = mc.smartCull;
		mc.smartCull = false;
		active = true;
		player.displayClientMessage(Component.literal("Freecam on (scroll = speed)"), true);
	}

	public static void disable(Minecraft mc, String message) {
		if (!active) {
			return;
		}
		active = false;
		polling = false;
		lastNanos = 0L;
		restoreCulling();
		if (mc.player != null && message != null) {
			mc.player.displayClientMessage(Component.literal(message), true);
		}
	}

	/** Silent reset for disconnects and world changes. */
	public static void reset() {
		if (active) {
			restoreCulling();
		}
		active = false;
		polling = false;
		lastNanos = 0L;
	}

	/** Called every client tick while active to catch conditions that should end freecam. */
	public static void tick(Minecraft mc) {
		if (!active) {
			return;
		}
		if (mc.player == null || mc.level == null) {
			reset();
			return;
		}
		// Your real body is still in the world, so get out of freecam if it takes damage.
		if (mc.player.hurtTime > 0) {
			disable(mc, "Freecam off (you took damage)");
		}
	}

	/** Mouse movement goes to the camera instead of the real player. Same 0.15 factor as Entity.turn. */
	public static void turn(double yRotDelta, double xRotDelta) {
		yaw += (float) (yRotDelta * 0.15);
		pitch = Mth.clamp(pitch + (float) (xRotDelta * 0.15), -90.0F, 90.0F);
	}

	public static void adjustSpeed(double scrollY) {
		if (scrollY == 0) {
			return;
		}
		double factor = scrollY > 0 ? 1.2 : 1.0 / 1.2;
		speed = Mth.clamp(speed * factor, MIN_SPEED, MAX_SPEED);
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			mc.player.displayClientMessage(
				Component.literal(String.format("Freecam speed: %.1f", speed)), true);
		}
	}

	/**
	 * While active, the real player must not react to the movement keys, except that it is held
	 * crouched. KeyMappingMixin asks this from KeyMapping.isDown(); our own polling in advance()
	 * is exempt via the polling flag. Returns null when the key should behave normally.
	 */
	public static Boolean forcedKeyState(KeyMapping key) {
		if (!active || polling) {
			return null;
		}
		Options o = Minecraft.getInstance().options;
		if (key == o.keyShift) {
			return KEEP_PLAYER_CROUCHED ? Boolean.TRUE : Boolean.FALSE;
		}
		if (key == o.keyUp || key == o.keyDown || key == o.keyLeft || key == o.keyRight
			|| key == o.keyJump || key == o.keySprint) {
			return Boolean.FALSE;
		}
		return null;
	}

	/** Moves the camera based on held keys. Called once per rendered frame from CameraMixin. */
	public static void advance() {
		long now = System.nanoTime();
		double dt = lastNanos == 0L ? 0.0 : Math.min((now - lastNanos) / 1.0e9, 0.1);
		lastNanos = now;

		Minecraft mc = Minecraft.getInstance();
		mc.smartCull = false; // keep cave culling off every frame in case something else turns it back on
		if (mc.screen != null || dt <= 0.0) {
			return;
		}

		boolean forward, back, left, right, up, down, sprint;
		polling = true;
		try {
			Options o = mc.options;
			forward = o.keyUp.isDown();
			back = o.keyDown.isDown();
			left = o.keyLeft.isDown();
			right = o.keyRight.isDown();
			up = o.keyJump.isDown();
			down = o.keyShift.isDown();
			sprint = o.keySprint.isDown();
		} finally {
			polling = false;
		}

		double yawRad = Math.toRadians(yaw);
		double pitchRad = Math.toRadians(pitch);
		double cosPitch = Math.cos(pitchRad);

		// Look direction (like spectator mode, W flies where you look) and the camera's right side.
		Vec3 look = new Vec3(-Math.sin(yawRad) * cosPitch, -Math.sin(pitchRad), Math.cos(yawRad) * cosPitch);
		Vec3 side = new Vec3(-Math.cos(yawRad), 0.0, -Math.sin(yawRad));

		double mx = 0.0, my = 0.0, mz = 0.0;
		if (forward) { mx += look.x; my += look.y; mz += look.z; }
		if (back)    { mx -= look.x; my -= look.y; mz -= look.z; }
		if (right)   { mx += side.x; mz += side.z; }
		if (left)    { mx -= side.x; mz -= side.z; }
		if (up)      { my += 1.0; }
		if (down)    { my -= 1.0; }

		double length = Math.sqrt(mx * mx + my * my + mz * mz);
		if (length < 1.0e-6) {
			return;
		}

		double step = speed * (sprint ? SPRINT_MULTIPLIER : 1.0) * dt / length;
		x += mx * step;
		y += my * step;
		z += mz * step;
	}
}
