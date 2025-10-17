package me.oscarsanchez.unabstore

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun NavigationApp(){

    val myNavController = rememberNavController()
    var mystartDestination : String = "login"

    val auth = Firebase.auth
    var currentUser =auth.currentUser

    if(currentUser !=null){
        mystartDestination = "home"
    }else{
        mystartDestination = "login"
    }

    NavHost(
        navController = myNavController,
        startDestination = mystartDestination
    ){
        composable("login"){
            LoginScreen (onClickRegister ={
                myNavController.navigate("register")
            }, onSuccessfulLogin ={
                myNavController.navigate("home"){
                    popUpTo("login"){inclusive = true}
                }
            })
        }
        composable("register"){
            RegisterScreen(onClickBack ={
                myNavController.popBackStack()
            }, onSuccessfulRegister = {
                myNavController.navigate("home"){
                    popUpTo(0)
                }
            })
        }
        composable("home"){
            HomeScreen(onClickLogout = {
                myNavController.navigate("login"){
                    popUpTo (0)
                }
            })
        }
    }
}