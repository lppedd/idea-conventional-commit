@file:Suppress("TrailingComma", "PublicApiImplicitType")

import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  java
  id("org.jetbrains.intellij") version "1.13.2"
  id("org.jetbrains.grammarkit") version "2021.2.2"
  kotlin("jvm") version "1.8.10"
}

group = "com.github.lppedd"

repositories {
  maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
  maven("https://jitpack.io")
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib", "1.8.10"))

  implementation("commons-validator", "commons-validator", "1.7") {
    exclude("commons-beanutils", "commons-beanutils")
  }

  implementation("org.json", "json", "20230227")
  implementation("com.github.everit-org.json-schema", "org.everit.json.schema", "1.14.2")

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
val generateConventionalCommitLexer = task<GenerateLexerTask>("generateConventionalCommitLexer") {
  source.set("src/main/kotlin/com/github/lppedd/cc/language/lexer/conventionalCommit.flex")
  targetDir.set("src/main/gen/com/github/lppedd/cc/language/lexer")
  targetClass.set("ConventionalCommitFlexLexer")
  purgeOldFiles.set(true)
}

tasks {
  wrapper {
    distributionType = Wrapper.DistributionType.ALL
  }

  val kotlinSettings: KotlinCompile.() -> Unit = {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xno-call-assertions",
        "-Xno-receiver-assertions",
        "-Xno-param-assertions",
        "-Xjvm-default=all",
        "-Xallow-kotlin-package",
        "-opt-in=kotlin.contracts.ExperimentalContracts",
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

  runPluginVerifier {
    ideVersions.set(listOf(
        "IC-2020.2.1",
        "IC-2021.1",
        "IC-2022.1",
        "IC-2023.1",
    ))
  }

  runIde {
    val dcevm = project.findProperty("dcevmExecutable")

    if (dcevm is String && dcevm.isNotBlank()) {
      executable = dcevm
    }
  }
}
