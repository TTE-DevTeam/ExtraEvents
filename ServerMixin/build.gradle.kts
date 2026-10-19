plugins {
  id("mod.base-conventions")
  id("com.gradleup.shadow") version "9.3.1"
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.23"
}

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
  remapper("net.fabricmc:tiny-remapper:0.10.4:fat")

  compileOnly(libs.ignite)
  compileOnly(libs.mixin)
  compileOnly(libs.mixinExtras)
  
  implementation("de.dertoaster:extraevents-api")

  paperweight.paperDevBundle(libs.versions.paper)
}
