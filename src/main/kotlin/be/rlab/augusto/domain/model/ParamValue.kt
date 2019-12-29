package be.rlab.augusto.domain.model

data class ParamValue(
    val name: String,
    val values: List<String>,
    val valid: Boolean
) {
    companion object {
        fun valid(
            name: String,
            values: List<String>
        ): ParamValue = ParamValue(
            name = name,
            values = values,
            valid = true
        )

        fun invalid(
            name: String
        ): ParamValue = ParamValue(
            name = name,
            values = emptyList(),
            valid = false
        )
    }

    override fun toString(): String {
        return "$name ${values.joinToString(" ")}"
    }
}
