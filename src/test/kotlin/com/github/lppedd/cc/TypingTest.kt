package com.github.lppedd.cc

class TypingTest : BaseTest() {
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

  fun test_colon_insertion_in_middle_of_scope() {
    prepareFile("build(np<caret>): my description")
    myFixture.type(":")
    myFixture.checkResult("build(np:<caret>): my description")
  }
}
