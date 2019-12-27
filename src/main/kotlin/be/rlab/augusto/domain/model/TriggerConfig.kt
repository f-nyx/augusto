package be.rlab.augusto.domain.model

data class TriggerConfig(
    /** List of triggers that will be evaluated on each message to determine whether this command must be
     * executed or not.
     */
    val params: List<ParamDefinition>,

    /** Minimum number of params that must be present in a message. */
    val minParams: Int,

    /** Maximum number of params that must be present in a message. */
    val maxParams: Int
) {
    companion object {
        fun default(): TriggerConfig = TriggerConfig(
            params = emptyList(),
            minParams = 0,
            maxParams = 0
        )

        fun config(
            params: List<ParamDefinition> = emptyList(),
            minParams: Int = 0,
            maxParams: Int = 0
        ): TriggerConfig = TriggerConfig(
            params = params,
            minParams = minParams,
            maxParams = maxParams
        )
    }
}
