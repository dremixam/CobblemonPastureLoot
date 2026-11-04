plugins {
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.11-SNAPSHOT"
    id("java")
    id("maven-publish")
    id("com.modrinth.minotaur") version "2.9.0"
    id("org.jetbrains.kotlin.jvm") version "2.2.20" apply false
}

// "loom.platform" comes from the node gradle.properties, fabric when absent.
val loader = loom.platform.get().name.lowercase()

// Loom remaps the Kotlin metadata of Cobblemon only when the Kotlin plugin is applied. Without it,
// Cobblemon crashes at startup in dev on Fabric (ClassNotFoundException on intermediary names).
if (loader == "fabric") {
    apply(plugin = "org.jetbrains.kotlin.jvm")
}

stonecutter.constants.match(loader, "fabric", "neoforge")

base {
    archivesName.set("${rootProject.property("archives_base_name")}-$loader")
}

version = "${rootProject.property("mod_version")}+${project.property("minecraft_version")}"
group = rootProject.property("maven_group") as String

val modId = rootProject.property("mod_id") as String
val javaVersion = (project.findProperty("java_version") as String? ?: "21").toInt()

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

java {
    withSourcesJar()
}

architectury {
    minecraft = project.property("minecraft_version") as String
    platformSetupLoomIde()
    if (loader == "fabric") fabric() else neoForge()
}

loom {
    if (loader == "fabric") {
        mods {
            create(modId) {
                sourceSet(sourceSets["main"])
            }
        }
    }

    runs {
        named("client") {
            runDir("run/client")
        }
        named("server") {
            runDir("run/server")
        }
    }
}

// The classes of the other loader do not compile here.
val excludedPlatform = if (loader == "fabric") "neoforge" else "fabric"
val basePackagePath = (rootProject.property("maven_group") as String).replace(".", "/") + "/" + modId
sourceSets["main"].java.exclude("$basePackagePath/platforms/$excludedPlatform/**")
stonecutter.filters.exclude("java/$basePackagePath/platforms/$excludedPlatform/**")

sourceSets["main"].resources.exclude(
    if (loader == "fabric") "META-INF/neoforge.mods.toml" else "fabric.mod.json"
)

stonecutter.tasks.configureSource(sourceSets["main"])

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://maven.architectury.dev/")
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.impactdev.net/repository/development/") // Cobblemon
    maven("https://thedarkcolour.github.io/KotlinForForge/") // KotlinForForge
}

dependencies {
    "minecraft"("com.mojang:minecraft:${project.property("minecraft_version")}")
    "mappings"(loom.officialMojangMappings())

    if (loader == "fabric") {
        "modImplementation"("net.fabricmc:fabric-loader:${project.property("loader_version")}")
        "modImplementation"("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
        "modImplementation"("com.cobblemon:fabric:${project.property("cobblemon_version")}")
        // NeoForge ships NightConfig itself, Fabric does not: it goes inside the jar.
        for (module in listOf("core", "toml")) {
            val nightConfig = "com.electronwill.night-config:$module:${rootProject.property("night_config_version")}"
            "implementation"(nightConfig)
            "include"(nightConfig)
        }
    } else {
        "neoForge"("net.neoforged:neoforge:${project.property("neoforge_version")}")
        "modImplementation"("com.cobblemon:neoforge:${project.property("cobblemon_version")}") {
            isTransitive = false
        }
        "runtimeOnly"("thedarkcolour:kotlinforforge-neoforge:${project.property("kotlinforforge_version")}")
        // KotlinForForge ships the Kotlin stdlib only at runtime, and the Cobblemon API exposes Kotlin types.
        "compileOnly"("org.jetbrains.kotlin:kotlin-stdlib:${project.property("kotlin_version")}")
    }

    "testImplementation"(platform("org.junit:junit-bom:5.10.2"))
    "testImplementation"("org.junit.jupiter:junit-jupiter")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// The values are declared as inputs, or a version change leaves processResources UP-TO-DATE.
tasks.processResources {
    val substitutions = mutableMapOf(
        "version" to project.version.toString(),
        "mod_id" to modId,
        "minecraft_version" to project.property("minecraft_version").toString(),
        "cobblemon_version" to project.property("cobblemon_version").toString(),
    )
    if (loader == "fabric") {
        substitutions["loader_version"] = project.property("loader_version").toString()
    } else {
        substitutions["neoforge_version"] = project.property("neoforge_version").toString()
        substitutions["neo_loader_version_range"] = project.property("neo_loader_version_range").toString()
    }
    inputs.properties(substitutions)
    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) { expand(substitutions) }
}

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE.md")) {
        rename { "${it}_$modId" }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven${loader.replaceFirstChar { it.uppercase() }}") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
}

val publishedVersion = rootProject.property("mod_version") as String

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("cobblemon-pasture-loot")
    // Must stay unique per node: both nodes share the same Minecraft version.
    versionNumber.set("$publishedVersion+${project.property("minecraft_version")}-$loader")
    versionName.set(publishedVersion)
    versionType.set(
        when {
            publishedVersion.contains("alpha") -> "alpha"
            publishedVersion.contains("beta") -> "beta"
            else -> "release"
        }
    )
    changelog.set(System.getenv("CHANGELOG") ?: "See the commits for the details of the changes.")
    uploadFile.set(tasks.named("remapJar"))
    gameVersions.set(listOf(project.property("minecraft_version") as String))
    loaders.set(listOf(loader))
    dependencies {
        required.project("cobblemon")
        if (loader == "fabric") {
            required.project("fabric-api")
        }
    }
}
