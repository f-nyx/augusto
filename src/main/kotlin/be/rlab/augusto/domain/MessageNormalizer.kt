package be.rlab.augusto.domain

import be.rlab.nlp.Normalizer
import be.rlab.nlp.model.Language
import be.rlab.tehanu.messages.model.EntityType
import be.rlab.tehanu.messages.model.TextMessage

/** Normalizes a [TextMessage].
 */
data class MessageNormalizer(
    private val message: TextMessage,
    private val language: Language,
    private val removeStopWords: Boolean = true,
    private val stripEntities: Boolean = true
) {

    fun normalize(): String {
        val cleanText: String = if (stripEntities) {
            message.entities.filter { entity ->
                entity.type != EntityType.RICH_TEXT
            }.fold(message.text) { cleanText, entity ->
                cleanText.replace(entity.value, "")
            }
        } else {
            message.text
        }

        return Normalizer(cleanText, language)
            .applyStemming()
            .removeStopWords()
            .normalize()
    }
}