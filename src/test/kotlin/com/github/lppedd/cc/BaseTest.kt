package com.github.lppedd.cc

import com.github.lppedd.cc.api.CommitTokenProviderService
import com.github.lppedd.cc.provider.TestProvider
import com.intellij.openapi.components.service
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

@Suppress("ReplaceNotNullAssertionWithElvisReturn")
abstract class BaseTest : LightJavaCodeInsightFixtureTestCase() {
  companion object {
    const val TEST_FILE_NAME: String = "test.conventionalcommit"
  }

  override fun setUp() {
    super.setUp()

    val providerService = project.service<CommitTokenProviderService>()
    providerService.registerSubjectProvider(TestProvider, myFixture.testRootDisposable)
    providerService.registerBodyProvider(TestProvider, myFixture.testRootDisposable)
    providerService.registerFooterTypeProvider(TestProvider, myFixture.testRootDisposable)
    providerService.registerFooterValueProvider(TestProvider, myFixture.testRootDisposable)
  }

  protected fun testCompletionVariants(text: String, vararg variants: String) {
    myFixture.configureByText(TEST_FILE_NAME, text)
    myFixture.file.document!!.putUserData(CommitMessage.DATA_KEY, CommitMessage(myFixture.project))
    myFixture.testCompletionVariants(TEST_FILE_NAME, *variants)
  }

  @Suppress("SameParameterValue")
  protected fun testCompletionVariantsContain(text: String, vararg variants: String) {
    myFixture.configureByText(TEST_FILE_NAME, text)
    myFixture.file.document!!.putUserData(CommitMessage.DATA_KEY, CommitMessage(myFixture.project))

    val result = myFixture.getCompletionVariants(TEST_FILE_NAME)!!
    assertContainsElements(result, *variants)
  }

  protected fun testCompletionSelectItemOrFirst(before: String, after: String, selectedItem: String? = null) {
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
