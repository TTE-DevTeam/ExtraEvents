plugins {
    `java-library`
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
}

group = "de.dertoaster"
version = "1.7.3"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    gradlePluginPortal()
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api("org.jetbrains:annotations-java5:24.1.0")
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION