package be.rlab.augusto.config

import be.rlab.augusto.command.Cancel
import be.rlab.augusto.command.ConvertNumbers
import be.rlab.augusto.domain.MessageSource
import be.rlab.augusto.domain.NaturalService
import be.rlab.augusto.domain.triggers.CategoryTrigger
import be.rlab.augusto.domain.triggers.TextTrigger
import be.rlab.augusto.domain.triggers.Trigger
import be.rlab.augusto.nlp.IndexManager
import be.rlab.augusto.nlp.TextClassifier
import be.rlab.augusto.nlp.model.Language
import com.typesafe.config.Config
import com.typesafe.config.ConfigValueType
import org.springframework.context.support.beans

object ApplicationBeans {

    private data class CommandConfig(
        val name: String,
        val language: Language,
        val triggers: List<Trigger>,
        val messageSource: MessageSource
    )

    fun beans(config: Config) = beans {
        // Listeners
        bean {
            val commandConfig = getConfig(ref(), config, "ConvertNumbers")

            ConvertNumbers(
                name = commandConfig.name,
                language = commandConfig.language,
                triggers = commandConfig.triggers,
                messages = commandConfig.messageSource
            )
        }

        bean {
            val commandConfig = getConfig(ref(), config, "Cancel")

            Cancel(
                name = commandConfig.name,
                language = commandConfig.language,
                triggers = commandConfig.triggers,
                messages = commandConfig.messageSource
            )
        }

        bean<NaturalService>()

        bean {
            IndexManager(
                indexPath = config.getConfig("bot").getString("index-path")
            )
        }
    }

    private fun getConfig(
        indexManager: IndexManager,
        config: Config,
        commandName: String
    ): CommandConfig {
        return resolveCommandConfig(indexManager, config).getValue(commandName)
    }

    private fun resolveCommandConfig(
        indexManager: IndexManager,
        config: Config
    ): Map<String, CommandConfig> {
        require(config.getValue("commands").valueType() == ConfigValueType.OBJECT)

        @Suppress("UNCHECKED_CAST")
        val commandsNames = config.getConfig("commands").root().entries.map { entry ->
            entry.key
        }

        return commandsNames.map { commandName ->
            val commandConfig: Config = config.getConfig("commands").getConfig(commandName)
            val language = Language.valueOf(commandConfig.getString("default-language").toUpperCase())
            val triggers = commandConfig.getConfigList("triggers").map { triggerConfig ->
                resolveTrigger(indexManager, commandName, triggerConfig)
            }

            val messageSource: MessageSource = if (commandConfig.hasPath("messages")) {
                MessageSource.parse(
                    defaultLanguage = language,
                    messages = commandConfig.getConfig("messages")
                )
            } else {
                MessageSource.empty()
            }

            commandName to CommandConfig(
                name = commandName,
                language = language,
                triggers = triggers,
                messageSource = messageSource
            )
        }.toMap()
    }

    private fun resolveTrigger(
        indexManager: IndexManager,
        commandName: String,
        triggerConfig: Config
    ): Trigger {
        val textClassifier = TextClassifier(
            indexManager = indexManager,
            namespace = commandName
        )

        return when (val type = triggerConfig.getString("type")) {
            "category" -> CategoryTrigger(
                name = triggerConfig.getString("name"),
                textClassifier = textClassifier,
                category = triggerConfig.getString("category"),
                score = triggerConfig.getDouble("score")
            )
            "text" -> TextTrigger(
                name = triggerConfig.getString("name"),
                textClassifier = textClassifier,
                startsWith = stringOrNull(triggerConfig, "starts-with"),
                endsWith = stringOrNull(triggerConfig, "ends-with"),
                contains = stringListOrDefault(triggerConfig, "contains"),
                distance = floatOrDefault(triggerConfig, "distance"),
                ignoreCase = booleanOrDefault(triggerConfig, "ignore-case"),
                regex = stringOrNull(triggerConfig, "regex")?.toRegex(),
                normalize = booleanOrDefault(triggerConfig, "normalize", false)
            )
            else -> throw RuntimeException("trigger type not supported: $type")
        }
    }

    private fun stringOrNull(
        config: Config,
        key: String
    ): String? {
        return if (config.hasPath(key)) {
            config.getString(key)
        } else {
            null
        }
    }

    private fun stringListOrDefault(
        config: Config,
        key: String
    ): List<String> {
        return if (config.hasPath(key)) {
            config.getStringList(key)
        } else {
            emptyList()
        }
    }

    private fun booleanOrDefault(
        config: Config,
        key: String,
        defaultValue: Boolean = true
    ): Boolean {
        return if (config.hasPath(key)) {
            config.getBoolean(key)
        } else {
            defaultValue
        }
    }

    private fun floatOrDefault(
        config: Config,
        key: String,
        defaultValue: Float = 0.0F
    ): Float {
        return if (config.hasPath(key)) {
            config.getDouble(key).toFloat()
        } else {
            defaultValue
        }
    }
}
