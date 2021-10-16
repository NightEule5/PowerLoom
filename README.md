# Power Loom

Power Loom is a Gradle plugin made to supplement Fabric Loom, providing some
quality-of-life improving features for Fabric mod development. It can generate and
modify `fabric.mod.json` files, download other mods into your dev environment for
testing, and setup publishing to both CurseForge and Modrinth simultaneously.

**This project is not finished. See the [Planned Features](#planned-features) list
for implemented vs planned features.**

## Testing

While this project is in early development, you can test by building via Jitpack:

In your `settings.gradle.kts`:
```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://jitpack.io")
    }

    plugins {
        id("dev.strixpyrr.powerloom") version "0.1.0"
    }
}
```

In your `build.gradle.kts`:
```kotlin
plugins {
    id("dev.strixpyrr.powerloom")
}
```

## Planned Features

- [x] `fabric.mod.json` generation
- [ ] Mod downloading
- [ ] CurseForge and Modrinth publishing
- [ ] Unified binaries for multi-project builds

## License

Distributed under the Apache-2.0 License. See `LICENSE` for more information.
