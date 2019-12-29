package be.rlab.augusto.domain.converters

class DecimalNumberConverter : NumberConverter {
    override fun fromDecimal(value: Int): String {
        return value.toString()
    }

    override fun toDecimal(value: String): Int {
        return value.toInt()
    }
}