# Repository Guidelines

## Project Structure & Module Organization

This is a Fabric mod for Minecraft 26.2 using Gradle and Fabric Loom with split source sets.

- `src/main/java/com/lunarlake/cs2LikeConsole/` — common mod code; entrypoint `Cs2LikeConsole`.
- `src/client/java/com/lunarlake/cs2LikeConsole/client/` — client-only code, mixins, and data generation (`Cs2LikeConsoleClient`, `Cs2LikeConsoleDataGenerator`).
- `src/main/resources/` — `fabric.mod.json`, mixin configs, and `assets/cs2-like-console/`.
- `src/client/resources/` — client mixin config (`cs2-like-console.client.mixins.json`).
- `build.gradle`, `gradle.properties`, `settings.gradle` — build config; versions live in `gradle.properties`.

Keep shared logic in `main`; put rendering, keybinds, and client mixins in `client`.

## Build, Test, and Development Commands

- `./gradlew build` — compile, remap, and produce the mod jar in `build/libs/`.
- `./gradlew runClient` — launch a dev Minecraft client with the mod loaded.
- `./gradlew runDatagen` — run the Fabric data generator output.
- `./gradlew genSources` — decompile Minecraft sources for reference.
- `./gradlew clean` — clear build output when resources look stale.

Requires JDK 25 (see `targetJavaVersion` in `build.gradle`).

## Coding Style & Naming Conventions

- Java 25, 4-space indentation, UTF-8 encoding enforced by the build.
- Classes `PascalCase`; methods and fields `camelCase`; constants `UPPER_SNAKE_CASE`.
- Package root is `com.lunarlake.cs2LikeConsole`; client classes live under the `.client` subpackage.
- Indent mixin JSON and other resource files with 2 spaces.
- No formatter or linter is configured; match the style of surrounding files and let IntelliJ format on save.

## Testing Guidelines

There is no test suite configured. Verify changes manually with `./gradlew runClient` and confirm the mod loads without mixin or entrypoint errors. If you add tests, place them under `src/test/java` and wire up a Gradle `test` task.

## Commit & Pull Request Guidelines

The repository has no commit history yet, so follow conventional practice: short imperative subjects (e.g. `Add console overlay rendering`) and a body explaining the why when non-obvious. Pull requests should describe the change, list manual verification steps, and note any Minecraft/Fabric API version changes; include screenshots for visible in-game behavior.
