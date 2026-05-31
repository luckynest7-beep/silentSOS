package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.MainViewModel

enum class NavigationScreen {
    CALCULATOR,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) { // Disguised perfectly with Dark Slate
                var currentScreen by remember { mutableStateOf(NavigationScreen.CALCULATOR) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Crossfade(
                        targetState = currentScreen,
                        modifier = Modifier.padding(innerPadding),
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            NavigationScreen.CALCULATOR -> {
                                CalculatorScreen(
                                    viewModel = viewModel,
                                    onSettingsClick = {
                                        currentScreen = NavigationScreen.SETTINGS
                                    }
                                )
                            }
                            NavigationScreen.SETTINGS -> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBackToCalc = {
                                        currentScreen = NavigationScreen.CALCULATOR
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
