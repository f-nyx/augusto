package be.rlab.augusto.domain

import be.rlab.tehanu.view.UserInput
import kotlin.reflect.KProperty

fun UserInput.param(): Any {
    return object {
        operator fun getValue(thisRef: UserInput?, property: KProperty<*>): String {
            return ""
        }

        operator fun setValue(thisRef: UserInput?, property: KProperty<*>, value: String) {
        }
    }
}