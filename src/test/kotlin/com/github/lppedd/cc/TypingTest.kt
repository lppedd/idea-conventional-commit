package com.github.lppedd.cc

class TypingTest : BaseTest() {
  fun test_opening_paren_insertion_after_type() {
    prepareFile("build<caret>")
    myFixture.type("(")
    myFixture.checkResult("build(<caret>)")
  }

  fun `test_opening_paren_insertion_before_(`() {
    prepareFile("build<caret>(")
    myFixture.type("(")
    myFixture.checkResult("build(<caret>)")
  }

  fun `test_opening_paren_insertion_before_()`() {
    prepareFile("build<caret>()")
    myFixture.type("(")
    myFixture.checkResult("build(<caret>)")
  }

  fun test_opening_paren_insertion_before_complete_scope() {
    prepareFile("build<caret>(gulp): my description")
    myFixture.type("(")
    myFixture.checkResult("build(<caret>gulp): my description")
  }

  fun test_opening_paren_insertion_inside_complete_scope() {
    prepareFile("build(<caret>gulp): my description")
    myFixture.type("(")
    myFixture.checkResult("build((<caret>gulp): my description")
  }

  fun test_closing_paren_insertion_inside_incomplete_scope_1() {
    prepareFile("build(<caret>")
    myFixture.type(")")
    myFixture.checkResult("build()<caret>")
  }

  fun test_closing_paren_insertion_inside_incomplete_scope_2() {
    prepareFile("build(np<caret>")
    myFixture.type(")")
    myFixture.checkResult("build(np)<caret>")
  }

  fun test_opening_paren_insertion_inside_incomplete_scope_3() {
    prepareFile("build(np<caret> other")
    myFixture.type("(")
    myFixture.checkResult("build(np(<caret> other")
  }

  fun test_closing_paren_insertion_inside_empty_scope() {
    prepareFile("build(<caret>)")
    myFixture.type(")")
    myFixture.checkResult("build()<caret>")
  }

  fun test_closing_paren_insertion_inside_complete_scope() {
    prepareFile("build(np<caret>m)")
    myFixture.type(")")
    myFixture.checkResult("build(np)<caret>m)")
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

  fun test_colon_insertion_before_colon_with_no_space_subject() {
    prepareFile("r(scope)<caret>:my description")
    myFixture.type(":")
    myFixture.checkResult("r(scope):<caret>my description")
  }

  fun test_colon_insertion_before_colon_with_one_space_subject() {
    prepareFile("refactor(scope)<caret>: my description")
    myFixture.type(":")
    myFixture.checkResult("refactor(scope): <caret>my description")
  }

  fun test_colon_insertion_before_colon_with_multiple_spaces_subject() {
    prepareFile("refactor(scope)<caret>:    my description")
    myFixture.type(":")
    myFixture.checkResult("refactor(scope): <caret>   my description")
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

  fun test_colon_insertion_after_type() {
    prepareFile("build<caret>")
    myFixture.type(":")
    myFixture.checkResult("build: <caret>")
  }

  fun test_colon_insertion_in_subject() {
    prepareFile("build: <caret>")
    myFixture.type(":")
    myFixture.checkResult("build: :<caret>")
  }
}
