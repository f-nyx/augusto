package be.rlab.augusto.domain

import be.rlab.tehanu.domain.MessageContext
import be.rlab.tehanu.domain.MessageListener
import be.rlab.tehanu.domain.model.Chat
import be.rlab.tehanu.domain.model.Message
import be.rlab.tehanu.domain.model.TextMessage
import be.rlab.tehanu.domain.model.User

class CountNumbers(
    override val name: String
) : MessageListener {

    override fun applies(
        chat: Chat,
        user: User?,
        message: Message
    ): Boolean {
        return message is TextMessage
    }

    override fun handle(
        context: MessageContext,
        message: Message
    ): MessageContext {
        require(message is TextMessage)

        return message.text.toIntOrNull()?.let { arabic ->
            val roman: String = RomanNumeric.arabicToRoman(arabic)
            context.answer("$roman")
        } ?: run {
            val arabic: Int = RomanNumeric.romanToDecimal(message.text)
            if (arabic > 0) {
                context.answer("$arabic")
            } else {
                context
            }
        }
    }
}
