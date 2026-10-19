plugins {
    `java-library`
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23"
}

group = "de.dertoaster"
version = "1.9.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    gradlePluginPortal()
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api("org.jetbrains:annotations-java5:24.1.0")
    paperweight.paperDevBundle("26.3.build.+")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION