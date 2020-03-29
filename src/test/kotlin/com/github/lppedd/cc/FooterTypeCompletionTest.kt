package com.github.lppedd.cc

class FooterTypeCompletionTest : BaseTest() {
  fun test_footer_type_completion() {
    testCompletionVariantsContain(
      text = """
        |build: my description
        |
        |<caret>
      """.trimMargin(),
      "BREAKING CHANGE",
      "Closes",
      "Implements"
    )
  }

  fun test_footer_type_insertion() {
    testCompletionSelectItemOrFirst(
      before = """
        |refactor: my description
        |
        |<caret>
      """.trimMargin(),
      after = """
        |refactor: my description
        |
        |BREAKING CHANGE: <caret>
      """.trimMargin(),
      selectedItem = "BREAKING CHANGE"
    )
  }

  fun test_footer_type_overwrite() {
    testCompletionSelectItemOrFirst(
      before = """
        |build(npm): description
        |
        |<caret>BREAKING CHANGE: footer value
      """.trimMargin(),
      after = """
        |build(npm): description
        |
        |Closes: <caret>footer value
      """.trimMargin(),
      selectedItem = "Closes"
    )
  }

  fun test_footer_type_overwrite_without_space_before_value() {
    testCompletionSelectItemOrFirst(
      before = """
        |build(npm): description
        |
        |<caret>Implements:footer value
      """.trimMargin(),
      after = """
        |build(npm): description
        |
        |Closes:<caret>footer value
      """.trimMargin(),
      selectedItem = "Closes"
    )
  }

  fun test_footer_type_overwrite_with_prefix() {
    testCompletionSelectItemOrFirst(
      before = """
        |build(npm): description
        |
        |I<caret>mplements: footer value
      """.trimMargin(),
      after = """
        |build(npm): description
        |
        |BREAKING CHANGE: <caret>footer value
      """.trimMargin(),
      selectedItem = "BREAKING CHANGE"
    )
  }
}
