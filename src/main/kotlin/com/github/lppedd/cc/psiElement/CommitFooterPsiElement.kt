package com.github.lppedd.cc.psiElement

import com.github.lppedd.cc.api.CommitFooter
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class CommitFooterPsiElement(project: Project, val commitFooter: CommitFooter) :
    CommitFakePsiElement(project)
