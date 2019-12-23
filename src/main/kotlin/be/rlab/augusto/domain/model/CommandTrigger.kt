package be.rlab.augusto.domain.model

import be.rlab.augusto.nlp.Normalizer
import be.rlab.augusto.nlp.model.Language

data class CommandTrigger(
    val language: Language,
    val category: String,
    val score: Double
) {
    companion object {
        fun trigger(
            language: Language,
            category: String,
            score: Double = 0.7
        ): CommandTrigger = CommandTrigger(
            language = language,
            category = Normalizer(category, language).normalize(),
            score = score
        )
    }
}
