package be.rlab.augusto.nlp

import be.rlab.augusto.nlp.model.*
import org.apache.lucene.classification.SimpleNaiveBayesClassifier
import org.apache.lucene.index.Term
import org.apache.lucene.search.TermQuery
import org.apache.lucene.util.BytesRef
import java.nio.charset.Charset
import org.apache.lucene.classification.ClassificationResult as LuceneClassificationResult

/** This class allows to train and to query a [naive bayes classifier](https://en.wikipedia.org/wiki/Naive_Bayes_classifier).
 * It uses an [Index] to store the training data set.
 */
class TextClassifier(
    private val indexManager: IndexManager,
    private val namespace: String
) {

    companion object {
        private const val CATEGORY_FIELD: String = "category"
        private const val TEXT_FIELD: String = "text"
    }

    /** Analyzes and sets the category for a text.
     * It stores the text and the category into the index.
     *
     * @param category Text category.
     * @param text Text to assign the specified category.
     * @param language Text language.
     */
    fun train(
        category: String,
        text: String,
        language: Language
    ) {
        indexManager.index(
            Document.new(
                namespace, language,
                Field.text(CATEGORY_FIELD, category),
                Field.text(TEXT_FIELD, text)
            )
        )
    }

    /** Trains the classifier from data sets.
     * @param dataSets Data sets used to train this classifier.
     */
    fun train(dataSets: List<TrainingDataSet>) {
        dataSets.forEach { dataSet ->
            dataSet.categories.zip(dataSet.values).forEach { (category, value) ->
                train(category, value, dataSet.language)
            }
        }
    }

    /** Resolves the top category for a text.
     * @param text Text to resolve category.
     * @param language Text language.
     * @return the resolved category or null if there's no matching category.
     */
    fun classify(
        text: String,
        language: Language
    ): String? {
        val result: LuceneClassificationResult<BytesRef>? = classifier(language).assignClass(text)
        return result?.assignedClass?.bytes?.toString(Charset.defaultCharset())
    }

    /** Resolves all categories for a text.
     * @param text Text to search categories for.
     * @param language Text language.
     * @return the list of matching categories.
     */
    fun classifyAll(
        text: String,
        language: Language
    ): List<ClassificationResult> {
        return classifier(language).getClasses(text).map { result ->
            ClassificationResult(
                assignedClass = result.assignedClass.utf8ToString(),
                score = result.score
            )
        }
    }

    private fun classifier(language: Language): SimpleNaiveBayesClassifier {
        val index: Index = indexManager.index(language)

        return SimpleNaiveBayesClassifier(
            index.indexReader,
            index.analyzer,
            TermQuery(Term(IndexManager.NAMESPACE_FIELD, namespace)),
            CATEGORY_FIELD,
            TEXT_FIELD
        )
    }
}
