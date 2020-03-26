package com.github.lppedd.cc.psiElement

import com.github.lppedd.cc.api.CommitScope
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class CommitScopePsiElement(project: Project, val commitScope: CommitScope) :
    CommitFakePsiElement(project)
