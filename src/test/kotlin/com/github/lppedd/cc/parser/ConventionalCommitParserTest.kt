package com.github.lppedd.cc.parser

import org.junit.Assert.*
import org.junit.Test
import kotlin.contracts.contract

class ConventionalCommitParserTest {
  @Test
  fun `fail incomplete`() {
    var parse = parseConventionalCommit("fix: ")
    assertIs<ParseResult.Error>(parse)
    assertEquals("The commit subject is missing or invalid", parse.message)

    parse = parseConventionalCommit("build(np:  ")
    assertIs<ParseResult.Error>(parse)
    assertEquals("The commit scope is missing the closing parenthesis", parse.message)

    parse = parseConventionalCommit("build(npm)")
    assertIs<ParseResult.Error>(parse)
    assertEquals("The ':' separator is missing after the type/scope", parse.message)
  }

  @Test
  fun `parse incomplete`() {
    var parse = parseConventionalCommit("fix:  ", lenient = true)
    assertIs<ParseResult.Success>(parse)

    var message = parse.message
    assertEquals("fix", message.type)
    assertEquals("  ", message.subject)

    parse = parseConventionalCommit("build(np:  ", lenient = true)
    assertIs<ParseResult.Success>(parse)

    message = parse.message
    assertEquals("build", message.type)
    assertEquals("np:  ", message.scope)
    assertEquals("", message.subject)

    parse = parseConventionalCommit("build(npm)", lenient = true)
    assertIs<ParseResult.Success>(parse)

    message = parse.message
    assertEquals("build", message.type)
    assertEquals("npm", message.scope)
    assertEquals("", message.subject)
  }

  @Test
  fun `parse one liner`() {
    val parse = parseConventionalCommit("fix: foo")
    assertIs<ParseResult.Success>(parse)

    val message = parse.message
    assertEquals("fix", message.type)
    assertEquals("foo", message.subject.trim())
  }

  @Test
  fun `parse empty scope`() {
    var parse = parseConventionalCommit("build(): bar")
    assertIs<ParseResult.Success>(parse)

    var message = parse.message
    assertEquals("build", message.type)
    assertEquals("", message.scope)
    assertEquals("bar", message.subject.trim())

    parse = parseConventionalCommit("build(   ): bar")
    assertIs<ParseResult.Success>(parse)

    message = parse.message
    assertEquals("build", message.type)
    assertEquals("   ", message.scope)
    assertEquals("bar", message.subject.trim())
  }

  @Test
  fun `parse scope with breaking change`() {
    val parse = parseConventionalCommit("build(foo)!: bar")
    assertIs<ParseResult.Success>(parse)

    val message = parse.message
    assertEquals("build", message.type)
    assertEquals("foo", message.scope)
    assertEquals("bar", message.subject.trim())
    assertTrue(message.isBreakingChange)
  }

  @Test
  fun `parse body`() {
    val parse = parseConventionalCommit(
      """
      |refactor!: foo
      |
      |bar is not foo 1
      |  bar is not foo 2
      """.trimMargin()
    )

    assertIs<ParseResult.Success>(parse)

    val message = parse.message
    assertEquals("refactor", message.type)
    assertEquals("foo", message.subject.trim())
    assertEquals("bar is not foo 1\n  bar is not foo 2", message.body)
  }

  @Test
  fun `parse body and footers`() {
    val parse = parseConventionalCommit(
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

    assertIs<ParseResult.Success>(parse)

    val message = parse.message
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
    val parse = parseConventionalCommit(
      """
      |refactor(scope): foo
      |
      |Closes: example 1
      |Fixes   #12
      |Refs: example 2
      """.trimMargin()
    )

    assertIs<ParseResult.Success>(parse)

    val message = parse.message
    assertEquals("refactor", message.type)
    assertEquals("scope", message.scope)
    assertEquals("foo", message.subject.trim())
    assertEquals(3, message.footers.size)

    val (type1, value1) = message.footers[0]
    assertEquals("Closes", type1)
    assertEquals("example 1", value1.trim())

    val (type2, value2) = message.footers[1]
    assertEquals("Fixes", type2)
    assertEquals("#12", value2.trim())

    val (type3, value3) = message.footers[2]
    assertEquals("Refs", type3)
    assertEquals("example 2", value3.trim())

    assertFalse(message.isBreakingChange)
  }

  @Test
  fun `parse BREAKING CHANGE footer`() {
    val parse = parseConventionalCommit(
      """
      |build(npm): switch to yarn
      |
      |BREAKING CHANGE: why?
      |BREAKING-CHANGE: yes!
      """.trimMargin()
    )

    assertIs<ParseResult.Success>(parse)

    val message = parse.message
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
    val parse = parseConventionalCommit(
      """
      |build(npm): switch to yarn
      |
      |Co-authored by: this may be a long
      | multiline footer value that spans
      | multiple lines.
      """.trimMargin()
    )

    assertIs<ParseResult.Success>(parse)

    val message = parse.message
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
    val parse = parseConventionalCommit(
      """
      |build: switch to yarn
      |
      |Closes:
      """.trimMargin(),
      lenient = true,
    )

    assertIs<ParseResult.Success>(parse)

    val message = parse.message
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
