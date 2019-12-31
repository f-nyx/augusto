package be.rlab.augusto.domain

import be.rlab.augusto.nlp.model.Language
import com.typesafe.config.Config
import com.typesafe.config.ConfigValueType

typealias MessageMap = Map<String, List<String>>

class MessageSource(
    private val messages: Map<Language, MessageMap>,
    private val defaultLanguage: Language
) {
    companion object {
        fun empty(): MessageSource =
            MessageSource(emptyMap(), Language.SPANISH)

        fun parse(
            messages: Config,
            defaultLanguage: Language
        ): MessageSource = MessageSource(
            defaultLanguage = defaultLanguage,
            messages = messages.root().entries.map { entry ->
                entry.key
            }.map { language ->
                val resolvedMessages: Config = messages.getConfig(language)
                val names: List<String> = resolvedMessages.root().entries.map { entry ->
                    entry.key
                }

                val messageMap: MessageMap = names.map { name ->
                    val values = when(resolvedMessages.getValue(name).valueType()) {
                        ConfigValueType.LIST -> resolvedMessages.getStringList(name)
                        else -> listOf(resolvedMessages.getString(name))
                    }

                    name to values
                }.toMap()

                Language.valueOf(language.toUpperCase()) to messageMap
            }.toMap()
        )
    }

    operator fun get(name: String): String {
        return messages.getValue(defaultLanguage)[name]?.let { messages ->
            resolve(defaultLanguage, messages.random())
        } ?: throw RuntimeException("message not found: $name")
    }

    fun get(
        name: String,
        vararg args: Any
    ): String {
        return messages.getValue(defaultLanguage)[name]?.let { messages ->
            resolve(defaultLanguage, messages.random(), *args)
        } ?: throw RuntimeException("message not found: $name")
    }

    private fun resolve(
        language: Language,
        message: String,
        vararg args: Any
    ): String {
        val messageMap: MessageMap = messages.getValue(language)

        val expanded: String = messageMap.keys.fold(message) { resolved, name ->
            val values: List<String> = messageMap.getValue(name)
            resolved.replace("{$name}", values.random())
        }

        return args.foldIndexed(expanded) { index, resolved, current ->
            val value = resolve(language, current.toString())
            resolved.replace("{$index}", value)
        }
    }
}
