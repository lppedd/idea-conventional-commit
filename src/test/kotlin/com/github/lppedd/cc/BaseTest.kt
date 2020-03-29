package com.github.lppedd.cc

import com.github.lppedd.cc.api.BODY_EP
import com.github.lppedd.cc.api.FOOTER_EP
import com.github.lppedd.cc.api.SUBJECT_EP
import com.github.lppedd.cc.provider.TestProvider
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

private const val TEST_FILE_NAME = "test.txt"

abstract class BaseTest : LightJavaCodeInsightFixtureTestCase() {
  override fun setUp() {
    super.setUp()

    SUBJECT_EP.getPoint(project).registerExtension(TestProvider, myFixture.testRootDisposable)
    BODY_EP.getPoint(project).registerExtension(TestProvider, myFixture.testRootDisposable)
    FOOTER_EP.getPoint(project).registerExtension(TestProvider, myFixture.testRootDisposable)
  }

  protected fun testCompletionVariants(text: String, vararg variants: String) {
    myFixture.configureByText(TEST_FILE_NAME, text)
    myFixture.file.document!!.putUserData(CommitMessage.DATA_KEY, CommitMessage(myFixture.project))
    myFixture.testCompletionVariants(TEST_FILE_NAME, *variants)
  }

  protected fun testCompletionVariantsContain(text: String, vararg variants: String) {
    myFixture.configureByText(TEST_FILE_NAME, text)
    myFixture.file.document!!.putUserData(CommitMessage.DATA_KEY, CommitMessage(myFixture.project))

    val result = myFixture.getCompletionVariants(TEST_FILE_NAME)!!
    UsefulTestCase.assertContainsElements(result, *variants)
  }

  protected fun testCompletionSelectItemOrFirst(
      before: String,
      after: String,
      selectedItem: String? = null,
  ) {
    prepareFile(before)
    myFixture.completeBasic()

    if (selectedItem != null) {
      myFixture.lookupElements
        ?.find { it.lookupString == selectedItem }
        ?.also { myFixture.lookup.currentItem = it }
      ?: throw IllegalStateException("LookupElement '$selectedItem' not found")
    }

    myFixture.finishLookup('\n')
    myFixture.checkResult(after)
  }

  protected fun prepareFile(text: String) {
    val file = myFixture.addFileToProject(TEST_FILE_NAME, text)
    file.document!!.putUserData(CommitMessage.DATA_KEY, CommitMessage(myFixture.project))
    myFixture.configureFromExistingVirtualFile(file.virtualFile)
  }
}
