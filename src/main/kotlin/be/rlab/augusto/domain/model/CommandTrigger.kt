package be.rlab.augusto.domain.model

import be.rlab.augusto.nlp.Normalizer
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.MessageContext
import be.rlab.tehanu.domain.model.TextMessage

data class CommandTrigger(
    val language: Language,
    val category: String,
    val score: Double,
    val handler: (MessageContext, TextMessage, List<ParamValue>) -> MessageContext,
    val config: TriggerConfig
) {
    companion object {
        fun trigger(
            language: Language,
            category: String,
            score: Double = 0.7,
            config: TriggerConfig = TriggerConfig.default(),
            handler: (MessageContext, TextMessage, List<ParamValue>) -> MessageContext
        ): CommandTrigger = CommandTrigger(
            language = language,
            category = Normalizer(category, language).normalize(),
            score = score,
            config = config,
            handler = handler
        )
    }
}
