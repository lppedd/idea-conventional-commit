import com.github.lppedd.cc.document
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

private const val TEST_FILE_NAME = "test.txt"

internal class ConventionalCommitCompletionTest : LightJavaCodeInsightFixtureTestCase() {
  fun test_type_completion_with_empty_line() {
    testCompletionVariants(
      "<caret>",
      "refactor",
      "fix",
      "feat",
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

  fun test_type_insertion() {
    testCompletionSelectItemOrFirst(
      "refacto<caret>",
      "refactor",
      "refactor"
    )
  }

  fun test_type_insertion_with_complex_line_1() {
    testCompletionSelectItemOrFirst(
      "r<caret>efx build(n",
      "revert build(n",
      "revert"
    )
  }

  fun test_type_insertion_with_complex_line_2() {
    testCompletionSelectItemOrFirst(
      "refactor t<caret>est(npm): my description",
      "refactor style(npm): my description",
      "style"
    )
  }

  fun test_type_insertion_with_complex_line_3() {
    testCompletionSelectItemOrFirst(
      "ref t<caret>est",
      "ref style",
      "style"
    )
  }

  fun test_scope_completion_with_opening_paren() {
    testCompletionVariants("build(<caret>", "npm", "gulp", "broccoli")
  }

  fun test_scope_insertion_with_incomplete_scope() {
    testCompletionSelectItemOrFirst(
      "build(np<caret>",
      "build(npm): <caret>",
      "npm"
    )
  }

  fun test_scope_insertion_with_standard_subject() {
    testCompletionSelectItemOrFirst(
      "build(<caret>npm): my description",
      "build(gulp): <caret>my description",
      "gulp"
    )
  }

  fun test_scope_insertion_with_non_standard_subject() {
    testCompletionSelectItemOrFirst(
      "build(g<caret>oogle):my description",
      "build(gulp):<caret>my description",
      "gulp"
    )
  }

  fun test_opening_paren_insertion_after_type() {
    prepareFile("build<caret>")
    myFixture.type("(")
    myFixture.checkResult("build(<caret>)")
  }

  fun test_opening_paren_insertion_with_it_already_present() {
    prepareFile("build<caret>(")
    myFixture.type("(")
    myFixture.checkResult("build(<caret>)")
  }

  fun test_opening_paren_insertion_with_both_already_present() {
    prepareFile("build<caret>()")
    myFixture.type("(")
    myFixture.checkResult("build(<caret>)")
  }

  fun test_opening_paren_insertion_with_complete_scope() {
    prepareFile("build<caret>(gulp): my description")
    myFixture.type("(")
    myFixture.checkResult("build(<caret>gulp): my description")
  }

  fun test_closing_paren_insertion_with_it_already_present() {
    prepareFile("build(<caret>)")
    myFixture.type(")")
    myFixture.checkResult("build()<caret>")
  }

  fun test_tab_before_scope_opening_paren() {
    prepareFile("refactor<caret>(scope): my description")
    myFixture.type("\t")
    myFixture.checkResult("refactor(<caret>scope): my description")
  }

  fun test_tab_before_scope_closing_paren() {
    prepareFile("refactor(scope<caret>): my description")
    myFixture.type("\t")
    myFixture.checkResult("refactor(scope)<caret>: my description")
  }

  fun test_colon_insertion_before_colon_with_standard_subject() {
    prepareFile("refactor(scope)<caret>: my description")
    myFixture.type(":")
    myFixture.checkResult("refactor(scope): <caret>my description")
  }

  fun test_colon_insertion_before_colon_with_non_standard_subject() {
    prepareFile("r(scope)<caret>:my description")
    myFixture.type(":")
    myFixture.checkResult("r(scope):<caret>my description")
  }

  fun test_colon_insertion_before_colon_without_scope() {
    prepareFile("build<caret>: my description")
    myFixture.type(":")
    myFixture.checkResult("build: <caret>my description")
  }

  private fun testCompletionVariants(text: String, vararg variants: String) {
    myFixture.configureByText(TEST_FILE_NAME, text)
    myFixture.file.document?.putUserData(CommitMessage.DATA_KEY, CommitMessage(myFixture.project))
    myFixture.testCompletionVariants(TEST_FILE_NAME, *variants)
  }

  private fun testCompletionSelectItemOrFirst(before: String, after: String, selectedItem: String? = null) {
    testByFile(before, after) {
      myFixture.completeBasic()

      if (selectedItem != null) {
        myFixture.lookupElements
          ?.find { it.lookupString == selectedItem }
          ?.also { myFixture.lookup.currentItem = it }
        ?: throw IllegalStateException("LookupElement '$selectedItem' not found")
      }

      myFixture.finishLookup('\n')
    }
  }

  private fun testByFile(before: String, after: String, action: () -> Unit) {
    prepareFile(before)
    action()
    myFixture.checkResult(after.trimIndent())
  }

  private fun prepareFile(text: String) {
    val file = myFixture.addFileToProject(TEST_FILE_NAME, text)
    file.document?.putUserData(CommitMessage.DATA_KEY, CommitMessage(myFixture.project))
    myFixture.configureFromExistingVirtualFile(file.virtualFile)
  }
}
