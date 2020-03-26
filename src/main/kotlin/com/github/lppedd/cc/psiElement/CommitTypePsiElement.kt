package com.github.lppedd.cc.psiElement

import com.github.lppedd.cc.api.CommitType
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class CommitTypePsiElement(project: Project, val commitType: CommitType) :
    CommitFakePsiElement(project)
