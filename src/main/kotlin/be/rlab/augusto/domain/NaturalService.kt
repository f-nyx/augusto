package be.rlab.augusto.domain

import be.rlab.augusto.nlp.IndexManager
import be.rlab.augusto.nlp.TextClassifier
import be.rlab.augusto.nlp.model.Language
import be.rlab.augusto.nlp.model.TrainingDataSet
import be.rlab.tehanu.domain.Memory
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
            val trainingConfig: Config = ConfigFactory.load("nlp/training/${resource}")
            val force: Boolean = trainingConfig.getBoolean("force")
            val classifierName: String = trainingConfig.getString("classifier")

            if (force || !appliedTrainingSets.contains(resource)) {
                val newTrainingSets: List<TrainingDataSet> = trainingConfig.getConfigList("training")
                    .map(this::createTrainingSet)

                val classifier = TextClassifier(
                    indexManager = indexManager,
                    namespace = classifierName
                )

                classifier.train(newTrainingSets)

                trainingSets = trainingSets + newTrainingSets
                appliedTrainingSets = appliedTrainingSets + resource
            }
        }

        indexManager.sync()
    }

    private fun createTrainingSet(config: Config): TrainingDataSet {
        return TrainingDataSet(
            language = Language.valueOf(config.getString("language").toUpperCase()),
            categories = config.getStringList("categories"),
            values = config.getStringList("values")
        )
    }
}