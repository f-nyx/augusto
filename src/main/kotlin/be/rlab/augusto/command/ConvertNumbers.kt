package be.rlab.augusto.command

import be.rlab.augusto.domain.MessageNormalizer
import be.rlab.augusto.domain.TriggerCommand
import be.rlab.augusto.domain.converters.DecimalNumberConverter
import be.rlab.augusto.domain.converters.NumberConverter
import be.rlab.augusto.domain.converters.RomanNumericConverter
import be.rlab.augusto.domain.model.ParamDefinition
import be.rlab.augusto.domain.model.ParamDefinition.Companion.param
import be.rlab.augusto.domain.model.ParamValue
import be.rlab.augusto.domain.triggers.Trigger
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.MessageContext
import be.rlab.tehanu.domain.model.TextMessage

class ConvertNumbers(
    override val name: String,
    override val language: Language,
    override val triggers: List<Trigger>
) : TriggerCommand() {

    companion object {
        const val NUM_PARAMS: Int = 2
    }

    private val supportedParams: List<ParamDefinition> = listOf(
        param("romanos"),
        param("decimales"),
        param("arábigos"),
        param("naturales"),
        param("comunes"),
        param("base", 1)
    )

    private val converters: Map<String, NumberConverter> = mapOf(
        "romanos" to RomanNumericConverter(),
        "decimales" to DecimalNumberConverter(),
        "arábigos" to DecimalNumberConverter(),
        "naturales" to DecimalNumberConverter(),
        "comunes" to DecimalNumberConverter()
    )

    private val okAnswers: List<String> = listOf(
        "dale", "de una", "bueno", "okey", "dale, convierto",
        "oka", "hecho, convirtiendo"
    )

    private val convertAnswers: List<String> = listOf(
        "qué valor querés", "qué número querés", "decime un número para",
        "escribí el número que querés", "decime qué valor querés"
    )

    private val unitAnswers: List<String> = listOf(
        "decime de qué unidad a qué unidad querés", "decime desde qué unidad y a cuál querés"
    )

    private val cannotConvertAnswers: List<String> = listOf(
        "no sé cómo", "no tengo idea cómo", "no puedo", "no me enseñaron a"
    )

    init {
        bind(
            this::convertFromUnits, listOf(trigger("mention"), trigger("convert")),
            params = supportedParams, minParams = NUM_PARAMS
        )

        bind(this::convertFromNumber, listOf(trigger("mention"), trigger("numbers")))
        bind(this::help, listOf(trigger("mention"), trigger("help")))
    }

    private fun convertFromUnits(
        context: MessageContext,
        message: TextMessage,
        params: List<ParamValue>
    ): MessageContext {
        val sourceConverter: NumberConverter? = converters[params[0].name]
        val targetConverter: NumberConverter? = converters[params[1].name]

        return when {
            sourceConverter == null -> {
                context.answer("${cannotConvertAnswers.random()} convertir desde ${params[0]}")
            }
            targetConverter == null -> {
                context.answer("${cannotConvertAnswers.random()} convertir a ${params[1]}")
            }
            else -> {
                context.answer(okAnswers.random())
                context.userInput {
                    val value: String by field(
                        "${convertAnswers.random()} que convierta"
                    )

                    onSubmit {
                        val decimal: Int = sourceConverter.toDecimal(value)
                        val target: String = targetConverter.fromDecimal(decimal)
                        context.talk(target)
                    }
                }
            }
        }
    }

    private fun convertFromNumber(
        context: MessageContext,
        message: TextMessage,
        params: List<ParamValue>
    ): MessageContext = context.userInput {
        val values: List<ParamValue> by params(
            "${unitAnswers.random()} que convierta", supportedParams, minParams = NUM_PARAMS
        )

        onSubmit {
            val sourceConverter: NumberConverter? = converters[values[0].name]
            val targetConverter: NumberConverter? = converters[values[1].name]

            when {
                sourceConverter == null -> {
                    context.answer("${cannotConvertAnswers.random()} convertir desde ${params[0]}")
                }
                targetConverter == null -> {
                    context.answer("${cannotConvertAnswers.random()} convertir a ${params[1]}")
                }
                else -> {
                    val decimal: Int = sourceConverter.toDecimal(
                        MessageNormalizer(message, language).normalize()
                    )
                    val target: String = targetConverter.fromDecimal(decimal)
                    context.talk(target)
                }
            }
        }
    }

    private fun help(
        context: MessageContext,
        message: TextMessage,
        params: List<ParamValue>
    ): MessageContext {
        return context.talk("""
            por ahora solamente puedo convertir de números romanos a decimales y viceversa
        """.trimIndent())
    }
}
