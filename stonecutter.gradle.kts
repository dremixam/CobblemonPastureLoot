plugins {
    id("dev.kikugie.stonecutter")
}

// The active node is the one the IDE indexes and the one used by tasks without project prefix
// (./gradlew build, ./gradlew runClient). Switch with the "Stonecutter switchTo..." run configurations.
stonecutter active "1.21.1-fabric"
