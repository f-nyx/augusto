package be.rlab.augusto.domain

import be.rlab.augusto.domain.model.ParamDefinition
import be.rlab.augusto.domain.model.ParamValue
import be.rlab.augusto.domain.triggers.RequiredParamsTrigger
import be.rlab.augusto.domain.triggers.Trigger
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.MessageContext
import be.rlab.tehanu.domain.MessageListener
import be.rlab.tehanu.domain.model.Chat
import be.rlab.tehanu.domain.model.Message
import be.rlab.tehanu.domain.model.TextMessage
import be.rlab.tehanu.domain.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory

typealias TriggerHandler = (
    MessageContext,
    TextMessage,
    List<ParamValue>
) -> MessageContext

/** Must be extended by bot commands in order to support language-specific triggers.
 *
 * The command must provide a list of [triggers] to evaluate incoming messages. It must [bind] a
 * [TriggerHandler] to any of the existing triggers. When all triggers for a [TriggerHandler] applies,
 * the handler is executed.
 *
 * [TriggerHandler]s may require parameters that can be defined in the [bind] method. All required
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

    private val handlers: MutableMap<TriggerHandler, List<Trigger>> = mutableMapOf()

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

            anyApplies
        }
    }

    /** Executes the handler that applies for the message.
     * @param context Current context.
     * @param message Message to handle.
     * @return the next state of the message context.
     */
    override fun handle(
        context: MessageContext,
        message: Message
    ): MessageContext {
        require(message is TextMessage)

        return handlers.filterValues { triggers ->
            applies(context.chat, context.user, message, triggers)
        }.map { (handler, triggers) ->
            val params = getParams(message, triggers)
            handler(context, message, params)
        }.first()
    }

    protected fun bind(
        handler: TriggerHandler,
        triggers: List<Trigger>,
        params: List<ParamDefinition> = emptyList(),
        minParams: Int = 0,
        maxParams: Int = minParams
    ) {
        handlers[handler] = triggers + RequiredParamsTrigger(
            name = "required_params",
            params = params,
            minParams = minParams,
            maxParams = maxParams
        )
    }

    protected fun trigger(name: String): Trigger {
        return triggers.find { trigger ->
            trigger.name == name
        } ?: throw RuntimeException("trigger not found: $name")
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
        val paramsTrigger: RequiredParamsTrigger = triggers.find { trigger ->
            trigger is RequiredParamsTrigger
        } as RequiredParamsTrigger

        val resolvedParams = ParamsParser(
            params = paramsTrigger.params,
            language = language
        ).parse(message.text)

        return resolvedParams.filter { param ->
            param.valid
        }
    }
}
