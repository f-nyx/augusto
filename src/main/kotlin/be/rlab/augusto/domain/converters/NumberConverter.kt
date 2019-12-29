package be.rlab.augusto.domain.converters

interface NumberConverter {
    fun toDecimal(value: String): Int
    fun fromDecimal(value: Int): String
}
