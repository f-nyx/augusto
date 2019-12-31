package be.rlab.augusto.command

import be.rlab.augusto.domain.MessageSource
import be.rlab.augusto.domain.TriggerCommand
import be.rlab.augusto.domain.triggers.Bind
import be.rlab.augusto.domain.triggers.Trigger
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.MessageContext
import be.rlab.tehanu.domain.model.TextMessage

class Cancel(
    override val name: String,
    override val language: Language,
    override val triggers: List<Trigger>,
    override val messages: MessageSource
) : TriggerCommand() {

    companion object {
        const val OK: String = "ok"
    }

    @Bind("cancel")
    fun cancel(
        context: MessageContext,
        message: TextMessage
    ): MessageContext {
        return context.answer(messages[OK])
    }
}
