package be.rlab.augusto.domain

import be.rlab.augusto.domain.model.CommandTrigger
import be.rlab.augusto.domain.model.CommandTrigger.Companion.trigger
import be.rlab.augusto.domain.model.TriggerConfig.Companion.config
import be.rlab.augusto.domain.model.ParamDefinition.Companion.param
import be.rlab.augusto.domain.model.ParamValue
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.MessageContext
import be.rlab.tehanu.domain.model.TextMessage

class ConvertNumbers(
    override val name: String
) : NaturalCommand() {

    override val triggers: List<CommandTrigger> = listOf(
        trigger(
            category = "convert",
            language = Language.SPANISH,
            score = 0.8,
            config = config(
                params = listOf(
                    param("romanos"),
                    param("arábigos"),
                    param("naturales"),
                    param("comunes"),
                    param("base", 1)
                ),
                minParams = 2,
                maxParams = 2
            ),
            handler = this::convert
        ),
        trigger(
            category = "help",
            language = Language.SPANISH,
            score = 0.8,
            handler = this::help
        )
    )

    private fun convert(
        context: MessageContext,
        message: TextMessage,
        params: List<ParamValue>
    ): MessageContext {
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

    private fun help(
        context: MessageContext,
        message: TextMessage,
        params: List<ParamValue>
    ): MessageContext {
        return context.talk("""
            por ahora solamente puedo convertir de números romanos a naturales y viceversa
        """.trimIndent())
    }
}
