# Glicka's Private Freecam (Fabric 1.21.11)

Client-side, spectator-style freecam. It only moves the camera. Your real player stays where it is,
crouched, and nothing extra is sent to the server beyond normal crouching.

## Build with GitHub (no installs needed)

1. Create a new private GitHub repository and upload everything in this folder.
2. IMPORTANT: the hidden `.github` folder must be in the repo. If it is missing, click Add file > Create new file,
   type the name `.github/workflows/build.yml`, and paste the text from `build-workflow.txt`.
3. Open the Actions tab, wait for the green check, open the run, and download Artifacts at the bottom.
4. Inside is `glickas-private-freecam-1.0.0.jar` (ignore the `-sources` jar). Put it in your `mods` folder
   next to Fabric API for 1.21.11.

To build on your own computer instead: install a JDK (25 recommended) and run `./gradlew build`.
The jar appears in `build/libs`.

## Controls

- **F6**: toggle freecam (rebind in Controls, Miscellaneous section).
- **WASD**: fly where you look. **Space** up, **Shift** down, **Sprint** doubles speed.
- **Scroll wheel**: change flight speed.
- Your real body stays crouched while freecam is on (set `KEEP_PLAYER_CROUCHED` to false in FreecamState.java to change).
  Keep your crouch setting on Hold, not Toggle.
- Attacking, placing, using items and interacting are blocked while the camera is detached.
- Freecam ends if you disconnect or your real body takes damage.
- Cave culling is turned off while freecam is on so terrain around the camera renders.

## If the build fails

Written without access to the Minecraft sources. Spots to check first (Mojang mappings, 1.21.11):
`CameraMixin` (Camera.setup, setPosition, setRotation), `EntityMixin` (Entity.turn),
`MouseHandlerMixin` (MouseHandler.onScroll), `FreecamState` (Minecraft.smartCull),
`FreecamMod` (KeyMapping.Category.MISC and the interaction callbacks).
