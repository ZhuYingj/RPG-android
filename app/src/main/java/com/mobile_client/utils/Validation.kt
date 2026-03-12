package com.mobile_client.utils

object Validation {
    const val MAX_USERNAME_LENGTH = 20
    const val MAX_EMAIL_LENGTH = 40
    const val MAX_PASSWORD_LENGTH = 20

    val EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[a-zA-Z]{2,}$".toRegex()

    fun validateUsername(username: String): String? = when {
        username.isBlank() -> "Nom d'utilisateur requis"
        username.contains(" ") -> "Nom d'utilisateur ne doit pas contenir d'espaces"
        else -> null
    }

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Courriel requis"
        !EMAIL_REGEX.matches(email) -> "Format d'email invalide"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isBlank() -> "Mot de passe requis"
        password.length < 5 || !password.contains(Regex("[0-9]")) ->
            "Minimum de 5 caractères incluant un chiffre"
        else -> null
    }
}
