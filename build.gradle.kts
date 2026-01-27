import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.3.1"
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

val groupId: String by project
val projectVersion: String by project
val paperApiVersion: String by project

group = groupId
version = projectVersion

dependencies {
    // PaperMC (using paperweight-userdev)
    paperweight.paperDevBundle(paperApiVersion)
    // bStats
    implementation("org.bstats:bstats-bukkit:3.1.0")
    // JUnit & Mockito
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.+")
    testImplementation("org.mockito:mockito-junit-jupiter:5.+")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

val localServerDir = "local-server" // Change the server directory here
val serverPort = 25565  // Change the server port here

tasks {
    runServer {
        runDirectory.set(file(localServerDir))

        val customJvmArgs = mutableListOf( // Add custom JVM arguments here
            "-Dcom.mojang.eula.agree=true", "-Dserver.port=$serverPort"
        )

        if (providers.gradleProperty("keepillegalblocks.debug").isPresent) customJvmArgs.add("-Dkeepillegalblocks.debug=true")

        jvmArgs(customJvmArgs)
        println("Starting with JVM args: $jvmArgs")

        doFirst {
            val serverProperties = file("$localServerDir/server.properties")
            val bukkitYml = file("$localServerDir/bukkit.yml")

            listOf(serverProperties, bukkitYml).forEach { file ->
                file.parentFile.mkdirs()
            }
            serverProperties.writeText( // Edit server.properties here
                """
                allow-nether=false
                enable-command-block=true
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

    test {
        useJUnitPlatform()
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
