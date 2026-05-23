package com.nitish.cricketscoringapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.nitish.cricketscoringapp.ui.theme.EmeraldPrimary
import com.nitish.cricketscoringapp.ui.theme.GoldPrimary
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val PREFS_NAME  = "app_prefs"
private const val KEY_IS_DARK = "is_dark_theme"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userSession: UserSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs      = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val initialDark = prefs.getBoolean(KEY_IS_DARK, true)

        setContent {
            var isDark by remember { mutableStateOf(initialDark) }

            CricketScoringAppTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                val currentEntry  by navController.currentBackStackEntryAsState()
                val currentRoute  = currentEntry?.destination?.route

                val hideOverlay = currentRoute == Screen.Login.route

                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        navController = navController,
                        startOnLogin  = !userSession.isSignedIn
                    )

                    AnimatedVisibility(
                        visible = !hideOverlay,
                        enter   = fadeIn(),
                        exit    = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 80.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Theme toggle FAB
                            SmallFloatingActionButton(
                                onClick = {
                                    isDark = !isDark
                                    prefs.edit().putBoolean(KEY_IS_DARK, isDark).apply()
                                },
                                containerColor = if (isDark) Color(0xFF1A2230) else Color(0xFFFFFFFF),
                                contentColor   = if (isDark) GoldPrimary else Color(0xFF546E8A)
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Cricket Rules FAB
                            if (currentRoute != Screen.CricketRules.route) {
                                SmallFloatingActionButton(
                                    onClick = { navController.navigate(Screen.CricketRules.route) },
                                    modifier = Modifier.padding(top = 8.dp),
                                    containerColor = if (isDark) Color(0xFF1A2230) else Color(0xFFFFFFFF),
                                    contentColor   = GoldPrimary
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = "Cricket Rules",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
