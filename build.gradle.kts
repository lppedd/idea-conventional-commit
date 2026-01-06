@file:Suppress("VulnerableLibrariesLocal", "ConvertLambdaToReference")

import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

fun stringProperty(key: String, default: String? = null): String =
  findProperty(key)?.toString() ?: default ?: error("Expected a valid property $key")

plugins {
  java
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.jetbrains.intellij.platform)
  alias(libs.plugins.jetbrains.grammarkit)
}

group = "com.github.lppedd"

repositories {
  mavenCentral()

  // For org.everit.json.schema
  maven("https://jitpack.io")

  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    create(type = stringProperty("platformType"), version = stringProperty("platformVersion"))

    bundledModule("intellij.platform.vcs.dvcs")
    bundledModule("intellij.platform.vcs.dvcs.impl")
    bundledModule("intellij.platform.vcs.log")
    bundledModule("intellij.platform.vcs.log.impl")

    bundledPlugin("com.intellij.java")
    bundledPlugin("com.intellij.modules.json")

    testFramework(TestFrameworkType.Plugin.Java)
    pluginVerifier()
  }

  implementation(libs.commons.validator) {
    exclude("commons-beanutils", "commons-beanutils")
  }

  implementation(libs.json.jettison)
  implementation(libs.json.skema)

  // TODO: move to catalog if it is really needed
  testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
  pluginConfiguration {
    val versionStr = stringProperty("version")
    version = versionStr

    val descriptionFile = file("plugin-description.html")
    description = descriptionFile.readText()

    val changeNotesFile = file("change-notes/${versionStr.replace('.', '_')}.html")
    changeNotes = changeNotesFile.readText()

    ideaVersion {
      sinceBuild = stringProperty("pluginSinceBuild")
      untilBuild = null
    }
  }

  pluginVerification {
    ides {
      create(IntelliJPlatformType.IntellijIdea, "2025.3")
    }
  }
}

grammarKit {
  // See https://github.com/JetBrains/intellij-deps-jflex/
  jflexRelease = "1.9.2"
  grammarKitRelease = "2022.3.2"
}

sourceSets {
  main {
    java {
      srcDir("src/main/gen")
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
  explicitApiWarning()
  compilerOptions {
    jvmTarget = JvmTarget.JVM_21
    languageVersion = KotlinVersion.KOTLIN_2_3
    freeCompilerArgs.addAll(
        "-Xno-call-assertions",
        "-Xno-receiver-assertions",
        "-Xno-param-assertions",
        "-Xjvm-default=all",
        "-Xallow-kotlin-package",
    )

    optIn.add("kotlin.contracts.ExperimentalContracts")
  }
}

tasks {
  val generateLexer = register<GenerateLexerTask>("generateConventionalCommitLexer") {
    sourceFile = file("src/main/kotlin/com/github/lppedd/cc/language/lexer/conventionalCommit.flex")
    targetOutputDir = file("src/main/gen/com/github/lppedd/cc/language/lexer")
    purgeOldFiles = true
  }

  compileKotlin {
    dependsOn(generateLexer)
  }

  compileTestKotlin {
    dependsOn(generateLexer)
  }

  val buildApiSourceJar = register<Jar>("buildConventionalCommitApiSourceJar") {
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

  runIde {
    val dcevm = stringProperty("dcevmExecutable", default = "")

    if (dcevm.isNotBlank()) {
      @Suppress("UsePropertyAccessSyntax")
      setExecutable(dcevm)
    }
  }
}
