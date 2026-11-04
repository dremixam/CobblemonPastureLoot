pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases/")
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
}

// One Gradle project per node (Minecraft version + loader), all built from the same src/.
// Loader specific code is split with //? if fabric/neoforge markers and the platforms/ packages.
stonecutter {
    create(rootProject) {
        versions(
            "1.21.1-fabric" to "1.21.1",
            "1.21.1-neoforge" to "1.21.1",
        )
    }
}

rootProject.name = "CobblemonPastureLoot"
