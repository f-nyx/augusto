package be.rlab.augusto.domain

import be.rlab.augusto.domain.model.CommandTrigger
import be.rlab.augusto.domain.model.CommandTrigger.Companion.trigger
import be.rlab.augusto.nlp.Normalizer
import be.rlab.augusto.nlp.StopWordTokenizer
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.MessageContext
import be.rlab.tehanu.domain.model.Message
import be.rlab.tehanu.domain.model.TextMessage

class ConvertNumbers(
    override val name: String
) : NaturalCommand() {

    override val triggers: List<CommandTrigger> = listOf(
        trigger(Language.SPANISH, "convert", 0.9)
    )

    private val indexedParams: List<String> = listOf(
        "roman", "arabig", "comun", "bas", "natural"
    )

    override fun handle(
        context: MessageContext,
        message: Message
    ): MessageContext {
        require(message is TextMessage)

        val tokens = StopWordTokenizer.new(Language.SPANISH)
            .tokenize(Normalizer(message.text, Language.SPANISH).normalize().reader())
            .map { it.toString() }

        val params: List<String> = tokens.fold(emptyList()) { params, token ->
            if (params.size < 2 && indexedParams.contains(token)) {
                params + token
            } else {
                params
            }
        }

        return message.text.toIntOrNull()?.let { arabic ->
            val roman: String = RomanNumeric.arabicToRoman(arabic)
            context.answer(roman)
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
