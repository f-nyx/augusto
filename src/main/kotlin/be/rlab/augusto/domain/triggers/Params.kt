package be.rlab.augusto.domain.triggers

@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
annotation class Params(
    val name: String,
    val minParams: Int = 1,
    val maxParams: Int = 1
)
