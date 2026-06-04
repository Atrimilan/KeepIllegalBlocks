import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.4.2"
    id("com.modrinth.minotaur") version "2.+"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
}

val groupId: String by project
val projectVersion: String by project

group = groupId
version = projectVersion

val paperApiVersion: String by project
val packetEventsVersion: String by project
val bStatsVersion: String by project

dependencies {
    // PaperMC
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    testImplementation("io.papermc.paper:paper-api:$paperApiVersion")
    // bStats (shaded jar)
    implementation("org.bstats:bstats-bukkit:$bStatsVersion")
    // PacketEvents (optional plugin)
    compileOnly("com.github.retrooper:packetevents-spigot:$packetEventsVersion")
    testImplementation("com.github.retrooper:packetevents-spigot:$packetEventsVersion")
    // JUnit & Mockito
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.+")
    testImplementation("org.mockito:mockito-junit-jupiter:5.+")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val localServerDir = "local-server" // Change the server directory here
val serverPort = 25565  // Change the server port here

val sanitizedPaperVersion = paperApiVersion
    .replace(Regex("\\.build.*"), "") // 26.1.2.build.+  ->  26.1.2
    .replace("-R0.1-SNAPSHOT", "") // 1.20.6-R0.1-SNAPSHOT  ->  1.20.6

tasks {
    runServer {
        version.set(sanitizedPaperVersion)
        runDirectory.set(file("$localServerDir/$sanitizedPaperVersion"))

        val customJvmArgs = mutableListOf( // Add custom JVM arguments here
            "-Dcom.mojang.eula.agree=true", "-Dserver.port=$serverPort"
        )

        if (providers.gradleProperty("keepillegalblocks.debug").isPresent) customJvmArgs.add("-Dkeepillegalblocks.debug=true")

        jvmArgs(customJvmArgs)
        println("Starting with JVM args: $jvmArgs")

        doFirst {
            val serverProperties = file("$localServerDir/$sanitizedPaperVersion/server.properties")
            val bukkitYml = file("$localServerDir/$sanitizedPaperVersion/bukkit.yml")

            listOf(serverProperties, bukkitYml).forEach { file ->
                file.parentFile.mkdirs()
            }
            serverProperties.writeText( // Edit server.properties here
                """
                allow-nether=false
                enable-command-block=true
                difficulty=peaceful
                gamemode=creative
                level-type=minecraft\:flat
                motd=A local Paper server
                """.trimIndent()
            )
            bukkitYml.writeText( // Edit bukkit.yml here
                """
                settings:
                  allow-end: false
                """.trimIndent()
            )
        }
    }

    named<ShadowJar>("shadowJar") {
        configurations = listOf(project.configurations.runtimeClasspath.get())
        archiveClassifier.set("") // Remove the "-all" classifier

        // bStats configuration
        dependencies { exclude { it.moduleGroup != "org.bstats" } }
        relocate("org.bstats", project.group.toString())
    }

    jar {
        enabled = false // Disable JAR in favor of ShadowJAR
    }

    assemble {
        dependsOn("shadowJar")
    }

    named("modrinth") {
        dependsOn("modrinthSyncBody") // Sync body on every Modrinth publishing
    }

    test {
        useJUnitPlatform()
    }
}

val projectVersionType: String by project
val compatibleLoaders: String by project
val supportedGameVersions: String by project

modrinth {
    token.set(providers.environmentVariable("MODRINTH_TOKEN"))
    projectId.set(providers.environmentVariable("MODRINTH_PROJECT_ID"))

    versionNumber.set(projectVersion)
    versionType.set(projectVersionType)
    gameVersions.addAll(supportedGameVersions.split(","))
    loaders.addAll(compatibleLoaders.split(","))

    changelog.set(file("changelogs/$projectVersion.md").readText())
    syncBodyFrom.set(file("MODRINTH_README.md").readText())

    uploadFile.set(tasks.shadowJar)

    dependencies {
        // https://modrinth.com/plugin/packetevents/version/2.12.1+spigot
        optional.version("packetevents", "2.12.1+spigot")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(resolveJavaVersion()))
}

fun resolveJavaVersion(): Int {
    return when {
        paperApiVersion.startsWith("26.") -> 25 // Java 25 since Minecraft 26.1
        else -> 21 // Java 21 for Minecraft versions 1.20.5 to 1.21.11
    }
}
