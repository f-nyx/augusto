package be.rlab.augusto.command

import be.rlab.tehanu.annotations.Handler
import be.rlab.tehanu.annotations.MessageListener
import be.rlab.tehanu.messages.MessageContext

@MessageListener("/cancel")
class Cancel {

    companion object {
        const val OK: String = "ok"
    }

    @Handler
    fun cancel(
        context: MessageContext
    ): MessageContext = with(context) {
        return answer(messages[OK])
    }
}
