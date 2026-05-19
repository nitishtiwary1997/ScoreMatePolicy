package com.nitish.cricketscoringapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.nitish.cricketscoringapp.data.remote.UserSession
import com.nitish.cricketscoringapp.presentation.navigation.AppNavGraph
import com.nitish.cricketscoringapp.ui.theme.CricketScoringAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userSession: UserSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CricketScoringAppTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    startOnLogin = !userSession.isSignedIn
                )
            }
        }
    }
}
