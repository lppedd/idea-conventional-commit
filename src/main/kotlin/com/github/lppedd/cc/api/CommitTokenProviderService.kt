package com.github.lppedd.cc.api

import com.intellij.openapi.Disposable
import org.jetbrains.annotations.TestOnly

/**
 * @author Edoardo Luppi
 */
interface CommitTokenProviderService {
  /** Returns all the registered commit type providers. */
  fun getTypeProviders(): List<CommitTypeProvider>

  /** Returns all the registered commit scope providers. */
  fun getScopeProviders(): List<CommitScopeProvider>

  /** Returns all the registered commit subject providers. */
  fun getSubjectProviders(): List<CommitSubjectProvider>

  /** Returns all the registered commit body providers. */
  fun getBodyProviders(): List<CommitBodyProvider>

  /** Returns all the registered commit footer's type providers. */
  fun getFooterTypeProviders(): List<CommitFooterTypeProvider>

  /** Returns all the registered commit footer's value providers. */
  fun getFooterValueProviders(): List<CommitFooterValueProvider>

  @TestOnly
  fun registerTypeProvider(typeProvider: CommitTypeProvider, disposable: Disposable): Unit =
    throw UnsupportedOperationException("Please don't override this method")

  @TestOnly
  fun registerScopeProvider(scopeProvider: CommitScopeProvider, disposable: Disposable): Unit =
    throw UnsupportedOperationException("Please don't override this method")

  @TestOnly
  fun registerSubjectProvider(subjectProvider: CommitSubjectProvider, disposable: Disposable): Unit =
    throw UnsupportedOperationException("Please don't override this method")

  @TestOnly
  fun registerBodyProvider(bodyProvider: CommitBodyProvider, disposable: Disposable): Unit =
    throw UnsupportedOperationException("Please don't override this method")

  @TestOnly
  fun registerFooterTypeProvider(footerTypeProvider: CommitFooterTypeProvider, disposable: Disposable): Unit =
    throw UnsupportedOperationException("Please don't override this method")

  @TestOnly
  fun registerFooterValueProvider(footerValueProvider: CommitFooterValueProvider, disposable: Disposable): Unit =
    throw UnsupportedOperationException("Please don't override this method")
}
