import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform
import org.jetbrains.intellij.platform.gradle.utils.asPath
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

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

    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.Java)

    pluginVerifier()
  }

  implementation(libs.jettison)
  implementation(libs.jsonSkema)

  testImplementation(libs.junit)
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
  jflexRelease = "1.10.15"
  grammarKitRelease = "2023.3"
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
  val cleanGeneratedSources by registering(Delete::class) {
    delete(layout.projectDirectory.dir("src/main/gen"))
  }

  val baseDir = "src/main/kotlin/com/github/lppedd/cc"
  val generateLangLexer by registering(GenerateLexerTask::class) {
    dependsOn(cleanGeneratedSources)
    sourceFile = layout.projectDirectory.file("$baseDir/language/lexer/conventionalCommit.flex")
    targetOutputDir = layout.projectDirectory.dir("src/main/gen")
  }

  val generateStrictLexer by registering(GenerateLexerTask::class) {
    dependsOn(cleanGeneratedSources)
    sourceFile = layout.projectDirectory.file("$baseDir/parser/strictConventionalCommit.flex")
    targetOutputDir = layout.projectDirectory.dir("src/main/gen")
  }

  val buildApiSourceJar by registering(Jar::class) {
    from(kotlin.sourceSets.main.map(KotlinSourceSet::kotlin)) {
      include("com/github/lppedd/cc/api/*.kt")
    }

    destinationDirectory = layout.buildDirectory.dir("libs")
    archiveClassifier = "src"
  }

  sourceSets {
    main {
      java {
        srcDirs(generateLangLexer, generateStrictLexer)
      }
    }
  }

  buildPlugin {
    from(buildApiSourceJar) {
      into("lib/src")
    }
  }

  runIde {
    val agentPath = layout.projectDirectory.file("hotswap-agent-2.0.1.jar").asPath
    jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:HotswapAgent=external", "-javaagent:$agentPath")
  }
}
