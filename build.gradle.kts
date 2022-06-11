@file:Suppress("TrailingComma")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  java
  id("org.jetbrains.intellij") version "1.6.0"
  kotlin("jvm") version "1.7.0"
}

group = "com.github.lppedd"

repositories {
  maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
  maven("https://jitpack.io")
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8", "1.7.0"))

  implementation("commons-validator", "commons-validator", "1.7") {
    exclude("commons-beanutils", "commons-beanutils")
  }

  implementation("org.json", "json", "20220320")
  implementation("com.github.everit-org.json-schema", "org.everit.json.schema", "1.14.1")

  testImplementation("junit:junit:4.13.2")
}

intellij {
  type.set(properties("platformType"))
  version.set(properties("platformVersion"))
  downloadSources.set(true)
  pluginName.set("idea-conventional-commit")
  plugins.set(listOf("java"))
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

sourceSets {
  main {
    java {
      srcDir("src/main/gen")
    }
  }
}

/** Points to the Java executable (usually `java.exe`) of a DCEVM-enabled JVM. */
val dcevmExecutable: String? by project

tasks {
  runIde {
    if (project.hasProperty("dcevm")) {
      val dcevm = dcevmExecutable ?: return@runIde

      if (dcevm.isNotBlank()) {
        setExecutable(dcevm)
      }
    }
  }

  val kotlinSettings: KotlinCompile.() -> Unit = {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += listOf(
      "-Xno-call-assertions",
      "-Xno-receiver-assertions",
      "-Xno-param-assertions",
      "-Xjvm-default=all",
      "-Xallow-kotlin-package",
      "-opt-in=kotlin.ExperimentalStdlibApi",
      "-opt-in=kotlin.ExperimentalUnsignedTypes",
      "-opt-in=kotlin.contracts.ExperimentalContracts",
      "-XXLanguage:+InlineClasses",
      "-XXLanguage:+UnitConversion"
    )
  }

  compileKotlin(kotlinSettings)
  compileTestKotlin(kotlinSettings)

  patchPluginXml {
    version.set(project.version.toString())
    sinceBuild.set(properties("pluginSinceBuild"))
    untilBuild.set(properties("pluginUntilBuild"))

    val projectPath = projectDir.path
    pluginDescription.set((File("$projectPath/plugin-description.html").readText(Charsets.UTF_8)))
    changeNotes.set((File("$projectPath/change-notes/${version.get().replace('.', '_')}.html").readText(Charsets.UTF_8)))
  }
}
