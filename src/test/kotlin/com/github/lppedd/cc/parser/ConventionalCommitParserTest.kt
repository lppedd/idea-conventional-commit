package com.github.lppedd.cc.parser

import org.junit.Assert.*
import org.junit.Test
import kotlin.contracts.contract

class ConventionalCommitParserTest {
  @Test
  fun `fail incomplete`() {
    var result = parseConventionalCommit("fix: ")
    assertIs<ParseResult.Error>(result)
    assertEquals("The commit subject is missing or invalid", result.message)

    result = parseConventionalCommit("build(np:  ")
    assertIs<ParseResult.Error>(result)
    assertEquals("The commit scope is missing the closing parenthesis", result.message)

    result = parseConventionalCommit("build(npm)")
    assertIs<ParseResult.Error>(result)
    assertEquals("The ':' separator is missing after the type/scope", result.message)
  }

  @Test
  fun `parse incomplete`() {
    var result = parseConventionalCommit("fix:  ", lenient = true)
    assertIs<ParseResult.Success>(result)

    var message = result.message
    assertEquals("fix", message.type)
    assertEquals("  ", message.subject)

    result = parseConventionalCommit("build(np:  ", lenient = true)
    assertIs<ParseResult.Success>(result)

    message = result.message
    assertEquals("build", message.type)
    assertEquals("np:  ", message.scope)
    assertEquals("", message.subject)

    result = parseConventionalCommit("build(npm)", lenient = true)
    assertIs<ParseResult.Success>(result)

    message = result.message
    assertEquals("build", message.type)
    assertEquals("npm", message.scope)
    assertEquals("", message.subject)
  }

  @Test
  fun `parse one liner`() {
    val result = parseConventionalCommit("fix: foo")
    assertIs<ParseResult.Success>(result)

    val message = result.message
    assertEquals("fix", message.type)
    assertEquals("foo", message.subject.trim())
  }

  @Test
  fun `parse empty scope`() {
    var result = parseConventionalCommit("build(): bar")
    assertIs<ParseResult.Success>(result)

    var message = result.message
    assertEquals("build", message.type)
    assertEquals("", message.scope)
    assertEquals("bar", message.subject.trim())

    result = parseConventionalCommit("build(   ): bar")
    assertIs<ParseResult.Success>(result)

    message = result.message
    assertEquals("build", message.type)
    assertEquals("   ", message.scope)
    assertEquals("bar", message.subject.trim())
  }

  @Test
  fun `parse scope with breaking change`() {
    val result = parseConventionalCommit("build(foo)!: bar")
    assertIs<ParseResult.Success>(result)

    val message = result.message
    assertEquals("build", message.type)
    assertEquals("foo", message.scope)
    assertEquals("bar", message.subject.trim())
    assertTrue(message.isBreakingChange)
  }

  @Test
  fun `parse body`() {
    val result = parseConventionalCommit(
      """
      |refactor!: foo
      |
      |bar is not foo 1
      |  bar is not foo 2
      """.trimMargin()
    )

    assertIs<ParseResult.Success>(result)

    val message = result.message
    assertEquals("refactor", message.type)
    assertEquals("foo", message.subject.trim())
    assertEquals("bar is not foo 1\n  bar is not foo 2", message.body)
  }

  @Test
  fun `parse body and footers`() {
    val result = parseConventionalCommit(
      """
      |refactor!: foo
      |
      |bar is not foo 1
      |bar is not foo 2
      |
      |Refs: example
      |BRAKING-CHANGE: yes
      """.trimMargin()
    )

    assertIs<ParseResult.Success>(result)

    val message = result.message
    assertEquals("refactor", message.type)
    assertEquals("foo", message.subject.trim())
    assertEquals("bar is not foo 1\nbar is not foo 2", message.body)
    assertEquals(2, message.footers.size)

    val (type1, value1) = message.footers[0]
    assertEquals("Refs", type1)
    assertEquals("example", value1.trim())

    val (type2, value2) = message.footers[1]
    assertEquals("BRAKING-CHANGE", type2)
    assertEquals("yes", value2.trim())

    assertTrue(message.isBreakingChange)
  }

  @Test
  fun `parse footers`() {
    val result = parseConventionalCommit(
      """
      |refactor(scope): foo
      |
      |Closes: example 1
      |Fixes   #12
      |[Author]   
      |Refs: example 2
      """.trimMargin()
    )

    assertIs<ParseResult.Success>(result)

    val message = result.message
    assertEquals("refactor", message.type)
    assertEquals("scope", message.scope)
    assertEquals("foo", message.subject.trim())
    assertEquals(4, message.footers.size)

    val (type1, value1) = message.footers[0]
    assertEquals("Closes", type1)
    assertEquals("example 1", value1.trim())

    val (type2, value2) = message.footers[1]
    assertEquals("Fixes", type2)
    assertEquals("#12", value2.trim())

    val (type3, value3) = message.footers[2]
    assertEquals("[Author]   ", type3)
    assertEquals("", value3)

    val (type4, value4) = message.footers[3]
    assertEquals("Refs", type4)
    assertEquals("example 2", value4.trim())

    assertFalse(message.isBreakingChange)
  }

  @Test
  fun `parse BREAKING CHANGE footer`() {
    val result = parseConventionalCommit(
      """
      |build(npm): switch to yarn
      |
      |BREAKING CHANGE: why?
      |BREAKING-CHANGE: yes!
      """.trimMargin()
    )

    assertIs<ParseResult.Success>(result)

    val message = result.message
    assertEquals("build", message.type)
    assertEquals("npm", message.scope)
    assertEquals("switch to yarn", message.subject.trim())
    assertEquals(2, message.footers.size)

    val (type1, value1) = message.footers[0]
    assertEquals("BREAKING CHANGE", type1)
    assertEquals("why?", value1.trim())

    val (type2, value2) = message.footers[1]
    assertEquals("BREAKING-CHANGE", type2)
    assertEquals("yes!", value2.trim())
  }

  @Test
  fun `parse multiline footer value`() {
    val result = parseConventionalCommit(
      """
      |build(npm): switch to yarn
      |
      |Co-authored by: this may be a long
      | multiline footer value that spans
      | multiple lines.
      """.trimMargin()
    )

    assertIs<ParseResult.Success>(result)

    val message = result.message
    assertEquals("build", message.type)
    assertEquals("npm", message.scope)
    assertEquals("switch to yarn", message.subject.trim())
    assertEquals(1, message.footers.size)

    val (type, value) = message.footers.first()
    assertEquals("Co-authored by", type)
    assertEquals(
      """
      | this may be a long
      | multiline footer value that spans
      | multiple lines.
      """.trimMargin(),
      value,
    )
  }

  @Test
  fun `parse missing footer value`() {
    val result = parseConventionalCommit(
      """
      |build: switch to yarn
      |
      |Closes:
      """.trimMargin(),
      lenient = true,
    )

    assertIs<ParseResult.Success>(result)

    val message = result.message
    assertEquals("build", message.type)
    assertEquals("switch to yarn", message.subject.trim())
    assertEquals(1, message.footers.size)

    val (type, value) = message.footers.first()
    assertEquals("Closes", type)
    assertEquals("", value)
  }

  private inline fun <reified T> assertIs(value: Any?): Boolean {
    contract {
      returns() implies (value is T)
    }

    if (value !is T) {
      fail("Expected a value of type ${T::class.java.name}")
    }

    return true
  }
}
