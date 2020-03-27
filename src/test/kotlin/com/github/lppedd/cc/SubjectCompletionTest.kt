package com.github.lppedd.cc

class SubjectCompletionTest : BaseTest() {
  fun test_subject_completion() {
    testCompletionVariants("build(npm):<caret>", "subject one", "and subject two")
    testCompletionVariants("build(npm): <caret>", "subject one", "and subject two")
  }

  fun test_subject_insertion_without_scope() {
    testCompletionSelectItemOrFirst(
      before = "build: <caret>",
      after = "build: subject one<caret>",
      selectedItem = "subject one"
    )
  }

  fun test_scope_insertion_with_scope() {
    testCompletionSelectItemOrFirst(
      before = "refactor(scope):<caret>old",
      after = "refactor(scope): and subject two<caret>",
      selectedItem = "and subject two"
    )
  }
}
