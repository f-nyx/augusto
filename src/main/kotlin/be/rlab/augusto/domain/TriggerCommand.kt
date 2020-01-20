package be.rlab.augusto.domain

import be.rlab.augusto.domain.model.ParamDefinition
import be.rlab.augusto.domain.model.ParamValue
import be.rlab.augusto.domain.triggers.Params
import be.rlab.augusto.domain.triggers.RequiredParamsTrigger
import be.rlab.nlp.model.Language
import be.rlab.tehanu.messages.model.TextMessage
import be.rlab.tehanu.triggers.Trigger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

/** Must be extended by bot commands in order to support language-specific triggers.
 *
 * The command must provide a list of [triggers] to evaluate incoming messages. It must [bind] a
 * TriggerHandler to any of the existing triggers. When all triggers for a TriggerHandler applies,
 * the handler is executed.
 *
 * TriggerHandlers may require parameters that can be defined in the [bind] method. All required
 * parameters must exist in the message in order to trigger the handler.
 *
 * This command only supports text messages.
 */
abstract class TriggerCommand {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TriggerCommand::class.java)
    }

    /** Command language. */
    protected abstract val language: Language

    fun initialize() {
        val paramsMap: Map<String, RequiredParamsTrigger> = this::class.declaredMemberProperties.filter { property ->
            val validType: Boolean =
                property.returnType.classifier == ParamDefinition::class ||
                        property.returnType.classifier == List::class
            val hasAnnotation: Boolean =
                property.javaField?.getAnnotation(Params::class.java) != null

            validType && hasAnnotation
        }.map { property ->
            val params = property.javaField?.getAnnotation(Params::class.java)!!

            property.isAccessible = true

            @Suppress("UNCHECKED_CAST")
            val definitions: List<ParamDefinition> = if (property.returnType.classifier == List::class) {
                property.getter.call(this) as List<ParamDefinition>
            } else {
                emptyList()
            }

            params.name to RequiredParamsTrigger(
                name = params.name,
                params = definitions,
                minParams = params.minParams,
                maxParams = params.maxParams
            )
        }.toMap()
    }

    private fun getParams(
        message: TextMessage,
        triggers: List<Trigger>
    ): List<ParamValue> {
        return triggers.find { trigger ->
            trigger is RequiredParamsTrigger
        }?.let { paramsTrigger ->
            val resolvedParams = ParamsParser(
                params = (paramsTrigger as RequiredParamsTrigger).params,
                language = language
            ).parse(message.text)

            resolvedParams.filter { param ->
                param.valid
            }
        } ?: emptyList()
    }
}
