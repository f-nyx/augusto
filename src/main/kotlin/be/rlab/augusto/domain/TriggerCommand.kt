package be.rlab.augusto.domain

import be.rlab.augusto.domain.model.ParamDefinition
import be.rlab.augusto.domain.model.ParamValue
import be.rlab.augusto.domain.triggers.Bind
import be.rlab.augusto.domain.triggers.Params
import be.rlab.augusto.domain.triggers.RequiredParamsTrigger
import be.rlab.augusto.domain.triggers.Trigger
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.MessageContext
import be.rlab.tehanu.domain.MessageListener
import be.rlab.tehanu.domain.model.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
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
abstract class TriggerCommand : MessageListener {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(TriggerCommand::class.java)
    }

    /** Command language. */
    protected abstract val language: Language

    /** List of available triggers to bind to handlers. */
    protected abstract val triggers: List<Trigger>
    protected abstract val messages: MessageSource

    private val handlers: MutableMap<KFunction<*>, List<Trigger>> = mutableMapOf()

    /** Verifies if a handler applies for this message.
     *
     * @param chat Source chat.
     * @param user User that sent the message.
     * @param message Message to evaluate.
     * @return true if there's a candidate handler for this message.
     */
    override fun applies(
        chat: Chat,
        user: User?,
        message: Message
    ): Boolean {
        return message is TextMessage && let {
            logger.info("searching matching handler for command $name")

            val anyApplies: Boolean = handlers.values.any { triggers ->
                applies(chat, user, message, triggers)
            }

            if (anyApplies) {
                logger.info("a handler applies")
            } else {
                logger.info("no handler applies for the message: ${message.text}")
            }

            val botMessage: Boolean = message.entities.any { entity ->
                entity.type == EntityType.BOT_COMMAND
            }

            anyApplies && !botMessage
        }
    }

    /** Executes the handler that applies for the message.
     * @param context Current context.
     * @param message Message to handle.
     * @return the next state of the message context.
     */
    @Suppress("UNCHECKED_CAST")
    override fun handle(
        context: MessageContext,
        message: Message
    ): MessageContext {
        require(message is TextMessage)

        return handlers.filterValues { triggers ->
            applies(context.chat, context.user, message, triggers)
        }.map { (handler, triggers) ->
            val params = getParams(message, triggers)

            if (handler.parameters.size == 3) {
                handler.call(this, context, message) as MessageContext
            } else {
                handler.call(this, context, message, params) as MessageContext
            }
        }.first()
    }

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

        this::class.functions.filter { method ->
            method.findAnnotation<Bind>() != null
        }.forEach { handler ->
            val bind: Bind = handler.findAnnotation()
                ?: throw RuntimeException("@Bind annotation not found.")
            val params: Params? = handler.findAnnotation()

            val resolvedTriggers: List<Trigger> = bind.triggers.map { name ->
                triggers.find { trigger ->
                    trigger.name == name
                } ?: throw RuntimeException("trigger $name not found")
            }
            require(handler.parameters.size in 3..4)
            require(handler.parameters[1].type.classifier == MessageContext::class)
            require(handler.parameters[2].type.classifier == TextMessage::class)
            require(handler.returnType.classifier == MessageContext::class)

            handler.isAccessible = true

            if (params == null) {
                handlers[handler] = resolvedTriggers
            } else {
                val paramsTrigger: RequiredParamsTrigger = paramsMap[params.name]
                    ?: throw RuntimeException("there is no field annotated with @Params for value ${params.name}")
                handlers[handler] = resolvedTriggers + paramsTrigger
            }
        }
    }

    private fun applies(
        chat: Chat,
        user: User?,
        message: TextMessage,
        triggers: List<Trigger>
    ): Boolean {
        logger.info("evaluating triggers")

        return triggers.all { trigger ->
            val applies: Boolean = trigger.applies(chat, user, message, language)

            logger.info("trigger '${trigger.name}' applies: $applies")

            applies
        }
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
