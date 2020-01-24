package be.rlab.augusto.command

import be.rlab.augusto.domain.MessageNormalizer
import be.rlab.augusto.domain.converters.DecimalNumberConverter
import be.rlab.augusto.domain.converters.NumberConverter
import be.rlab.augusto.domain.converters.RomanNumericConverter
import be.rlab.augusto.domain.triggers.CategoryTrigger
import be.rlab.augusto.domain.triggers.TextTrigger
import be.rlab.nlp.model.Language
import be.rlab.tehanu.annotations.*
import be.rlab.tehanu.messages.MessageContext
import be.rlab.tehanu.messages.model.ParamValue
import be.rlab.tehanu.messages.model.TextMessage
import be.rlab.tehanu.view.model.params

@MessageListener("ConvertNumbers")
@Params(
    Param("romanos"),
    Param("decimales"),
    Param("arábigos"),
    Param("naturales"),
    Param("comunes"),
    Param("base", 1)
)
class ConvertNumbers {

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

    @Handler("mention", minParams = NUM_PARAMS, maxParams = NUM_PARAMS)
    @Trigger(type = CategoryTrigger::class, params = [
        TriggerParam("namespace", "ConvertNumbers::convert"),
        TriggerParam("score", "0.75")
    ])
    fun convertFromUnits(
        context: MessageContext
    ): MessageContext = withConverters(context) { sourceConverter, targetConverter ->
        context.answer(context.messages[ACCEPT_ANSWERS])
        context.userInput {
            val value: String by field(context.messages[CONVERT_ANSWERS])

            onSubmit {
                val decimal: Int = sourceConverter.toDecimal(value)
                val target: String = targetConverter.fromDecimal(decimal)
                context.talk(target)
            }
        }
    }

    @Handler("mention")
    @Trigger(
        type = TextTrigger::class,
        params = [
            TriggerParam("regex", "(\\d+)"),
            TriggerParam("normalize", "true")
        ]
    )
    fun convertFromNumber(
        context: MessageContext,
        message: TextMessage
    ): MessageContext = context.userInput {
        val params: List<ParamValue> by params(context.messages[UNIT_ANSWERS], NUM_PARAMS)

        onSubmit {
            withConverters(context, params) { sourceConverter, targetConverter ->
                val decimal: Int = sourceConverter.toDecimal(
                    MessageNormalizer(message, Language.SPANISH).normalize()
                )
                val target: String = targetConverter.fromDecimal(decimal)
                context.talk(target)
            }
        }
    }

    @Handler("mention")
    @Trigger(
        type = CategoryTrigger::class,
        params = [
            TriggerParam("namespace", "ConvertNumbers::help"),
            TriggerParam("score", "0.75")
        ]
    )
    fun help(context: MessageContext): MessageContext = with(context) {
        return context.talk(messages[HELP])
    }

    private fun withConverters(
        context: MessageContext,
        params: List<ParamValue> = context.params,
        callback: (NumberConverter, NumberConverter) -> MessageContext
    ): MessageContext = with(context) {
        val sourceConverter: NumberConverter? = converters[params[0].name]
        val targetConverter: NumberConverter? = converters[params[1].name]

        return when {
            sourceConverter == null ->
                answer(messages.get(UNKNOWN_ANSWERS, CONVERT_FROM, params[0]))

            targetConverter == null ->
                answer(messages.get(UNKNOWN_ANSWERS, CONVERT_TO, params[1]))

            else ->
                callback(sourceConverter, targetConverter)
        }
    }
}
