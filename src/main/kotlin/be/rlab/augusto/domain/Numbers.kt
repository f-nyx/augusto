package be.rlab.augusto.domain

import be.rlab.augusto.nlp.model.Language
import com.ibm.icu.text.NumberFormat
import com.ibm.icu.text.RuleBasedNumberFormat
import com.ibm.icu.util.ULocale

/** Utilities for numbers in different languages.
 */
object Numbers {

    /** Returns the natural language representation of a number.
     * @param value Number to spell.
     * @param language Language to spell.
     * @return the number spelled in the specified language.
     */
    fun spell(
        value: Int,
        language: Language
    ): String {
        val formatter: NumberFormat = RuleBasedNumberFormat(ULocale(language.code), RuleBasedNumberFormat.SPELLOUT)
        return formatter.format(value)
    }

    /** Parses a number from natural language.
     * @param value Number in natural language.
     * @param language Source language.
     * @return The number value.
     */
    fun parse(
        value: String,
        language: Language
    ): Number {
        val formatter: NumberFormat = RuleBasedNumberFormat(ULocale(language.code), RuleBasedNumberFormat.SPELLOUT)
        return formatter.parse(value)
    }
}
