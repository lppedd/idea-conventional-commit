package com.github.lppedd.cc.api

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.TestOnly

/**
 * @author Edoardo Luppi
 */
public interface CommitTokenProviderService {
  public companion object {
    @JvmStatic
    public fun getInstance(project: Project): CommitTokenProviderService = project.service()
  }

  /**
   * Returns all the registered commit type providers.
   */
  public fun getTypeProviders(): List<CommitTypeProvider>

  /**
   * Returns all the registered commit scope providers.
   */
  public fun getScopeProviders(): List<CommitScopeProvider>

  /**
   * Returns all the registered commit subject providers.
   */
  public fun getSubjectProviders(): List<CommitSubjectProvider>

  /**
   * Returns all the registered commit body providers.
   */
  public fun getBodyProviders(): List<CommitBodyProvider>

  /**
   *  Returns all the registered commit footer's type providers.
   **/
  public fun getFooterTypeProviders(): List<CommitFooterTypeProvider>

  /**
   * Returns all the registered commit footer's value providers.
   */
  public fun getFooterValueProviders(): List<CommitFooterValueProvider>

  @TestOnly
  public fun registerTypeProvider(typeProvider: CommitTypeProvider, disposable: Disposable): Unit =
    throw UnsupportedOperationException("Please do not override this method")

  @TestOnly
  public fun registerScopeProvider(scopeProvider: CommitScopeProvider, disposable: Disposable): Unit =
    throw UnsupportedOperationException("Please do not override this method")

  @TestOnly
  public fun registerSubjectProvider(subjectProvider: CommitSubjectProvider, disposable: Disposable): Unit =
    throw UnsupportedOperationException("Please do not override this method")

  @TestOnly
  public fun registerBodyProvider(bodyProvider: CommitBodyProvider, disposable: Disposable): Unit =
    throw UnsupportedOperationException("Please do not override this method")

  @TestOnly
  public fun registerFooterTypeProvider(footerTypeProvider: CommitFooterTypeProvider, disposable: Disposable): Unit =
    throw UnsupportedOperationException("Please do not override this method")

  @TestOnly
  public fun registerFooterValueProvider(footerValueProvider: CommitFooterValueProvider, disposable: Disposable): Unit =
    throw UnsupportedOperationException("Please do not override this method")
}
