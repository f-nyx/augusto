package be.rlab.augusto.command

import be.rlab.augusto.domain.ParamsParser
import be.rlab.augusto.domain.model.ParamDefinition
import be.rlab.augusto.domain.triggers.RequiredParamsTrigger
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.view.UserInput
import be.rlab.tehanu.view.model.Field

fun UserInput.params(
    description: String,
    params: List<ParamDefinition> = emptyList(),
    language: Language = Language.SPANISH,
    minParams: Int,
    maxParams: Int = minParams
): Field = field(description) {

    buildValue { rawValue ->
        ParamsParser(
            params = params,
            language = language
        ).parse(rawValue.first().toString())
    }

    // TODO(nyx): add retry limit
    validator { value ->
        val trigger = RequiredParamsTrigger(
            name = "params field",
            params = params,
            minParams = minParams,
            maxParams = maxParams
        )

        require(trigger.evaluate(value, language)) {
            "no entiendo lo que me pedís"
        }
    }
}
