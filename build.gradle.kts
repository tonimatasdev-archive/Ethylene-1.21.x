plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
}

val modVersion: String by extra
val minecraftVersion: String by extra
val loaderVersion: String by extra

version = modVersion
group = "net.ethylenemc"

base.archivesName.set("ethylene")

loom.accessWidenerPath.set(file("src/main/resources/ethylene.accesswidener"))

repositories {
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    
    // Spigot
    implementation("org.spigotmc:spigot-api:${minecraftVersion}-R0.1-SNAPSHOT")
    implementation("jline:jline:2.12.1")
    implementation("commons-lang:commons-lang:2.6")
}
