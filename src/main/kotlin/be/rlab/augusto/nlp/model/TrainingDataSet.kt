package be.rlab.augusto.nlp.model

data class TrainingDataSet(
    val language: Language,
    val categories: List<String>,
    val values: List<String>
)
