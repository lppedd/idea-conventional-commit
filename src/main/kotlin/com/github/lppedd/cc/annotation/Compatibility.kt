package com.github.lppedd.cc.annotation

/**
 * @author Edoardo Luppi
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.EXPRESSION,
    AnnotationTarget.LOCAL_VARIABLE,
)
@Retention(AnnotationRetention.SOURCE)
internal annotation class Compatibility(
    /**
     * The minimum supported IDE version that is required
     * for removing the annotated element.
     */
    val minVersion: String = "?",

    /**
     * A brief explanation of why this behavior is needed
     * to maintain compatibility.
     */
    val description: String = "",

    /**
     * A suggestion for replacing the annotated element
     * when it's time to remove it.
     */
    val replaceWith: String = "",
)
