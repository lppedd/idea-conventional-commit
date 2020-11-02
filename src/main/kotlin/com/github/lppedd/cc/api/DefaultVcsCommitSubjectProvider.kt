package com.github.lppedd.cc.api

import com.github.lppedd.cc.CCIcons
import com.github.lppedd.cc.parser.CCParser
import com.github.lppedd.cc.parser.CommitTokens
import com.github.lppedd.cc.parser.ValidToken
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsConfiguration
import org.jetbrains.annotations.ApiStatus

/**
 * @author Edoardo Luppi
 */
@ApiStatus.Internal
internal class DefaultVcsCommitSubjectProvider(private val project: Project) : CommitSubjectProvider {
  override fun getId(): String = ID

  override fun getPresentation(): ProviderPresentation =
    ProviderPresentation("Default - VCS", CCIcons.Logo)

  override fun getCommitSubjects(commitType: String?, commitScope: String?): Collection<CommitSubject> =
    VcsConfiguration.getInstance(project)
      .recentMessages
      .asReversed()
      .asSequence()
      .take(30)
      .map { it.lines().first(String::isNotBlank) }
      .map(CCParser::parseHeader)
      .map(CommitTokens::subject)
      .filterIsInstance<ValidToken>()
      .map(ValidToken::value)
      .map(String::trim)
      .filter(String::isNotEmpty)
      .map(::CommitSubject)
      .toList()

  companion object {
    const val ID: String = "f3be5600-71b8-401c-bf50-e2465d8efca8"
  }
}
