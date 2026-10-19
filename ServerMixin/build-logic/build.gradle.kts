import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(libs.build.paperweight)
  implementation(libs.build.shadow)
  implementation(libs.build.spotless)
}

dependencies {
  compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

java {
  sourceCompatibility = JavaVersion.VERSION_25
  targetCompatibility = JavaVersion.VERSION_25
}

kotlin {
  target {
    compilerOptions {
      jvmTarget.set(JvmTarget.fromTarget("25"))
    }
  }
}
