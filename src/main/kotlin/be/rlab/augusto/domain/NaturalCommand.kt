package be.rlab.augusto.domain

import be.rlab.augusto.domain.model.CommandTrigger
import be.rlab.augusto.domain.model.ParamParseException
import be.rlab.augusto.domain.model.ParamValue
import be.rlab.augusto.nlp.Normalizer
import be.rlab.augusto.nlp.TextClassifier
import be.rlab.augusto.nlp.model.Language
import be.rlab.tehanu.domain.MessageContext
import be.rlab.tehanu.domain.MessageListener
import be.rlab.tehanu.domain.model.Chat
import be.rlab.tehanu.domain.model.Message
import be.rlab.tehanu.domain.model.TextMessage
import be.rlab.tehanu.domain.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Must be extended by bot commands in order to support triggering with natural language.
 * It uses a [TextClassifier] that must be previously trained.
 *
 * The command must define a list of [triggers] that indicate the categories and minimum thresholds that a
 * message should match in order to execute the command.
 *
 * This command only supports text messages.
 */
abstract class NaturalCommand : MessageListener {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(NaturalCommand::class.java)
    }

    /** Text classifier, injected by the application context.
     */
    lateinit var textClassifier: TextClassifier

    /** List of triggers that will be evaluated on each message to determine whether this command must be
     * executed or not.
     */
    protected abstract val triggers: List<CommandTrigger>

    /** Determines whether any trigger matches any of the message resolved categories.
     * It returns true if any trigger matches a resolved category and the category score is equal
     * to or greater than the trigger minimum score.
     */
    override fun applies(
        chat: Chat,
        user: User?,
        message: Message
    ): Boolean {
        return message is TextMessage && let {
            return try {
                // TODO(nyx): use user language.
                classify(message, Language.SPANISH)?.let { trigger ->
                    parseParams(trigger, message)
                }
                true
            } catch (cause: ParamParseException) {
                logger.info("command matches but there are missing parameters: ${cause.message}")
                false
            }
        }
    }

    override fun handle(
        context: MessageContext,
        message: Message
    ): MessageContext {
        require(message is TextMessage)

        val category: String = textClassifier.classifyAll(
            text = message.text,
            language = Language.SPANISH
        ).first().assignedClass

        return triggers.find { trigger ->
            trigger.category == category
        }?.let { trigger ->
            trigger.handler(context, message, getParams(trigger, message))
        } ?: throw RuntimeException("there is no registered trigger for $category")
    }

    private fun classify(
        message: TextMessage,
        language: Language
    ): CommandTrigger? {
        return textClassifier.classifyAll(
            text = message.text,
            language = language
        ).map { category ->
            val normalizedCategory: String = Normalizer.new(
                text = category.assignedClass,
                language = language
            ).normalize()

            triggers.find { trigger ->
                category.score >= trigger.score &&
                normalizedCategory == trigger.category
            }
        }.first()
    }

    private fun getParams(
        trigger: CommandTrigger,
        message: TextMessage
    ): List<ParamValue> {
        return parseParams(trigger, message).filter { param ->
            param.valid
        }
    }

    private fun parseParams(
        trigger: CommandTrigger,
        message: TextMessage
    ): List<ParamValue> {
        val tokens: List<String> = Normalizer(message.text, trigger.language)
            .removeStopWords()
            .applyStemming()
            .normalize()
            .split(" ")

        val params: List<ParamValue> = trigger.config.params.map { param ->
            val index: Int = tokens.indexOf(Normalizer(param.name, trigger.language).normalize())

            if (index == -1) {
                ParamValue.invalid(param.name)
            } else {

                if (param.args > 0) {
                    val validArgs: Boolean = (1 .. param.args).all { position ->
                        val argIndex = index + position

                        if (argIndex >= tokens.size) {
                            false
                        } else {
                            trigger.config.params.none { otherParam ->
                                Normalizer(otherParam.name, trigger.language).normalize() == tokens[argIndex]
                            }
                        }
                    }

                    if (validArgs) {
                        ParamValue.valid(
                            name = param.name,
                            values = tokens.slice(index + 1 .. index + param.args)
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

        val validCount: Int = params.count { param ->
            param.valid
        }

        if (validCount < trigger.config.minParams || validCount > trigger.config.maxParams) {
            throw ParamParseException("invalid number of parameters")
        }

        return params.sortedBy { param ->
            if (param.valid) {
                tokens.indexOf(Normalizer(param.name, trigger.language).normalize())
            } else {
                Int.MAX_VALUE
            }
        }
    }
}
