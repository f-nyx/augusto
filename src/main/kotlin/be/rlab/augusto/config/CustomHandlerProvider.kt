package be.rlab.augusto.config

import be.rlab.augusto.domain.triggers.CategoryTrigger
import be.rlab.augusto.domain.triggers.TextTrigger
import be.rlab.nlp.TextClassifier
import be.rlab.search.IndexManager
import be.rlab.tehanu.config.DefaultHandlerProvider
import be.rlab.tehanu.messages.Trigger
import be.rlab.tehanu.annotations.Trigger as TriggerAnnotation
import com.typesafe.config.Config
import kotlin.reflect.KClass

class CustomHandlerProvider(
    config: Config,
    private val indexManager: IndexManager
) : DefaultHandlerProvider(config) {

    private val triggerFactories: Map<KClass<out Trigger>, (String, TriggerAnnotation) -> Trigger> = mapOf(
        TextTrigger::class to { targetName, triggerConfig ->
            TextTrigger(
                textClassifier = TextClassifier(
                    indexManager = indexManager,
                    namespace = targetName
                ),
                name = triggerConfig.name,
                startsWith = triggerParam(triggerConfig, "starts-with"),
                endsWith = triggerParam(triggerConfig, "ends-with"),
                contains = triggerParams(triggerConfig, "contains").toList(),
                ignoreCase = triggerParam(triggerConfig, "ignore-case")?.toBoolean() ?: true,
                regex = triggerParam(triggerConfig, "regex")?.toRegex(),
                normalize = triggerParam(triggerConfig, "normalize")?.toBoolean() ?: false
            )
        },
        CategoryTrigger::class to { targetName, triggerConfig ->
            CategoryTrigger(
                name = triggerConfig.name,
                textClassifier = TextClassifier(
                    indexManager = indexManager,
                    namespace = targetName
                ),
                category = triggerParam(triggerConfig, "category")
                    ?: throw RuntimeException("the trigger parameter 'category' is required"),
                score = triggerParam(triggerConfig, "score")?.toDouble() ?: 0.75
            )
        }
    )

    override fun resolveTrigger(
        targetName: String,
        type: KClass<out Trigger>,
        triggerConfig: TriggerAnnotation
    ): Trigger {
        return triggerFactories[type]?.let { factory ->
            factory(targetName, triggerConfig)
        } ?: throw RuntimeException("trigger type not supported: $type")
    }

    override fun resolveTrigger(
        targetName: String,
        type: String,
        triggerConfig: Config
    ): Trigger {
        return when(type) {
            "category" -> CategoryTrigger(
                name = triggerConfig.getString("name"),
                textClassifier = TextClassifier(
                    indexManager = indexManager,
                    namespace = targetName
                ),
                category = triggerConfig.getString("category"),
                score = triggerConfig.getDouble("score")
            )
            else -> super.resolveTrigger(targetName, type, triggerConfig)
        }
    }
}
