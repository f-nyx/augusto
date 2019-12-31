package be.rlab.augusto.domain.triggers

@Target(AnnotationTarget.FUNCTION)
annotation class Bind(vararg val triggers: String)
