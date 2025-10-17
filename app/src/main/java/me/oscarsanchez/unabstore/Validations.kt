package me.oscarsanchez.unabstore

import android.util.Patterns


fun validateEmail(email:String): Pair<Boolean, String>{
    return when{
        email.isEmpty() -> Pair(false, "El correo es requerido.")
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->Pair(false, "El correo es invalido.")
        !email.endsWith("@test.com")-> Pair(false, "El email no es corporativo.")
        else ->{
            Pair(true, "El email no es corporativo.")
        }
    }

}
fun validatePassword(password:String): Pair<Boolean, String>{
    return when{
        password.isEmpty() -> Pair(false, "La contraseña es requerido.")
        password.length < 8 -> Pair(false, "La constraseña debe tener al menos 8 caracteres.")
        !password.any { it.isDigit()} -> Pair(false, "La constraseña debe tener al menos un numero.")
        else -> Pair(true,"")
    }
}

fun validateName(Name:String): Pair<Boolean, String>{
    return when{
        Name.isEmpty() -> Pair(false, "La nombre es requerido.")
        Name.length < 3 -> Pair(false, "La nombre debe tener al menos 3 caracteres.")
        else -> Pair(true,"")
    }
}

fun validateConfirmPassword(password: String, confirmPassword: String): Pair<Boolean, String>{
    return when{
        confirmPassword.isEmpty() -> Pair(false, "La contraseña es requerido.")
        confirmPassword != password -> Pair(false, "La contraseña debe tener al menos 3 caracteres.")
        else -> Pair(true,"")
    }
}