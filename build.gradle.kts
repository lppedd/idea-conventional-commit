import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  java
  id("org.jetbrains.intellij") version "0.4.18"
  kotlin("jvm") version "1.4-M1"
}

group = "com.github.lppedd"
version = "0.15.0"

repositories {
  maven("https://dl.bintray.com/kotlin/kotlin-eap")
  maven("https://jitpack.io")
  mavenCentral()
}

dependencies {
  compileOnly(kotlin("stdlib-jdk8", "1.3.31"))

  implementation("cglib:cglib-nodep:3.3.0")
  implementation("org.json", "json", "20190722")
  implementation("com.github.everit-org.json-schema", "org.everit.json.schema", "1.12.1")

  testImplementation("junit:junit:4.12")
}

intellij {
  version = "IU-192.5728.98"
  downloadSources = true
  pluginName = "idea-conventional-commit"
  setPlugins("java")
}

configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
  val kotlinSettings: KotlinCompile.() -> Unit = {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf(
      "-Xno-call-assertions",
      "-Xno-receiver-assertions",
      "-Xno-param-assertions",
      "-Xjvm-default=enable",
      "-Xallow-kotlin-package",
      "-Xopt-in=kotlin.ExperimentalUnsignedTypes",
      "-Xopt-in=kotlin.contracts.ExperimentalContracts",
      "-XXLanguage:+InlineClasses"
    )
  }

  compileKotlin(kotlinSettings)
  compileTestKotlin(kotlinSettings)

  patchPluginXml {
    version(project.version)
    sinceBuild("192.5728")
    untilBuild("201.*")
    pluginDescription(File("plugin-description.html").readText(Charsets.UTF_8))
    changeNotes(File("change-notes/${version.replace('.', '_')}.html").readText(Charsets.UTF_8))
  }
}
