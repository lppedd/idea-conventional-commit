package com.github.lppedd.cc

class TypeCompletionTest : BaseTest() {
  fun test_type_completion_with_empty_line() {
    testCompletionVariants(
      "<caret>",
      "refactor",
      "fix",
      "feat",
      "chore",
      "build",
      "style",
      "test",
      "docs",
      "perf",
      "ci",
      "revert",
    )
  }

  fun test_type_completion_with_incomplete_type() {
    testCompletionVariants("f<caret>", "fix", "feat", "refactor", "perf")
    testCompletionVariants("buil<caret>", "build")
  }

  fun test_type_completion_with_already_completed_type() {
    testCompletionVariants("build<caret>", "build")
  }

  fun test_type_insertion_with_empty_file() {
    testCompletionSelectItemOrFirst(
      before = "<caret>",
      after = "refactor",
      selectedItem = "refactor"
    )
  }

  fun test_type_overwrite_1() {
    testCompletionSelectItemOrFirst(
      before = "refacto<caret>",
      after = "refactor",
      selectedItem = "refactor"
    )
  }

  fun test_type_overwrite_2() {
    testCompletionSelectItemOrFirst(
      before = "build<caret>(): my description",
      after = "build<caret>(): my description",
      selectedItem = "build"
    )
  }

  fun test_type_insertion_with_complex_line_1() {
    testCompletionSelectItemOrFirst(
      before = "r<caret>efx build(n",
      after = "revert build(n",
      selectedItem = "revert"
    )
  }

  fun test_type_insertion_with_complex_line_2() {
    testCompletionSelectItemOrFirst(
      before = "refactor t<caret>est(npm): my description",
      after = "refactor style(npm): my description",
      selectedItem = "style"
    )
  }

  fun test_type_insertion_with_complex_line_3() {
    testCompletionSelectItemOrFirst(
      before = "ref t<caret>est",
      after = "ref style",
      selectedItem = "style"
    )
  }

  fun test_type_insertion_with_complex_line_4() {
    testCompletionSelectItemOrFirst(
      before = "r<caret>ef(npm) build(n",
      after = "revert(npm) build(n",
      selectedItem = "revert"
    )
  }

  fun test_type_insertion_with_complex_line_5() {
    testCompletionSelectItemOrFirst(
      before = "r<caret>ef(npm): build(n",
      after = "revert(npm): build(n",
      selectedItem = "revert"
    )
  }

  fun test_type_insertion_with_complex_line_6() {
    testCompletionSelectItemOrFirst(
      before = "x  r<caret>ef(npm): build(n",
      after = "x  revert(npm): build(n",
      selectedItem = "revert"
    )
  }
}
