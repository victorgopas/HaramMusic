package com.bicheator.harammusic.core.util


object Validators {

    fun isValidUsername(username: String): Boolean =
        username.trim().length in 3..20

    fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    fun isValidPassword(password: String): Boolean =
        password.length >= 6
}
