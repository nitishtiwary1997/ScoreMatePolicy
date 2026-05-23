package com.nitish.cricketscoringapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nitish.cricketscoringapp.data.remote.UserSession
import com.nitish.cricketscoringapp.presentation.navigation.AppNavGraph
import com.nitish.cricketscoringapp.presentation.navigation.Screen
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
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        navController = navController,
                        startOnLogin = !userSession.isSignedIn
                    )

                    val hideRulesButton = currentRoute == Screen.CricketRules.route ||
                            currentRoute == Screen.Login.route
                    if (!hideRulesButton) {
                        SmallFloatingActionButton(
                            onClick = { navController.navigate(Screen.CricketRules.route) },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 80.dp),
                            containerColor = Color(0xFF1A2230),
                            contentColor = Color(0xFFFFD740)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = "Cricket Rules"
                            )
                        }
                    }
                }
            }
        }
    }
}
