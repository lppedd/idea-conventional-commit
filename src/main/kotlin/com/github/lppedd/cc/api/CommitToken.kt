package com.github.lppedd.cc.api

/**
 * Represents a Conventional Commit's commit token, e.g. a type, scope, subject, and so on.
 *
 * @author Edoardo Luppi
 */
interface CommitToken {
  /** Returns the token's presentable text, to be shown in the completion's popup. */
  fun getText(): String

  /**
   * Returns the token's actual value, to be applied to the commit message
   * when the token is selected by the user.
   *
   * The presentable text and the actual underlying value might be the same.
   */
  fun getValue(): String

  /** Returns the token's description, to be shown in the Quick Documentation popup. */
  fun getDescription(): String

  /**
   * Returns the token's UI presentation options, which will impact how the token
   * is presented in the completion's popup.
   */
  fun getPresentation(): TokenPresentation
}
