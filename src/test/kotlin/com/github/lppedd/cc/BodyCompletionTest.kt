package com.github.lppedd.cc

class BodyCompletionTest : BaseTest() {
  fun test_body_insertion() {
    testCompletionSelectItemOrFirst(
      before = """
        |build(google): my description
        |
        |<caret>
      """.trimMargin(),
      after = """
        |build(google): my description
        |
        |Example body<caret>
      """.trimMargin(),
      selectedItem = "Example body"
    )
  }

  fun test_body_multiline_insertion() {
    testCompletionSelectItemOrFirst(
      before = """
        |build(google): my description
        |
        |<caret>
      """.trimMargin(),
      after = """
        |build(google): my description
        |
        |Example of a
        |multiline body
        |as it spawns multiple lines.<caret>
      """.trimMargin(),
      selectedItem = "Example of a\nmultiline body\nas it spawns multiple lines."
    )
  }

  fun test_body_multi_line_overwrite_single_line() {
    testCompletionSelectItemOrFirst(
      before = """
        |build(google): my description
        |
        |Example <caret>of a
        |multiline body
        |as it spawns multiple lines.
        |
        |Not included
      """.trimMargin(),
      after = """
        |build(google): my description
        |
        |Example body<caret>
        |
        |Not included
      """.trimMargin(),
      selectedItem = "Example body"
    )
  }

  fun test_body_single_line_overwrite_multi_line() {
    testCompletionSelectItemOrFirst(
      before = """
        |build(google): my description
        |
        |Example<caret> body
        |
        |Not included
      """.trimMargin(),
      after = """
        |build(google): my description
        |
        |Example of a
        |multiline body
        |as it spawns multiple lines.<caret>
        |
        |Not included
      """.trimMargin(),
      selectedItem = "Example of a\nmultiline body\nas it spawns multiple lines."
    )
  }

  fun test_body_single_line_overwrite_multi_line_with_double_new_line_inside() {
    testCompletionSelectItemOrFirst(
      before = """
        |build(google): my description
        |
        |Example<caret> body
        |
        |Not included
      """.trimMargin(),
      after = """
        |build(google): my description
        |
        |Example of a
        |multiline body
        |
        |with double new line.<caret>
        |
        |Not included
      """.trimMargin(),
      selectedItem = "Example of a\nmultiline body\n\nwith double new line."
    )
  }
}
