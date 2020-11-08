package com.github.lppedd.cc

import com.github.lppedd.cc.completion.Priority

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

    internal val PriorityScope = Priority(10_000)
    internal val PrioritySubject = Priority(20_000)
    internal val PriorityFooterValue = Priority(10_000)

    // Those three could appear in the same completion invocation
    internal val PriorityFooterType = Priority(10_000)
    internal val PriorityBody = Priority(1_000_000)
    internal val PriorityType = Priority(100_000_000)
  }
}
