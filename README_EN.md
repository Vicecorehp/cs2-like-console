# cs2-like-console

English | [中文](README.md)

A CS2-style console for the Minecraft client: press `` ` `` to open a full-screen panel and run vanilla or server commands.

## Features

- Full-screen dark console panel replacing the vanilla chat input
- Vanilla command completion: type `give ` and press `Tab`
- `↑` / `↓` for command history, mouse wheel and `PageUp` / `PageDown` for log scrolling
- Ring-buffered log keeping the last 300 lines
- Built-in client commands: `disconnect`, `exit`, `quit`

## Requirements

| Item | Version |
| --- | --- |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.5+ |
| Fabric API | 0.161.0+26.2 |
| Java | 25 |

Client-side only; nothing is needed on the server.

## Installation

1. Install Fabric Loader and Fabric API.
2. Drop `build/libs/cs2-like-console-<version>.jar` into `.minecraft/mods/`.

## Usage

In-game, press `` ` `` to open the console (press `Esc` to close). Type a command and hit Enter; the leading `/` is optional.

| Command | Effect |
| --- | --- |
| `disconnect` | Leave the current world and return to the main menu |
| `exit` / `quit` | Quit the game |

Any other input is sent as a vanilla command, for example `give @s stone` or `time set day`.

The key can be changed under Options → Controls → Key Binds, category `CS2 Like Console`.

## Building from Source

JDK 25 is required. Versions live in `gradle.properties`.

```bash
./gradlew build        # build the jar into build/libs/
./gradlew runClient    # launch a dev client with the mod loaded
./gradlew genSources   # decompile Minecraft sources
```

## Layout

- `src/main/java/com/lunarlake/cs2LikeConsole/` — common code and mod entrypoint
- `src/client/java/com/lunarlake/cs2LikeConsole/client/` — console UI, commands, and key mapping
- `src/main/resources/` — `fabric.mod.json` and mixin configs
- `src/client/resources/` — language files, icon, and client mixin config

## License

All Rights Reserved. See `LICENSE.txt`.
