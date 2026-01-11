package com.github.lppedd.cc

class FooterCompletionTest : BaseTest() {
  fun test_footer_insertion() {
    testCompletionSelectItemOrFirst(
      before = """
        |build(google): my description
        |
        |BREAKING-CHANGE: Foo<caret>ter two
      """.trimMargin(),
      after = """
        |build(google): my description
        |
        |BREAKING-CHANGE: Footer one<caret>
      """.trimMargin(),
      selectedItem = "Footer one"
    )
  }

  fun test_footer_multiline_insertion() {
    testCompletionSelectItemOrFirst(
      before = """
        |build(google): my description
        |
        |BREAKING-CHANGE:<caret>
      """.trimMargin(),
      after = """
        |build(google): my description
        |
        |BREAKING-CHANGE: Long footer three
        |which spawns
        |
        |multiple lines.
        |<caret>
      """.trimMargin(),
      selectedItem = "Long footer three\nwhich spawns\n\nmultiple lines.\n"
    )
  }

  fun test_footer_multi_line_overwrite_single_line() {
    testCompletionSelectItemOrFirst(
      before = """
        |Some other text
        |
        |My-footer-type:<caret> Footer two
        |
        |Not included
      """.trimMargin(),
      after = """
        |Some other text
        |
        |My-footer-type: Long footer three
        |which spawns
        |
        |multiple lines.
        |<caret>
        |
        |Not included
      """.trimMargin(),
      selectedItem = "Long footer three\nwhich spawns\n\nmultiple lines.\n"
    )
  }

  fun test_footer_single_line_overwrite_multi_line() {
    testCompletionSelectItemOrFirst(
      before = """
        |Some other text
        |
        |BREAKING-CHANGE: <caret>Long footer three
        |which spawns
        |multiple lines.
        |
        |Not included
      """.trimMargin(),
      after = """
        |Some other text
        |
        |BREAKING-CHANGE: Footer one<caret>
        |
        |Not included
      """.trimMargin(),
    )
  }
}
