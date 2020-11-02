package com.github.lppedd.cc

import com.github.lppedd.cc.completion.Priority

const val APP_NAME: String = "ConventionalCommit"
const val STORAGE_FILE: String = "conventionalCommit.xml"
const val DEFAULT_FILE: String = "conventionalcommit.json"
const val COAUTHORS_FILE: String = "conventionalcommit.coauthors"
const val DEFAULT_SCHEMA: String = "conventionalcommit.schema.json"
const val MAX_ITEMS_PER_PROVIDER: Int = 200

internal val PRIORITY_SCOPE = Priority(10_000)
internal val PRIORITY_SUBJECT = Priority(20_000)
internal val PRIORITY_FOOTER_VALUE = Priority(10_000)

// Those three could appear in the same completion invocation
internal val PRIORITY_FOOTER_TYPE = Priority(10_000)
internal val PRIORITY_BODY = Priority(1_000_000)
internal val PRIORITY_TYPE = Priority(100_000_000)
