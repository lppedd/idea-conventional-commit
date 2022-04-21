package com.github.lppedd.cc.psiElement

import com.github.lppedd.cc.api.CommitSubject
import com.intellij.openapi.project.Project

/**
 * @author Edoardo Luppi
 */
internal class CommitSubjectPsiElement(project: Project, val commitSubject: CommitSubject) :
    CommitFakePsiElement(project, commitSubject.text)
