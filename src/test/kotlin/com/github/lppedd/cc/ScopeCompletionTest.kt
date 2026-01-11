package com.github.lppedd.cc

class ScopeCompletionTest : BaseTest() {
  fun test_scope_completion() {
    testCompletionVariants("build(<caret>", "npm", "gradle", "maven")
    testCompletionVariants("build(<caret>)", "npm", "gradle", "maven")
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
      after = "build(gradle): <caret>my description",
      selectedItem = "gradle"
    )
  }

  fun test_scope_insertion_with_non_standard_subject() {
    testCompletionSelectItemOrFirst(
      before = "build(g<caret>oogle):my description",
      after = "build(gradle):<caret>my description",
      selectedItem = "gradle"
    )
  }
}
