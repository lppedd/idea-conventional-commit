@file:JvmName("CCConstants")

package com.github.lppedd.cc

const val APP_NAME: String = "ConventionalCommit"
const val STORAGE_FILE: String = "conventionalCommit.xml"
const val DEFAULT_FILE: String = "conventionalcommit.json"
const val COAUTHORS_FILE: String = "conventionalcommit.coauthors"
const val DEFAULT_SCHEMA: String = "conventionalcommit.schema.json"
const val DEFAULT_PROVIDER_ID: String = "e9d4e8de-79a0-48b8-b1ba-b4161e2572c0"
const val DEFAULT_VCS_PROVIDER_ID: String = "f3be5600-71b8-401c-bf50-e2465d8efca8"

internal const val WEIGHT_SCOPE = 10000
internal const val WEIGHT_SUBJECT = 20000
internal const val WEIGHT_FOOTER = 10000

// Those three could appear in the same completion invocation
internal const val WEIGHT_FOOTER_TYPE = 10_000
internal const val WEIGHT_BODY = 1_000_000
internal const val WEIGHT_TYPE = 100_000_000
