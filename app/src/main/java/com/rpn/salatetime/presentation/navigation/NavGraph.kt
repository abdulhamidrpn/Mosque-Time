package com.rpn.salatetime.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.rpn.salatetime.R
import com.rpn.salatetime.domain.model.NotificationTrigger
import com.rpn.salatetime.presentation.screen.components.DynamicImage
import com.rpn.salatetime.presentation.screen.login.LoginRoute
import com.rpn.salatetime.presentation.screen.main.MainRoute
import com.rpn.salatetime.presentation.screen.main.MainViewModel
import com.rpn.salatetime.presentation.screen.message.MessageRoute
import com.rpn.salatetime.presentation.screen.setting.SettingsRoute
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

// ─────────────────────────────────────────────────────────────────────────────
// Route hierarchy
// ─────────────────────────────────────────────────────────────────────────────

sealed class Route {
    data object Splash : Route()
    data object Login : Route()
    data object Home : Route()
    data object Settings : Route()
    data class Message(val notificationTrigger: NotificationTrigger) : Route()
}

// ─────────────────────────────────────────────────────────────────────────────
// NavGraph
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NavGraph(vm: MainViewModel = koinViewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    // Start on Splash unconditionally. The LaunchedEffect below replaces it
    // once the DataStore auth state is known (isAuthReady = true).
    val backStack = remember { mutableStateListOf<Route>(Route.Splash) }

    // ── Auth state observer ───────────────────────────────────────────────────
    //
    // Fires whenever isAuthReady or isLoggedIn changes.
    //
    // isAuthReady == false → DataStore hasn't emitted yet → stay on Splash.
    // isAuthReady == true  → first emission received:
    //   isLoggedIn  → replace entire stack with [Home]
    //   !isLoggedIn → replace entire stack with [Login]
    //
    // Using `replace entire stack` (not push) means:
    //   - After login  : back button at Home exits the app, not Login.
    //   - After logout : back button at Login exits the app, not Home.
    //
    LaunchedEffect(uiState.isAuthReady, uiState.isLoggedIn) {
        if (!uiState.isAuthReady) return@LaunchedEffect

        val target: Route = if (uiState.isLoggedIn) Route.Home else Route.Login
        val current = backStack.lastOrNull()

        // Only replace when the destination actually differs from what's on top,
        // to avoid wiping a partially-built stack (e.g. Home → Settings) on a
        // benign re-emission of the same auth state.
        if (current == target) return@LaunchedEffect
        if (current is Route.Splash || current is Route.Login || current is Route.Home) {
            Timber.d("Auth ready → navigating to $target (was $current)")
            Snapshot.withMutableSnapshot {
                backStack.clear()
                backStack.add(target)
            }
        }
    }

    // ── Back handler ──────────────────────────────────────────────────────────
    //
    // When the stack has exactly one entry (Login or Home), back presses are
    // consumed and ignored — the system back animation still plays on Android 14+
    // but the destination does not change.  This prevents the user from pressing
    // back from Home to reach Login, or from Login to reach nothing.
    //
    // For deeper screens (Settings, Message) the NavDisplay onBack handler pops
    // normally.
    //
    BackHandler(enabled = backStack.size <= 1) {
        // Intentionally no-op: consume the event so the system doesn't finish
        // the Activity while a route is displayed.
        Timber.d("Back consumed at root route: ${backStack.lastOrNull()}")
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        entryProvider = { route ->
            when (route) {

                // ── Splash ────────────────────────────────────────────────────
                is Route.Splash -> NavEntry(route) {
                    SplashScreen()
                }

                // ── Login ─────────────────────────────────────────────────────
                is Route.Login -> NavEntry(route) {
                    // Back at Login is handled by BackHandler above (no-op).
                    LoginRoute(
                        onNavigateToHome = {
                            // After a successful login the ViewModel writes
                            // userId to DataStore → settingsFlow emits → LaunchedEffect
                            // above redirects automatically.  The explicit push here is
                            // a safety net in case the observer fires slightly late.
                            Snapshot.withMutableSnapshot {
                                backStack.clear()
                                backStack.add(Route.Home)
                            }
                        }
                    )
                }

                // ── Home ──────────────────────────────────────────────────────
                is Route.Home -> NavEntry(route) {
                    // Back at Home is handled by BackHandler above (no-op).
                    MainRoute(
                        viewModel = vm,
                        onNavigateToMessage = { trigger ->
                            Timber.d("Navigate to Message: $trigger")
                            backStack.add(Route.Message(trigger))
                        },
                        onNavigateToSettings = {
                            backStack.add(Route.Settings)
                        }
                    )
                }

                // ── Message ───────────────────────────────────────────────────
                is Route.Message -> NavEntry(route) {
                    MessageRoute(
                        viewModel = vm,
                        notificationTrigger = route.notificationTrigger,
                        onBack = {
                            if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                        }
                    )
                }

                // ── Settings ──────────────────────────────────────────────────
                is Route.Settings -> NavEntry(route) {
                    SettingsRoute(
                        onBack = {
                            if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                        },
                        navigateToLogin = {
                            // navigateToLogin is a direct jump (e.g. "Go to login" link).
                            // Full stack replacement so back from Login doesn't return here.
                            Snapshot.withMutableSnapshot {
                                backStack.clear()
                                backStack.add(Route.Login)
                            }
                        },
                        onLogout = {
                            // 1. Tell the ViewModel to clear DataStore + stop Supabase sync.
                            //    This causes settingsFlow to emit userId = "" → LaunchedEffect
                            //    redirects to Login automatically.
                            vm.logout()

                            // 2. Eagerly replace the stack so the user sees Login immediately
                            //    without waiting for the DataStore write round-trip.
                            Snapshot.withMutableSnapshot {
                                backStack.clear()
                                backStack.add(Route.Login)
                            }
                        }
                    )
                }
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Splash screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        DynamicImage(
            source = R.drawable.logo,
            modifier = Modifier
                .heightIn(max = 300.dp)   // cap at 300dp tall
                .wrapContentWidth()        // width follows the image's natural aspect ratio
                .padding(16.dp),
        )
    }
}