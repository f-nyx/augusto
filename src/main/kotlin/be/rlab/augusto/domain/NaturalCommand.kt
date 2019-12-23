package be.rlab.augusto.domain

import be.rlab.augusto.domain.model.CommandTrigger
import be.rlab.augusto.nlp.Normalizer
import be.rlab.augusto.nlp.TextClassifier
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.MessageListener
import be.rlab.tehanu.domain.model.Chat
import be.rlab.tehanu.domain.model.Message
import be.rlab.tehanu.domain.model.TextMessage
import be.rlab.tehanu.domain.model.User

/** Must be extended by bot commands in order to support triggering with natural language.
 * It uses a [TextClassifier] that must be previously trained.
 *
 * The command must define a list of [triggers] that indicate the categories and minimum thresholds that a
 * message should match in order to execute the command.
 *
 * This command only supports text messages.
 */
abstract class NaturalCommand : MessageListener {

    /** Text classifier, injected by the application context.
     */
    lateinit var textClassifier: TextClassifier

    /** List of triggers that will be evaluated on each message to determine whether this command must be
     * executed or not.
     */
    protected abstract val triggers: List<CommandTrigger>

    /** Determines whether any trigger matches any of the message resolved categories.
     * It returns true if any trigger matches a resolved category and the category score is equal
     * to or greater than the trigger minimum score.
     */
    override fun applies(
        chat: Chat,
        user: User?,
        message: Message
    ): Boolean {
        return message is TextMessage && let {
            // TODO(nyx): use user language.
            textClassifier.classifyAll(
                text = message.text,
                language = Language.SPANISH
            ).any { category ->
                triggers.any { trigger ->
                    category.score >= trigger.score &&
                    Normalizer.new(
                        text = category.assignedClass,
                        language = Language.SPANISH
                    ).normalize() == trigger.category
                }
            }
        }
    }
}
