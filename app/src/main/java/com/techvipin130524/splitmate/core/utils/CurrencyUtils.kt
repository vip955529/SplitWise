package com.techvipin130524.splitmate.core.utils

import java.util.Locale
import kotlin.math.abs

object CurrencyUtils {

    fun getCurrencySymbol(code: String): String {
        return when (code.uppercase()) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "CAD" -> "CA$"
            "AED" -> "AED "
            "JPY" -> "¥"
            else -> "₹"
        }
    }

    /**
     * Converts minor units (paise) to display string format.
     * Example: 1050L -> "₹10.50", 1000L -> "₹10.00"
     */
    fun formatPaise(paise: Long, currencySymbol: String = "₹"): String {
        val absPaise = abs(paise)
        val rupees = absPaise / 100
        val remainingPaise = absPaise % 100
        val formatted = String.format(Locale.US, "%s%d.%02d", currencySymbol, rupees, remainingPaise)
        return if (paise < 0) "-$formatted" else formatted
    }

    /**
     * Formats signed balance string with explicit + or - sign.
     * Example: 1050L -> "+₹10.50", -500L -> "-₹5.00", 0L -> "₹0.00"
     */
    fun formatSignedPaise(paise: Long, currencySymbol: String = "₹"): String {
        return when {
            paise > 0 -> "+${formatPaise(paise, currencySymbol)}"
            paise < 0 -> formatPaise(paise, currencySymbol)
            else -> formatPaise(0L, currencySymbol)
        }
    }

    /**
     * Parses a display string like "10.5" or "10.50" or "10" into minor units (paise).
     * Returns null if invalid input.
     */
    fun parseToPaise(amountString: String): Long? {
        val trimmed = amountString.trim().replace(",", "")
        if (trimmed.isEmpty()) return null
        return try {
            val doubleValue = trimmed.toDouble()
            if (doubleValue < 0) null else (doubleValue * 100).toLong()
        } catch (e: NumberFormatException) {
            null
        }
    }
}
