package be.rlab.augusto.domain.triggers

import be.rlab.augusto.domain.MessageNormalizer
import be.rlab.augusto.nlp.Normalizer
import be.rlab.augusto.nlp.TextClassifier
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.model.Chat
import be.rlab.tehanu.domain.model.TextMessage
import be.rlab.tehanu.domain.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CategoryTrigger(
    private val textClassifier: TextClassifier,
    override val name: String,
    private val category: String,
    private val score: Double
) : Trigger {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CategoryTrigger::class.java)
    }

    override fun applies(
        chat: Chat,
        user: User?,
        message: TextMessage,
        language: Language
    ): Boolean {
        // TODO(nyx): verify user language.
        val normalizedText: String = MessageNormalizer(message, language).normalize()

        logger.info("searching matching categories for normalized message: $normalizedText")

        return textClassifier.classifyAll(
            text = normalizedText,
            language = language
        ).any { result ->
            val normalizedResult: String = Normalizer.new(
                text = result.assignedClass,
                language = language
            ).normalize()
            val normalizedCategory: String = Normalizer.new(
                text = category,
                language = language
            ).normalize()

            logger.info("evaluating classifier for category '$normalizedCategory'. Classifier: $result")

            result.score >= score && normalizedResult == normalizedCategory
        }
    }
}
