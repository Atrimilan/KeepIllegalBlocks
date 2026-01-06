import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.2"
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

        jvmArgs(
            "-Dcom.mojang.eula.agree=true", "-Dserver.port=$serverPort"
        )

        doFirst {
            // Note: if you have already run the server once, you must manually delete the following files in order to modify them
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

    test {
        useJUnitPlatform()
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
