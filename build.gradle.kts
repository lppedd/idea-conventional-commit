@file:Suppress("VulnerableLibrariesLocal")

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
    jetbrainsRuntime()
  }
}

dependencies {
  intellijPlatform {
    create(type = stringProperty("platformType"), version = stringProperty("platformVersion"))
    bundledPlugin("com.intellij.java")
    testFramework(TestFrameworkType.Plugin.Java)
    pluginVerifier()
  }

  implementation(libs.commons.validator) {
    exclude("commons-beanutils", "commons-beanutils")
  }

  implementation(libs.org.json)
  implementation(libs.org.json.schema)

  // TODO: move to catalog if it is really needed
  testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
  pluginConfiguration {
    val versionStr = stringProperty("version")
    version = versionStr

    val descriptionFile = projectDir.resolve("plugin-description.html")
    description = descriptionFile.readText()

    val changeNotesFile = projectDir.resolve("change-notes/${versionStr.replace('.', '_')}.html")
    changeNotes = changeNotesFile.readText()

    ideaVersion {
      sinceBuild = stringProperty("pluginSinceBuild")
      untilBuild = stringProperty("pluginUntilBuild")
    }
  }

  pluginVerification {
    ides {
      ide(IntelliJPlatformType.IntellijIdeaCommunity, "2024.1")
      // 2024.2 fails on usages of com.intellij.dvcs, but it is not an issue
      // ide(IntelliJPlatformType.IntellijIdeaCommunity, "2024.2")
      ide(IntelliJPlatformType.IntellijIdeaCommunity, "2024.3")
    }
  }
}

grammarKit {
  // See https://github.com/JetBrains/intellij-deps-jflex/
  jflexRelease = "1.9.2"
  grammarKitRelease = "2021.1.2"
}

sourceSets {
  main {
    java {
      srcDir("src/main/gen")
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
  explicitApiWarning()
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
    languageVersion = KotlinVersion.KOTLIN_1_9
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

  runIde {
    val dcevm = stringProperty("dcevmExecutable", default = "")

    if (dcevm.isNotBlank()) {
      setExecutable(dcevm)
    }
  }
}
