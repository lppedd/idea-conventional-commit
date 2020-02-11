plugins {
  java
  id("org.jetbrains.intellij") version "0.4.16"
  kotlin("jvm") version "1.3.70-eap-184"
}

group = "com.github.lppedd"
version = "0.4.0"

repositories {
  maven("https://dl.bintray.com/kotlin/kotlin-eap")
  maven("https://jitpack.io")
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8", "1.3.70-eap-184"))
  implementation("org.json", "json", "20190722")
  implementation("com.github.everit-org.json-schema", "org.everit.json.schema", "1.12.1")
  testCompile("junit:junit:4.12")
}

configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-XXLanguage:+NewInference"
  }

  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-XXLanguage:+NewInference"
  }

  patchPluginXml {
    version(project.version)
    sinceBuild("192")
    untilBuild("201.*")
    pluginDescription(File("plugin-description.html").readText(Charsets.UTF_8))
    changeNotes(File("change-notes/${version.replace('.', '_')}.html").readText(Charsets.UTF_8))
  }
}

intellij {
  version = "IU-2019.2"
  downloadSources = true
  pluginName = "Conventional Commit"
  setPlugins("java")
}
