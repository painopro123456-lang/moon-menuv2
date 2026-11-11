# Moon Menu V2

A client-side Fabric mod for Minecraft 26.2. Open the compact menu with **Right Shift**. The menu is fixed-size, centered, and does not dim or cover the rest of the game screen.

## UI requirements implemented

- Fixed-size compact floating panel (560 x 360 logical pixels, constrained to smaller screens).
- Translucent white surfaces using low-alpha fills, so Minecraft remains visible through the panel.
- No fullscreen backdrop or screen dimmer.
- Not resizable.
- Rounded surfaces, subdued accents, and dark readable text.

**Transparency note:** the panel uses real alpha transparency. A post-processing blur behind the panel is not included in this initial build; it needs a separate Minecraft 26.2-compatible render pipeline and is not faked with an opaque background.

## Build

Requires JDK 25 and Gradle 9.8.0. Run `gradle build`. GitHub Actions builds and tests the project on pushes to `main`, then uploads the JAR as a workflow artifact.

Install the JAR in the Fabric `mods` folder alongside Fabric API for Minecraft 26.2.
