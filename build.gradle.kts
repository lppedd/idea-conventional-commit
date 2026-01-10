import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform
import org.jetbrains.intellij.platform.gradle.utils.asPath
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun stringProperty(key: String, default: String? = null): String =
  findProperty(key)?.toString() ?: default ?: error("Expected a valid property $key")

plugins {
  java
  alias(libs.plugins.kotlin)
  alias(libs.plugins.intellijPlatform)
  alias(libs.plugins.grammarkit)
}

group = "com.github.lppedd"

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    intellijIdea(version = stringProperty("platformVersion"))

    bundledModule("intellij.platform.vcs.dvcs")
    bundledModule("intellij.platform.vcs.dvcs.impl")
    bundledModule("intellij.platform.vcs.log")
    bundledModule("intellij.platform.vcs.log.impl")

    bundledPlugin("com.intellij.java")
    bundledPlugin("com.intellij.modules.json")

    testFramework(TestFrameworkType.Plugin.Java)
    pluginVerifier()
  }

  implementation(libs.jettison)
  implementation(libs.jsonSkema)

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

kotlin {
  @Suppress("UnstableApiUsage")
  jvmToolchain {
    languageVersion = JavaLanguageVersion.of(21)
    vendor = JvmVendorSpec.JETBRAINS
  }

  explicitApiWarning()
  compilerOptions {
    languageVersion = KotlinVersion.KOTLIN_2_3
    jvmTarget = JvmTarget.JVM_21
    jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    freeCompilerArgs.addAll(
      "-Xno-call-assertions",
      "-Xno-receiver-assertions",
      "-Xno-param-assertions",
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

  withType<KotlinCompile>().configureEach {
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
    val agentPath = layout.projectDirectory.file("hotswap-agent-2.0.1.jar").asPath
    jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:HotswapAgent=external", "-javaagent:$agentPath")
  }
}
