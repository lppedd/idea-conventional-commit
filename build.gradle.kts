@file:Suppress("TrailingComma", "PublicApiImplicitType")

import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  java
  id("org.jetbrains.intellij") version "1.6.0"
  id("org.jetbrains.grammarkit") version "2021.2.2"
  kotlin("jvm") version "1.7.10"
}

group = "com.github.lppedd"

repositories {
  maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
  maven("https://jitpack.io")
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8", "1.7.10"))

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

grammarKit {
  jflexRelease.set("1.7.0-1")
  grammarKitRelease.set("2021.1.2")
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
val generateConventionalCommitLexer = task<GenerateLexerTask>("generateConventionalCommitLexer") {
  source.set("src/main/kotlin/com/github/lppedd/cc/language/lexer/conventionalCommit.flex")
  targetDir.set("src/main/gen/com/github/lppedd/cc/language/lexer")
  targetClass.set("ConventionalCommitFlexLexer")
  purgeOldFiles.set(true)
}

tasks {
  runIde {
    if (project.hasProperty("dcevmExecutable")) {
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

    dependsOn(generateConventionalCommitLexer)
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
