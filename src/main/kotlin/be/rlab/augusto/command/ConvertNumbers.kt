package be.rlab.augusto.command

import be.rlab.augusto.domain.MessageNormalizer
import be.rlab.augusto.domain.MessageSource
import be.rlab.augusto.domain.TriggerCommand
import be.rlab.augusto.domain.converters.DecimalNumberConverter
import be.rlab.augusto.domain.converters.NumberConverter
import be.rlab.augusto.domain.converters.RomanNumericConverter
import be.rlab.augusto.domain.model.ParamDefinition
import be.rlab.augusto.domain.model.ParamDefinition.Companion.param
import be.rlab.augusto.domain.model.ParamValue
import be.rlab.augusto.domain.triggers.Bind
import be.rlab.augusto.domain.triggers.Params
import be.rlab.augusto.domain.triggers.Trigger
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.MessageContext
import be.rlab.tehanu.domain.model.TextMessage

class ConvertNumbers(
    override val name: String,
    override val language: Language,
    override val triggers: List<Trigger>,
    override val messages: MessageSource
) : TriggerCommand() {

    companion object {
        const val NUM_PARAMS: Int = 2
        const val CONVERT_FROM: String = "{convert-from}"
        const val CONVERT_TO: String = "{convert-to}"
        const val CONVERT_ANSWERS: String = "convert-answers"
        const val ACCEPT_ANSWERS: String = "accept-answers"
        const val UNIT_ANSWERS: String = "unit-answers"
        const val UNKNOWN_ANSWERS: String = "unknown-answers"
        const val HELP: String = "help"
    }

    private val converters: Map<String, NumberConverter> = mapOf(
        "romanos" to RomanNumericConverter(),
        "decimales" to DecimalNumberConverter(),
        "arábigos" to DecimalNumberConverter(),
        "naturales" to DecimalNumberConverter(),
        "comunes" to DecimalNumberConverter()
    )

    @Params("convert-params", minParams = 2, maxParams = 2)
    private val convertParams: List<ParamDefinition> = listOf(
        param("romanos"),
        param("decimales"),
        param("arábigos"),
        param("naturales"),
        param("comunes"),
        param("base", 1)
    )

    @Bind("convert")
    @Params("convert-params")
    fun convertFromUnits(
        context: MessageContext,
        message: TextMessage,
        params: List<ParamValue>
    ): MessageContext = withConverters(context, params) { sourceConverter, targetConverter ->
        context.answer(messages[ACCEPT_ANSWERS])
        context.userInput {
            val value: String by field(messages[CONVERT_ANSWERS])

            onSubmit {
                val decimal: Int = sourceConverter.toDecimal(value)
                val target: String = targetConverter.fromDecimal(decimal)
                context.talk(target)
            }
        }
    }

    @Bind("mention", "numbers")
    fun convertFromNumber(
        context: MessageContext,
        message: TextMessage
    ): MessageContext = context.userInput {
        val values: List<ParamValue> by params(messages[UNIT_ANSWERS], convertParams, minParams = NUM_PARAMS)

        onSubmit {
            withConverters(context, values) { sourceConverter, targetConverter ->
                val decimal: Int = sourceConverter.toDecimal(
                    MessageNormalizer(message, language).normalize()
                )
                val target: String = targetConverter.fromDecimal(decimal)
                context.talk(target)
            }
        }
    }

    @Bind("help")
    fun help(
        context: MessageContext,
        message: TextMessage
    ): MessageContext {
        return context.talk(messages[HELP])
    }

    private fun withConverters(
        context: MessageContext,
        params: List<ParamValue>,
        callback: (NumberConverter, NumberConverter) -> MessageContext
    ): MessageContext {
        val sourceConverter: NumberConverter? = converters[params[0].name]
        val targetConverter: NumberConverter? = converters[params[1].name]

        return when {
            sourceConverter == null ->
                context.answer(messages.get(UNKNOWN_ANSWERS, CONVERT_FROM, params[0]))

            targetConverter == null ->
                context.answer(messages.get(UNKNOWN_ANSWERS, CONVERT_TO, params[1]))

            else ->
                callback(sourceConverter, targetConverter)
        }
    }
}
