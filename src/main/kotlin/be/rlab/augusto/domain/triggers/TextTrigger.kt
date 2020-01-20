package be.rlab.augusto.domain.triggers

import be.rlab.augusto.domain.MessageNormalizer
import be.rlab.nlp.TextClassifier
import be.rlab.nlp.model.Language
import be.rlab.tehanu.triggers.Trigger
import be.rlab.tehanu.messages.model.Chat
import be.rlab.tehanu.messages.model.Message
import be.rlab.tehanu.messages.model.TextMessage
import be.rlab.tehanu.messages.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Trigger that evaluates different parts of a message.
 */
class TextTrigger(
    private val textClassifier: TextClassifier,
    override val name: String,
    /** The message must contain any of this elements. */
    private val contains: List<String> = emptyList(),
    /** The message must start with a string. */
    private val startsWith: String?,
    /** The message must end with a string. */
    private val endsWith: String?,
    /** The message must match a regex. */
    private val regex: Regex?,
    /** Distance to match terms in the [contains] field. */
    private val distance: Float = -1.0F,
    /** Ignore case on matching. */
    private val ignoreCase: Boolean,
    /** true to normalize the message text. */
    private val normalize: Boolean
) : Trigger {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TextTrigger::class.java)
    }

    override fun applies(
        chat: Chat,
        user: User?,
        message: Message,
        language: Language
    ): Boolean {
        require(message is TextMessage)
        logger.info("evaluating text matches within the message")

        val text: String = if (normalize) {
            MessageNormalizer(message, language).normalize()
        } else {
            message.text
        }

        val starts: Boolean = startsWith?.let {
            logger.info("message must start with $startsWith (ignore case: $ignoreCase)")
            text.startsWith(startsWith, ignoreCase)
        } ?: true
        val ends: Boolean = endsWith?.let {
            logger.info("message must end with $endsWith (ignore case: $ignoreCase)")
            text.endsWith(endsWith, ignoreCase)
        } ?: true

        logger.info("message must contain any: ${contains.joinToString()} (distance: $distance)")

        val containsAll: Boolean = contains.isEmpty() || contains.any { other ->
            if (distance > 0) {
                textClassifier.distance(text, other) >= distance
            } else {
                text.contains(other, ignoreCase)
            }
        }

        val matches: Boolean = regex?.let {
            logger.info("message must match the regex: $regex")
            regex.matches(text)
        } ?: true

        return matches && starts && ends && containsAll
    }
}
