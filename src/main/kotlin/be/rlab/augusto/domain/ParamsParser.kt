package be.rlab.augusto.domain

import be.rlab.augusto.domain.model.ParamDefinition
import be.rlab.augusto.domain.model.ParamValue
import be.rlab.nlp.Normalizer
import be.rlab.nlp.model.Language

class ParamsParser(
    /** List of parameters to parse. */
    private val params: List<ParamDefinition>,
    /** Language to normalize text. */
    private val language: Language
) {

    fun parse(text: String): List<ParamValue> {
        val tokens: List<String> = Normalizer(text, language)
            .removeStopWords()
            .applyStemming()
            .normalize()
            .split(" ")

        val params: List<ParamValue> = params.map { param ->
            val index: Int = tokens.indexOf(Normalizer(param.name, language).normalize())

            if (index == -1) {
                ParamValue.invalid(param.name)
            } else {

                if (param.args > 0) {
                    val validArgs: Boolean = (1 .. param.args).all { position ->
                        val argIndex = index + position

                        if (argIndex >= tokens.size) {
                            false
                        } else {
                            params.none { otherParam ->
                                Normalizer(otherParam.name, language).normalize() == tokens[argIndex]
                            }
                        }
                    }

                    if (validArgs) {
                        ParamValue.valid(
                            name = param.name,
                            values = tokens.slice(index + 1..index + param.args)
                        )
                    } else {
                        ParamValue.invalid(param.name)
                    }
                } else {
                    ParamValue.valid(
                        name = param.name,
                        values = emptyList()
                    )
                }
            }
        }

        return params.sortedBy { param ->
            if (param.valid) {
                tokens.indexOf(Normalizer(param.name, language).normalize())
            } else {
                Int.MAX_VALUE
            }
        }
    }
}
