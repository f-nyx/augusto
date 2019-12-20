package be.rlab.augusto.domain

object RomanNumeric {

    private val romantoArabicMapping : Map<Int, String> = mapOf(
        1 to "I",
        4 to "IV",
        5 to "V",
        9 to "IX",
        10 to "X",
        40 to "XL",
        50 to "L",
        90 to "XC",
        100 to "C",
        400 to "CD",
        500 to "D",
        900 to "CM",
        1000 to "M"
    )

    private val arabicToRomanMapping : Map<Int, String> = mapOf(
        1000 to "M",
        900 to "CM",
        500 to "D",
        400 to "CD",
        100 to "C",
        90 to "XC",
        50 to "L",
        40 to "XL",
        10 to "X",
        9 to "IX",
        5 to "V",
        4 to "IV",
        1 to "I"
    )

    fun romanToDecimal(str: String): Int {
        var res = 0
        var i = 0
        while (i < str.length) {
            val s1 = toArabic(str[i])
            if (i + 1 < str.length) {
                val s2 = toArabic(str[i + 1])
                if (s1 >= s2) {
                    res += s1
                } else {
                    res = res + s2 - s1
                    i++
                }
            } else {
                res += s1
                i++
            }
            i++
        }
        return res
    }

    fun arabicToRoman(arabicToConvert: Int): String {
        var arabicRemainder = arabicToConvert
        var romanOutput = ""
        arabicToRomanMapping.forEach { (arabic, roman) ->
            while(arabicRemainder >= arabic) {
                romanOutput += roman
                arabicRemainder -= arabic
            }
        }
        return romanOutput
    }

    // This function returns value of a Roman symbol
    private fun toArabic(romanDigit: Char): Int {
        return romantoArabicMapping.entries.find { (arabic, roman) ->
            roman[0] == romanDigit
        }?.key ?: -1
    }
}
