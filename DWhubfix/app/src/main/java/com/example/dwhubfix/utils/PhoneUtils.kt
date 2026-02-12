package com.example.dwhubfix.utils

object PhoneUtils {
    fun formatPhoneNumber(input: String): String {
        val cleaned = input.replace(Regex("[^0-9+]"), "")
        if (cleaned.startsWith("0")) {
            return "62" + cleaned.substring(1)
        }
        if (cleaned.startsWith("+")) {
            return cleaned.substring(1)
        }
        return cleaned
    }

    fun isValidPhoneNumber(input: String): Boolean {
        // Simple check: at least 7-8 digits
        val cleaned = input.replace(Regex("[^0-9]"), "")
        return cleaned.length >= 8
    }
}
