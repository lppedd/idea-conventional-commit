package com.github.lppedd.cc.psiElement

import com.github.lppedd.cc.api.CommitFooterValue
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class CommitFooterValuePsiElement(project: Project, val commitFooterValue: CommitFooterValue) :
    CommitFakePsiElement(project, commitFooterValue.text)
