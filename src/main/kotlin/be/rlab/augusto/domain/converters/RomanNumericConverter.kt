package be.rlab.augusto.domain.converters

import be.rlab.augusto.domain.RomanNumeric

class RomanNumericConverter : NumberConverter {
    override fun fromDecimal(value: Int): String {
        return RomanNumeric.arabicToRoman(value)
    }

    override fun toDecimal(value: String): Int {
        return RomanNumeric.romanToDecimal(value)
    }
}