package be.rlab.augusto.domain.model

data class ParamDefinition(
    val name: String,
    val args: Int,
    val required: Boolean
) {
    companion object {
        fun param(
            name: String,
            args: Int = 0,
            required: Boolean = false
        ): ParamDefinition = ParamDefinition(
            name = name,
            args = args,
            required = required
        )
    }
}
