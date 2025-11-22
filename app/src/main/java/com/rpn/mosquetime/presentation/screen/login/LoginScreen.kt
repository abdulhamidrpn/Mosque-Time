package com.rpn.mosquetime.presentation.screen.login

import android.widget.Toast
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rpn.mosquetime.R
import com.rpn.mosquetime.presentation.common.TvButton
import com.rpn.mosquetime.presentation.theme.AppIcons
import com.rpn.mosquetime.presentation.theme.MosqueTimeTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> onNavigateToHome()
                is LoginEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LoginScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit
) {
    // Focus requesters for manual focus traversal
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val loginButtonFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
            label = { Text(stringResource(R.string.email)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(2 / 3f)
                .focusRequester(emailFocusRequester)
                .focusable(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            )
        )

        Spacer(modifier = Modifier.size(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
            label = { Text(stringResource(R.string.password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth(2 / 3f)
                .focusRequester(passwordFocusRequester)
                .focusable(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            keyboardActions = KeyboardActions(
                onDone = { loginButtonFocusRequester.requestFocus() }
            )
        )

        Spacer(modifier = Modifier.size(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(2 / 3f),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            TvButton(
                text = stringResource(R.string.back_to_home),
                icon = AppIcons.ArrowBack,
                onClick = { onEvent(LoginEvent.BackToHome) },
                modifier = Modifier.weight(1f)
            )
            TvButton(
                text = stringResource(R.string.login),
                icon = AppIcons.Login,
                onClick = { onEvent(LoginEvent.Submit) },
                modifier = Modifier.weight(1f)
            )
        }


        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Preview(device = "id:tv_1080p", showBackground = true)
@Composable
fun LoginScreenPreview() {
    MosqueTimeTheme {
        LoginScreen(
            state = LoginState(
                email = "abdulhamidrpn@gmail.com",
                password = "Hello12345",
                isLoading = false,
                error = null
            ),
            onEvent = {}
        )
    }
}