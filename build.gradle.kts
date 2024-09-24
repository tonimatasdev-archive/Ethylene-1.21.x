plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("maven-publish")
}

val modVersion: String by extra
val minecraftVersion: String by extra
val loaderVersion: String by extra

version = modVersion
group = "net.ethylenemc"

base.archivesName.set("craftethylene")

loom.accessWidenerPath.set(file("src/main/resources/craftbukkit.accesswidener"))

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    implementation("org.spigotmc:spigot-api:${minecraftVersion}-R0.1-SNAPSHOT")
    implementation("jline:jline:2.12.1")
    implementation("commons-lang:commons-lang:2.6")
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    
    repositories {
    }
}
