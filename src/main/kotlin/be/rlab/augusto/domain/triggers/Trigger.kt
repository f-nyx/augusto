package be.rlab.augusto.domain.triggers

import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.model.Chat
import be.rlab.tehanu.domain.model.TextMessage
import be.rlab.tehanu.domain.model.User

/** Must be implemented in order to evaluate if a message can be handled by a
 * the underlying handler.
 */
interface Trigger {

    /** Trigger name used to bind triggers by name. */
    val name: String

    /** Determines whether this trigger applies for a message.
     *
     * @param chat Chat the message comes from.
     * @param user User that sent the message.
     * @param message Message to evaluate.
     * @param language Language to evaluate.
     */
    fun applies(
        chat: Chat,
        user: User?,
        message: TextMessage,
        language: Language
    ): Boolean
}
