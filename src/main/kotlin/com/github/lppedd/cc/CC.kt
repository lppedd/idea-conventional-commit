package com.github.lppedd.cc

/**
 * @author Edoardo Luppi
 */
object CC {
  const val PluginId: String = "com.github.lppedd.idea-conventional-commit"
  const val AppName: String = "ConventionalCommit"

  object Settings {
    const val File: String = "conventionalCommit.xml"
  }

  object Provider {
    const val MaxItems: Int = 200
  }

  object CoAuthors {
    const val File: String = "conventionalcommit.coauthors"
  }

  object Tokens {
    const val File: String = "conventionalcommit.json"
    const val SchemaFile: String = "conventionalcommit.schema.json"
  }

  object Registry {
    const val VcsEnabled: String = "com.github.lppedd.cc.providers.vcs"
  }
}
