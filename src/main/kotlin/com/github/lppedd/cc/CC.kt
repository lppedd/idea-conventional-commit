package com.github.lppedd.cc

/**
 * @author Edoardo Luppi
 */
@Suppress("ConstPropertyName")
public object CC {
  public const val PluginId: String = "com.github.lppedd.idea-conventional-commit"
  public const val AppName: String = "ConventionalCommit"

  public object Settings {
    public const val File: String = "conventionalCommit.xml"
  }

  public object Provider {
    public const val MaxItems: Int = 200
  }

  public object CoAuthors {
    public const val File: String = "conventionalcommit.coauthors"
  }

  public object Tokens {
    public const val File: String = "conventionalcommit.json"
    public const val SchemaFile: String = "conventionalcommit.schema.json"
  }

  public object Registry {
    public const val VcsEnabled: String = "com.github.lppedd.cc.providers.vcs"
  }
}
