package com.github.lppedd.cc.inspection

import com.github.lppedd.cc.api.INSPECTION_EP
import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.message.CommitMessageInspectionProfile
import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import java.lang.reflect.Field
import java.lang.reflect.Method
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

  /**
   * This trick allows us to have additional inspections which are enabled by default,
   * in addition to being able to contribute to the commit's dialog standard inspections
   * (which doesn't required all of this).
   */
  private fun hackInspectionProfile() {
    val inspectionProfileImplClazz = InspectionProfileImpl::class.java

    val myToolSupplierField = inspectionProfileImplClazz.getDeclaredField("myToolSupplier")
    myToolSupplierField.isAccessible = true

    val myBaseProfileField = inspectionProfileImplClazz.getDeclaredField("myBaseProfile")
    myBaseProfileField.isAccessible = true

    // IDEA -193.3519 > Supplier<List<InspectionToolWrapper>>#get
    // IDEA 193.3519+ > InspectionToolsSupplier#createTools
    // (remember to set this classLoader after setting the superclass, or it will throw)
    val ideaToolSupplier = myToolSupplierField.get(this)
    val inspectionProfileImpl: Any
    val proxyToolSupplier: Any

    if (ideaToolSupplier is Supplier<*>) {
      @Suppress("UNCHECKED_CAST")
      val delegate = ideaToolSupplier as Supplier<List<InspectionToolWrapper<*, *>>>
      proxyToolSupplier = CommitInspectionToolSupplier(delegate)
      inspectionProfileImpl = InspectionProfileImpl(myName, proxyToolSupplier, null)
    } else {
      val inspectionToolSupplierClazz = Class.forName(
        "com.intellij.codeInspection.ex.InspectionToolsSupplier",
        true,
        javaClass.classLoader
      )

      val enhancer = Enhancer()
      enhancer.useCache = false
      enhancer.setSuperclass(inspectionToolSupplierClazz)
      enhancer.setCallback(InspectionToolSupplierInterceptor(ideaToolSupplier))
      enhancer.classLoader = javaClass.classLoader

      proxyToolSupplier = enhancer.create()
      inspectionProfileImpl = inspectionProfileImplClazz
        .getConstructor(String::class.java, inspectionToolSupplierClazz, inspectionProfileImplClazz)
        .newInstance(myName, proxyToolSupplier, null)
    }

    myToolSupplierField.set(this, proxyToolSupplier)
    myBaseProfileField.set(this, inspectionProfileImpl)

    // From here onwards it might not be needed, but hey, just that we are here...
    val staticDEFAULTField = CommitMessageInspectionProfile::class.java.getDeclaredField("DEFAULT")
    staticDEFAULTField.isAccessible = true

    val modifiersField = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true
    modifiersField.setInt(staticDEFAULTField, staticDEFAULTField.modifiers and Modifier.FINAL.inv())

    staticDEFAULTField.set(null, inspectionProfileImpl)
  }
}

/** Provides support for IDEA 2019.2 only. */
private class CommitInspectionToolSupplier(
    private val delegate: Supplier<List<InspectionToolWrapper<*, *>>>,
) : Supplier<List<InspectionToolWrapper<*, *>>> {
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

/**
 * Provides support for IDEA 2019.3 onward.
 * Using CGLIB (or other bytecode libraries) is required as
 * we don't have access to new releases classes.
 */
private class InspectionToolSupplierInterceptor(private val delegate: Any) : MethodInterceptor {
  override fun intercept(obj: Any?, method: Method, args: Array<Any?>?, proxy: MethodProxy): Any? =
    if ("createTools" == method.name) {
      val additionalInspections =
        INSPECTION_EP.extensions
          .asSequence()
          .flatMap { it.getInspections().asSequence() }
          .map(::LocalInspectionToolWrapper)
          .toList()

      @Suppress("UNCHECKED_CAST")
      val originalInspections = proxy.invoke(delegate, args) as List<InspectionToolWrapper<*, *>>
      originalInspections + additionalInspections
    } else {
      proxy.invoke(delegate, args)
    }
}
