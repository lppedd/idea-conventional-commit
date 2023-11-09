@file:Suppress("VulnerableLibrariesLocal")

import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

fun properties(key: String): String =
  property(key).toString()

plugins {
  java
  alias(libs.plugins.jetbrains.intellij)
  alias(libs.plugins.jetbrains.grammarkit)
  alias(libs.plugins.kotlin.jvm)
}

group = "com.github.lppedd"

repositories {
  // For org.everit.json.schema
  maven("https://jitpack.io")
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(libs.commons.validator) {
    exclude("commons-beanutils", "commons-beanutils")
  }

  implementation(libs.org.json)
  implementation(libs.org.json.schema)

  // TODO: move to catalog if it is really needed
  testImplementation("junit:junit:4.13.2")
}

intellij {
  type = properties("platformType")
  version = properties("platformVersion")
  downloadSources = true
  pluginName = "idea-conventional-commit"
  plugins = listOf("java")
}

grammarKit {
  jflexRelease = "1.7.0-1"
  grammarKitRelease = "2021.1.2"
}

sourceSets {
  main {
    java {
      srcDir("src/main/gen")
    }
  }
}

tasks {
  wrapper {
    distributionType = Wrapper.DistributionType.ALL
  }

  kotlin {
    jvmToolchain(11)
    compilerOptions {
      jvmTarget = JvmTarget.JVM_11
      languageVersion = KotlinVersion.KOTLIN_1_9
      optIn.add("kotlin.contracts.ExperimentalContracts")
      freeCompilerArgs.addAll(
          "-Xno-call-assertions",
          "-Xno-receiver-assertions",
          "-Xno-param-assertions",
          "-Xjvm-default=all",
          "-Xallow-kotlin-package",
      )
    }
  }

  val generateLexer = task<GenerateLexerTask>("generateConventionalCommitLexer") {
    source = "src/main/kotlin/com/github/lppedd/cc/language/lexer/conventionalCommit.flex"
    targetDir = "src/main/gen/com/github/lppedd/cc/language/lexer"
    targetClass = "ConventionalCommitFlexLexer"
    purgeOldFiles = true
  }

  compileKotlin {
    dependsOn(generateLexer)
  }

  compileTestKotlin {
    dependsOn(generateLexer)
  }

  val versionStr = "$version"
  val projectPath = layout.projectDirectory

  patchPluginXml {
    version = versionStr
    sinceBuild = properties("pluginSinceBuild")
    untilBuild = properties("pluginUntilBuild")

    val pluginDescriptionFile = File("$projectPath/plugin-description.html")
    pluginDescription = pluginDescriptionFile.readText()

    val changeNotesFile = File("$projectPath/change-notes/${versionStr.replace('.', '_')}.html")
    changeNotes = changeNotesFile.readText()
  }

  val buildApiSourceJar = task<Jar>("buildConventionalCommitApiSourceJar") {
    dependsOn(generateLexer)
    from(kotlin.sourceSets.main.get().kotlin) {
      include("com/github/lppedd/cc/api/*.kt")
    }

    destinationDirectory = layout.buildDirectory.dir("libs")
    archiveClassifier = "src"
  }

  buildPlugin {
    dependsOn(buildApiSourceJar)
    from(buildApiSourceJar) {
      into("lib/src")
    }
  }

  runPluginVerifier {
    ideVersions = listOf(
        "IC-2021.1",
        "IC-2022.1",
        "IC-2023.1",
        "IC-2023.2",
    )
  }

  runIde {
    val dcevm = findProperty("dcevmExecutable")

    if (dcevm is String && dcevm.isNotBlank()) {
      executable = dcevm
    }
  }
}
