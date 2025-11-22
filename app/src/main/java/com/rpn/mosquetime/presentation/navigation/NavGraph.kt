package com.rpn.mosquetime.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rpn.mosquetime.presentation.screen.login.LoginRoute
import com.rpn.mosquetime.presentation.screen.main.MainNotification
import com.rpn.mosquetime.presentation.screen.main.MainRoute
import com.rpn.mosquetime.presentation.screen.main.MainViewModel
import com.rpn.mosquetime.presentation.screen.message.MessageScreen
import com.rpn.mosquetime.presentation.screen.settings.SettingsScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController() // Manage NavController internally now
    val scope = rememberCoroutineScope()

    // Get current back stack entry to determine selected drawer item
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val vm: MainViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.effect.collect { e ->
//            if (false) (context as? Activity)?.finish()
        }
    }
    // Main content area - NavHost goes here

    NavHost(
        navController = navController, startDestination = Screen.Home.route,
        modifier = Modifier.fillMaxSize() // Ensure NavHost fills the space
    ) {
        composable(Screen.Login.route) {
            LoginRoute(
                onNavigateToHome = { navController.navigate(Screen.Home.route) }
            )

        }
        composable(Screen.Home.route) {
            MainRoute(
                viewModel = vm,
                onNavigateToMessage = { mainNotification ->
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "notification",
                        mainNotification
                    )
                    navController.navigate(Screen.Message.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Message.route) {

            val notification =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<MainNotification>("notification")
            MessageScreen(
                state = state,
                notification = notification,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onChange = {

                },
                navigateToLoginPage = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")

    object Message : Screen("message_screen")
    object Settings : Screen("settings_screen")
}