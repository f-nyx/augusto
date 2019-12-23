package be.rlab.augusto.nlp

import be.rlab.augusto.nlp.model.Language
import java.text.Normalizer as JavaNormalizer

/** String normalizer.
 *
 * By default it removes diacritics, applies the stemmer for the specified language, converts all terms to
 * lowercase, and joins the terms with a single space.
 *
 * @param text Text to normalize.
 * @param language Text language.
 * @param caseSensitive Indicates whether to convert string to lowercase or not.
 * @param form Normalization form.
 * @param removeDiacritics Indicates whether to remove diacritics.
 * @param stemming Indicates whether to apply the stemer to each term.
 * @param tokenize Indicates whether to split text into words.
 * @param joinWith String to join the terms.
 */
data class Normalizer(
    private val text: String,
    private val language: Language,
    private val caseSensitive: Boolean = false,
    private val form: JavaNormalizer.Form = JavaNormalizer.Form.NFD,
    private val removeDiacritics: Boolean = true,
    private val stemming: Boolean = true,
    private val tokenize: Boolean = true,
    private val joinWith: String = " "
) {
    companion object {
        private val REGEX_UNACCENT: Regex = Regex("\\p{InCombiningDiacriticalMarks}+")

        /** Creates a new normalizer for the specified text.
         * @param text Text to normalize.
         * @param language Text language.
         * @return a new normalizer.
         */
        fun new(
            text: String,
            language: Language
        ): Normalizer = Normalizer(
            text = text,
            language = language
        )
    }

    /** Word tokenizer to split text into words.
     */
    private val wordTokenizer = WordTokenizer()

    fun caseSensitive(): Normalizer = copy(
        caseSensitive = true
    )

    fun caseInsensitive(): Normalizer = copy(
        caseSensitive = false
    )

    fun form(form: JavaNormalizer.Form): Normalizer = copy(
        form = form
    )

    fun removeDiacritics(): Normalizer = copy(
        removeDiacritics = true
    )

    fun keepDiacritics(): Normalizer = copy(
        removeDiacritics = false
    )

    fun applyStemming(): Normalizer = copy(
        stemming = true
    )

    fun skipStemming(): Normalizer = copy(
        stemming = false
    )

    fun applyTokenizer(): Normalizer = copy(
        tokenize = true
    )

    fun skipTokenizer(): Normalizer = copy(
        tokenize = false
    )

    fun joinWith(joinText: String): Normalizer = copy(
        joinWith = joinText
    )

    /** Applies normalizations and returns the normalized text.
     * @return a valid text.
     */
    fun normalize(): String {
        val normalizedText = with(JavaNormalizer.normalize(text, form)) { ->
            if (removeDiacritics) {
                replace(REGEX_UNACCENT, "")
            } else {
                this
            }
        }

        return if (tokenize) {
            wordTokenizer.tokenize(normalizedText.reader()).map { word ->
                word.toString()
            }
        } else {
            listOf(normalizedText)
        }.map { token ->
            if (caseSensitive) {
                token
            } else {
                token.toLowerCase()
            }
        }.joinToString(joinWith) { token ->
            if (stemming) {
                val stemmer: SnowballStemmer = SnowballStemmer.new(language)
                stemmer.stem(token)
            } else {
                token
            }
        }
    }
}
