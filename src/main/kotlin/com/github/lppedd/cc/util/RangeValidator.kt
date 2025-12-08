package com.github.lppedd.cc.util

import com.github.lppedd.cc.component1
import com.github.lppedd.cc.component2
import com.intellij.openapi.util.TextRange

/**
 * @author Edoardo Luppi
 */
internal class RangeValidator(private val start: Int, private val end: Int) {
  private val ranges = sortedSetOf(MyTextRange(start, end))

  fun isValid(): Boolean =
    ranges.isEmpty()

  fun invalidRanges(): Set<TextRange> =
    ranges.toSet()

  fun markValid(start: Int, end: Int) {
    markValid(TextRange(start, end))
  }

  private fun markValid(range: TextRange) {
    checkRange(range)

    if (range.isEmpty) {
      return
    }

    for (r in ranges) {
      val (s, e) = r.intersection(range) ?: continue
      ranges.remove(r)

      if (r.startOffset < s) {
        ranges += MyTextRange(r.startOffset, s)
      }

      if (e < r.endOffset) {
        ranges += MyTextRange(e, r.endOffset)
      }
    }
  }

  private fun checkRange(range: TextRange) {
    require(range.startOffset <= range.endOffset) {
      "Range start value must be smaller or equal to the end value"
    }

    require(range.startOffset >= start && range.endOffset <= end) {
      "Range $range out of bounds"
    }
  }

  private class MyTextRange : TextRange, Comparable<MyTextRange> {
    constructor(startOffset: Int, endOffset: Int) : super(startOffset, endOffset)

    override fun compareTo(other: MyTextRange): Int {
      if (other.contains(this)) {
        return 0
      }

      val r = startOffset - other.startOffset
      return if (r == 0) length - other.length else r
    }
  }
}
