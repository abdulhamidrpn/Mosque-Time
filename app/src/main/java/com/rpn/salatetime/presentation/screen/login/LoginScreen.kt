package com.rpn.salatetime.presentation.screen.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Route
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome ->
                    onNavigateToHome()

                is LoginEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Long
                    )
            }
        }
    }

    LoginScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    state: LoginState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onEvent: (LoginEvent) -> Unit
) {
    val isTv = LocalConfiguration.current.screenWidthDp >= 600
    val cardWidth: Dp = if (isTv) 480.dp else Dp.Unspecified
    val outerPad: Dp = if (isTv) 48.dp else 24.dp
    val titleSize = if (isTv) 32.sp else 26.sp
    val subtitleSize = if (isTv) 18.sp else 14.sp
    val buttonHeight = if (isTv) 60.dp else 52.dp
    val spacing = if (isTv) 20.dp else 16.dp

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    actionColor = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = outerPad, vertical = 12.dp)
                )
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            // ── Animated card ─────────────────────────────────────────────────
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = cardWidth)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = outerPad, vertical = outerPad),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    // ── Header ────────────────────────────────────────────────
                    Text(
                        text = "Salat Time",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = titleSize),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Sign in to manage your mosque",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = subtitleSize),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(if (isTv) 12.dp else 8.dp))

                    // ── Fields ────────────────────────────────────────────────
                    LoginFields(
                        state = state,
                        isTv = isTv,
                        onEvent = onEvent
                    )

                    Spacer(Modifier.height(if (isTv) 8.dp else 4.dp))

                    // ── Submit button ─────────────────────────────────────────
                    Button(
                        onClick = { onEvent(LoginEvent.Submit) },
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = if (isTv) 18.sp else 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fields
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoginFields(
    state: LoginState,
    isTv: Boolean,
    onEvent: (LoginEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val fieldShape = RoundedCornerShape(12.dp)
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = if (isTv) 18.sp else 16.sp
    )
    val labelStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = if (isTv) 16.sp else 14.sp
    )

    // ── Email ─────────────────────────────────────────────────────────────────
    OutlinedTextField(
        value = state.email,
        onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
        label = { Text("Email", style = labelStyle) },
        placeholder = { Text("you@example.com", style = labelStyle) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        textStyle = textStyle,
        shape = fieldShape,
        modifier = Modifier.fillMaxWidth()
    )

    // ── Password ──────────────────────────────────────────────────────────────
    OutlinedTextField(
        value = state.password,
        onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
        label = { Text("Password", style = labelStyle) },
        placeholder = { Text("Your password", style = labelStyle) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            IconButton(onClick = { onEvent(LoginEvent.TogglePasswordVisibility) }) {
                Icon(
                    imageVector = if (state.showPassword)
                        Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (state.showPassword)
                        "Hide password" else "Show password",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        visualTransformation = if (state.showPassword)
            VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onEvent(LoginEvent.Submit)
            }
        ),
        textStyle = textStyle,
        shape = fieldShape,
        modifier = Modifier.fillMaxWidth()
    )
}