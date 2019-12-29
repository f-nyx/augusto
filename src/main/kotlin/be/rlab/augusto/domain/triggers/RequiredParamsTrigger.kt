package be.rlab.augusto.domain.triggers

import be.rlab.augusto.domain.ParamsParser
import be.rlab.augusto.domain.model.ParamDefinition
import be.rlab.augusto.domain.model.ParamValue
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.model.Chat
import be.rlab.tehanu.domain.model.TextMessage
import be.rlab.tehanu.domain.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RequiredParamsTrigger(
    /** Trigger name. */
    override val name: String,

    /** List of parameters to evaluate. */
    val params: List<ParamDefinition>,

    /** Minimum number of params that must be present in a message. */
    val minParams: Int,

    /** Maximum number of params that must be present in a message. */
    val maxParams: Int
) : Trigger {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RequiredParamsTrigger::class.java)
    }

    override fun applies(
        chat: Chat,
        user: User?,
        message: TextMessage,
        language: Language
    ): Boolean {
        return if (params.isEmpty()) {
            logger.info("no parameters to evaluate")
            true
        } else {
            evaluate(message.text, language)
        }
    }

    fun evaluate(
        text: String,
        language: Language
    ): Boolean {
        logger.info("evaluating parameters within the message")

        val resolvedParams = parseParams(text, language)

        val validCount: Int = resolvedParams.count { param ->
            param.valid
        }
        val validParams: Boolean = validCount in minParams..maxParams

        if (validParams) {
            logger.info("all required parameters exist")
        } else {
            logger.info("missing or invalid parameters: expected $minParams to $maxParams, $validCount found")
        }

        return validParams
    }

    private fun parseParams(
        text: String,
        language: Language
    ): List<ParamValue> {
        return ParamsParser(
            params = params,
            language = language
        ).parse(text)
    }
}
