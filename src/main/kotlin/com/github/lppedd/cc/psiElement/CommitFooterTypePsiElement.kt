package com.github.lppedd.cc.psiElement

import com.github.lppedd.cc.api.CommitFooterType
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class CommitFooterTypePsiElement(project: Project, val commitFooterType: CommitFooterType) :
    CommitFakePsiElement(project, commitFooterType.text)
