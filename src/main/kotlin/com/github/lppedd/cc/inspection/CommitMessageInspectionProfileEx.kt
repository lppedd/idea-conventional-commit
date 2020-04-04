package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.api.INSPECTION_EP
import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.message.CommitMessageInspectionProfile
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.function.Supplier

/**
 * @author Edoardo Luppi
 */
private class CommitMessageInspectionProfileEx(project: Project) : CommitMessageInspectionProfile(project) {
  init {
    try {
      hackInspectionProfile()
    } catch (ignored: Exception) {
      // Ouch, agent Smith caught me and I can't do anything about it :(
    }
  }

  private fun hackInspectionProfile() {
    val myToolSupplierField = InspectionProfileImpl::class.java.getDeclaredField("myToolSupplier")
    myToolSupplierField.isAccessible = true

    val myBaseProfileField = InspectionProfileImpl::class.java.getDeclaredField("myBaseProfile")
    myBaseProfileField.isAccessible = true

    @Suppress("UNCHECKED_CAST")
    val ideaToolSupplier = myToolSupplierField.get(this) as Supplier<List<InspectionToolWrapper<*, *>>>
    val delegatingToolSupplier = CommitInspectionToolSupplier(ideaToolSupplier)
    val inspectionProfile = InspectionProfileImpl(myName, delegatingToolSupplier, null)

    myToolSupplierField.set(this, delegatingToolSupplier)
    myBaseProfileField.set(this, inspectionProfile)

    // From here onwards it might not be needed, but hey, just that we are here...
    val staticDEFAULTField = CommitMessageInspectionProfile::class.java.getDeclaredField("DEFAULT")
    staticDEFAULTField.isAccessible = true

    val modifiersField = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true
    modifiersField.setInt(staticDEFAULTField, staticDEFAULTField.modifiers and Modifier.FINAL.inv())

    staticDEFAULTField.set(null, inspectionProfile)
  }
}

private class CommitInspectionToolSupplier(private val delegate: Supplier<List<InspectionToolWrapper<*, *>>>) :
    Supplier<List<InspectionToolWrapper<*, *>>> {
  override fun get(): List<InspectionToolWrapper<*, *>> {
    val additionalInspections =
      INSPECTION_EP.extensions
        .asSequence()
        .flatMap { it.getInspections().asSequence() }
        .map(::LocalInspectionToolWrapper)
        .toList()

    return delegate.get() + additionalInspections
  }
}
