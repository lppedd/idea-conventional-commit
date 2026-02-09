import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform
import org.jetbrains.intellij.platform.gradle.utils.asPath
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

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

val platformVersion: Provider<String> = providers.gradleProperty("platformVersion")
val pluginSinceBuild: Provider<String> = providers.gradleProperty("pluginSinceBuild")
val pluginVerifyType: Provider<String> = providers.gradleProperty("pluginVerifyType")
val pluginVersion: Provider<String> = providers.gradleProperty("version")

dependencies {
  intellijPlatform {
    intellijIdea(version = platformVersion)

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
    version = pluginVersion

    val pluginDescFile = layout.projectDirectory.file("plugin-description.html")
    description = providers.fileContents(pluginDescFile).asText

    val changeNotesPath = pluginVersion.map { "change-notes/${it.replace('.', '_')}.html" }
    val changeNotesFile = layout.projectDirectory.file(changeNotesPath)
    changeNotes = providers.fileContents(changeNotesFile).asText

    ideaVersion {
      sinceBuild = pluginSinceBuild
    }
  }

  pluginVerification {
    ides {
      if (pluginVerifyType.getOrElse("stable") == "recommended") {
        recommended()
      } else {
        create(IntelliJPlatformType.IntellijIdea, "2025.3")
      }
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
  val baseDir = "src/main/kotlin/com/github/lppedd/cc"
  val generateLangLexer by registering(GenerateLexerTask::class) {
    sourceFile = layout.projectDirectory.file("$baseDir/language/lexer/languageConventionalCommit.flex")
    targetOutputDir = layout.projectDirectory.dir("src/main/gen/lang")
    purgeOldFiles = true
  }

  val generateSpecLexer by registering(GenerateLexerTask::class) {
    sourceFile = layout.projectDirectory.file("$baseDir/parser/specConventionalCommit.flex")
    targetOutputDir = layout.projectDirectory.dir("src/main/gen/spec")
    purgeOldFiles = true
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
        srcDirs(generateLangLexer, generateSpecLexer)
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
