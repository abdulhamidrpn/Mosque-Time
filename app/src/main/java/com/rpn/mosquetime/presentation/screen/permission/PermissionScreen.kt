// ui/screens/PermissionScreen.kt
package com.rpn.mosquetime.presentation.screen.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rpn.mosquetime.R
import com.rpn.mosquetime.presentation.screen.permission.components.PermissionCard
import com.rpn.mosquetime.presentation.screen.permission.components.PermissionInfo
import com.rpn.mosquetime.presentation.theme.AppIcons
import com.rpn.mosquetime.presentation.theme.MosqueTimeTheme

// Unique identifiers for special permissions
const val WRITE_SETTINGS_PERMISSION = "android.permission.WRITE_SETTINGS"
const val SCHEDULE_ALARM_PERMISSION = "android.permission.SCHEDULE_EXACT_ALARM"

@Composable
fun PermissionScreen(
    // Request callback now takes only the *standard* permissions to request via launcher
    onRequestStandardPermissions: (List<String>) -> Unit,
    // Callback when user clicks "Let's Go" after essential permissions are granted
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    // Local state to track permission status, refreshed on resume or after granting
    var permissionStatus by remember { mutableStateOf(checkAllPermissions(context)) }

    // Launcher for Write Settings Intent
    val writeSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Re-check status after returning from settings
        permissionStatus = checkAllPermissions(context)
    }

    // Launcher for Schedule Exact Alarm Intent
    val scheduleAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        permissionStatus = checkAllPermissions(context)
    }

    // This effect will re-check permissions when the screen becomes active again
    // (e.g., returning from settings). Requires Lifecycle dependency:
    // implementation "androidx.lifecycle:lifecycle-runtime-compose:2.7.0"
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                println("PermissionScreen Resumed: Re-checking permissions")
                permissionStatus = checkAllPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // If not using lifecycle observer, rely on MainActivity's onStart/onResume check

    // Define all permissions required by the app
    val allPermissionsInfo = remember {
        buildList {
            // Essential Permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(
                    PermissionInfo(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        context.getString(R.string.media_audio_access),
                        context.getString(R.string.media_audio_access_description),
                        Icons.Filled.AudioFile
                    )
                )
                add(
                    PermissionInfo(
                        Manifest.permission.POST_NOTIFICATIONS,
                        context.getString(R.string.post_notifications),
                        context.getString(R.string.post_notifications_description),
                        Icons.Filled.Notifications
                    )
                )
            } else {
                add(
                    PermissionInfo(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        context.getString(R.string.storage_access),
                        context.getString(R.string.storage_access_description),
                        Icons.Filled.Storage
                    )
                )
            }

            // Optional Permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(
                    PermissionInfo(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        context.getString(R.string.nearby_devices),
                        context.getString(R.string.nearby_devices_description),
                        Icons.Filled.BluetoothConnected,
                        isEssential = false
                    )
                )
                add(
                    PermissionInfo(
                        SCHEDULE_ALARM_PERMISSION,
                        context.getString(R.string.alarms_and_reminders),
                        context.getString(R.string.alarms_and_reminders_description),
                        Icons.Filled.Alarm,
                        isEssential = false,
                        isStandardPermission = false
                    )
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                add(
                    PermissionInfo(
                        WRITE_SETTINGS_PERMISSION,
                        context.getString(R.string.modify_system_settings),
                        context.getString(R.string.modify_system_settings_description),
                        Icons.Filled.RingVolume,
                        isEssential = false,
                        isStandardPermission = false
                    )
                )
            }
        }
    }

    // Calculate if all *essential* permissions are granted
    val allEssentialGranted = remember(permissionStatus, allPermissionsInfo) {
        allPermissionsInfo.all { info ->
            !info.isEssential || permissionStatus[info.permission] == true
        }
    }

    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(bottom = 80.dp) // Padding for the bottom button area
            .focusRequester(focusRequester)
    ) {
        // Header (Keep as before)
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.welcome_to_app, stringResource(R.string.app_name)))
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.permission_prompt),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

        // Permission List
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            allPermissionsInfo.forEach { permInfo ->
                val isGranted = permissionStatus[permInfo.permission] ?: false
                PermissionCard(
                    permissionInfo = permInfo,
                    isGranted = isGranted,
                    onGrantClick = {
                        if (permInfo.isStandardPermission) {
                            // Request standard permission via Activity's launcher
                            onRequestStandardPermissions(listOf(permInfo.permission))
                        } else {
                            // Launch Intent for special permissions
                            when (permInfo.permission) {
                                WRITE_SETTINGS_PERMISSION -> {
                                    val intent =
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                        } else {
                                            TODO("VERSION.SDK_INT < M")
                                        }
                                    intent.data = ("package:" + context.packageName).toUri()
                                    // Need Activity context to launch, or use launcher
                                    try {
                                        writeSettingsLauncher.launch(intent)
                                    } catch (e: Exception) { /* Handle */
                                    }
                                }

                                SCHEDULE_ALARM_PERMISSION -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val intent =
                                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        try {
                                            scheduleAlarmLauncher.launch(intent)
                                        } catch (e: Exception) { /* Handle */
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    } // End Main Column

    // Bottom Button
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = onFinish,
            enabled = allEssentialGranted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.continue_button), style = MaterialTheme.typography.titleMedium)
        }
    }
}


// Helper function to check all relevant permissions
private fun checkAllPermissions(context: Context): Map<String, Boolean> {
    val permissions = mutableListOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.BLUETOOTH_CONNECT,
        WRITE_SETTINGS_PERMISSION,
        SCHEDULE_ALARM_PERMISSION
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.remove(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
    }

    return permissions.associateWith { context.hasPermission(it) }
}

// Extension function for checking standard permissions
fun Context.hasPermission(permission: String): Boolean {
    if (permission == WRITE_SETTINGS_PERMISSION) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(this)
        }
        return true
    }
    if (permission == SCHEDULE_ALARM_PERMISSION) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}


// --- Preview Function ---
@Preview(showBackground = true, name = "Permission Screen Light")
@Composable
fun PermissionScreenPreviewLight() {
    MosqueTimeTheme(useDarkTheme = false) {
        PermissionScreen(
            onRequestStandardPermissions = { println("Preview Request: $it") },
            onFinish = { println("Preview Finish") }
        )
    }
}

@Preview(showBackground = true, name = "Permission Screen Dark")
@Composable
fun PermissionScreenPreviewDark() {
    MosqueTimeTheme(useDarkTheme = true) {
        PermissionScreen(
            onRequestStandardPermissions = { println("Preview Request: $it") },
            onFinish = { println("Preview Finish") }
        )
    }
}