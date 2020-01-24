package be.rlab.augusto.domain.triggers

import be.rlab.augusto.domain.MessageNormalizer
import be.rlab.nlp.Normalizer
import be.rlab.nlp.TextClassifier
import be.rlab.nlp.model.Language
import be.rlab.tehanu.messages.Trigger
import be.rlab.tehanu.messages.model.Chat
import be.rlab.tehanu.messages.model.Message
import be.rlab.tehanu.messages.model.TextMessage
import be.rlab.tehanu.messages.model.User
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
        const val DEFAULT_CATEGORY = "::default"
    }

    override fun applies(
        chat: Chat,
        user: User?,
        message: Message,
        language: Language
    ): Boolean {
        require(message is TextMessage)
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
