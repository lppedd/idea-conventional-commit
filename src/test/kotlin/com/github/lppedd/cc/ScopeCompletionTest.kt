package com.github.lppedd.cc

class ScopeCompletionTest : BaseTest() {
  fun test_scope_completion() {
    testCompletionVariants("build(<caret>", "npm", "gulp", "broccoli")
    testCompletionVariants("build(<caret>)", "npm", "gulp", "broccoli")
  }

  fun test_scope_insertion_with_incomplete_scope() {
    testCompletionSelectItemOrFirst(
      before = "build(np<caret>",
      after = "build(npm): <caret>",
      selectedItem = "npm"
    )
  }

  fun test_scope_insertion_with_standard_subject() {
    testCompletionSelectItemOrFirst(
      before = "build(<caret>npm): my description",
      after = "build(gulp): <caret>my description",
      selectedItem = "gulp"
    )
  }

  fun test_scope_insertion_with_non_standard_subject() {
    testCompletionSelectItemOrFirst(
      before = "build(g<caret>oogle):my description",
      after = "build(gulp):<caret>my description",
      selectedItem = "gulp"
    )
  }
}
