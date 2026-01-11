package com.github.lppedd.cc.api

/**
 * Represents a Conventional Commit's commit token (e.g., type, scope, subject, etc.).
 *
 * @author Edoardo Luppi
 */
public interface CommitToken {
  /**
   * Returns the actual token value, to be applied to the commit message when the token
   * is selected by the user.
   */
  public fun getValue(): String

  /**
   * Returns the token presentable text, to be shown in the completion popup item.
   *
   * The presentable text and the actual underlying [value][getValue] might be the same.
   */
  public fun getText(): String

  /**
   * Returns the optional token description, to be shown in the Quick Documentation popup.
   */
  public fun getDescription(): String? = null

  /**
   * Returns the optional token UI presentation options, which will impact how the token
   * is presented in the completion popup.
   *
   * If no presentation is returned, a default one is used instead.
   */
  public fun getPresentation(): TokenPresentation? = null
}
