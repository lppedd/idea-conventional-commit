package com.github.lppedd.cc.psiElement

import com.github.lppedd.cc.api.CommitBody
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class CommitBodyPsiElement(project: Project, val commitBody: CommitBody) :
    CommitFakePsiElement(project)
