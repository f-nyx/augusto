package be.rlab.augusto.domain

import be.rlab.augusto.domain.triggers.CategoryTrigger
import be.rlab.nlp.TextClassifier
import be.rlab.nlp.model.Language
import be.rlab.nlp.model.TrainingDataSet
import be.rlab.search.IndexManager
import be.rlab.tehanu.store.Memory
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class NaturalService(
    memory: Memory,
    private val indexManager: IndexManager
) {

    companion object {
        private const val APPLIED_TRAINING_SETS_SLOT = "appliedTrainingSets"
        private const val TRAINING_SETS_SLOT = "trainingSets"
    }

    private var appliedTrainingSets: Set<String> by memory.slot(APPLIED_TRAINING_SETS_SLOT, emptySet<String>())
    private var trainingSets: List<TrainingDataSet> by memory.slot(TRAINING_SETS_SLOT, emptyList<TrainingDataSet>())

    /** Loads training data from configuration files, if required.
     */
    fun loadTrainingData() {
        Thread.currentThread().contextClassLoader.getResourceAsStream(
            "nlp/training/"
        )!!.reader().readLines().filter { resource ->
            resource.endsWith(".conf")
        }.forEach { resource ->
            val trainingConfig: Config = ConfigFactory.load("nlp/training/${resource}").resolve()

            trainingConfig.getConfigList("training-set").forEach { config ->
                train(resource, config)
            }
        }

        indexManager.sync()
    }

    private fun train(
        resource: String,
        trainingConfig: Config
    ) {
        val force: Boolean = trainingConfig.getBoolean("force")

        if (force || !appliedTrainingSets.contains(resource)) {
            val namespace: String = trainingConfig.getString("namespace")
            val newTrainingSets: List<TrainingDataSet> = trainingConfig.getConfigList("training")
                .map(this::createTrainingSet)

            val classifier = TextClassifier(
                indexManager = indexManager,
                namespace = namespace
            )

            classifier.train(newTrainingSets)

            trainingSets = trainingSets + newTrainingSets
            appliedTrainingSets = appliedTrainingSets + resource
        }
    }

    private fun createTrainingSet(config: Config): TrainingDataSet {
        return TrainingDataSet(
            language = Language.valueOf(config.getString("language").toUpperCase()),
            categories = if (config.hasPath("categories")) {
                config.getStringList("categories")
            } else {
                listOf(CategoryTrigger.DEFAULT_CATEGORY)
            },
            values = config.getStringList("values")
        )
    }
}