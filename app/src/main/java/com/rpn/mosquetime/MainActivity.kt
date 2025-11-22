package com.rpn.mosquetime

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rpn.mosquetime.presentation.navigation.NavGraph
import com.rpn.mosquetime.presentation.screen.settings.SettingsViewModel
import com.rpn.mosquetime.presentation.screen.settings.composable.Theme
import com.rpn.mosquetime.presentation.theme.MosqueTimeTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModel()

    // Dialog state remains
    private var showPermissionRationaleDialog by mutableStateOf(false)
    private var permissionsToRequestFromDialog: List<String> = emptyList() // Use List
    private var showSettingsDialog by mutableStateOf(false)

    // Flag to track if initial essential permissions are granted
    private var essentialPermissionsGranted by mutableStateOf(false)


    // --- Define Essential Permissions ---
    private val essentialPermissions = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    // --- Standard Permission Launcher ---
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
            // Check if essential permissions are now granted after this request
            checkEssentialPermissions() // Update the essentialPermissionsGranted state

            // Handle denial/permanent denial for the requested permissions
            val allRequestedGranted = permissionsResult.values.all { it }
            if (!allRequestedGranted) {
                handlePermissionDenial(permissionsResult)
            } else {
                println("Permissions Granted via launcher")
                // Essential permissions check will handle proceeding
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val uiState by settingsViewModel.uiState.collectAsState()
            val useDarkTheme = when (uiState.theme) {
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.SYSTEM -> isSystemInDarkTheme()
            }

            MosqueTimeTheme(useDarkTheme = useDarkTheme) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    requestEssentialPermissions()
                    NavGraph()
                }
            }
        }
    }


    // --- Permission Check/Request Logic ---
    fun requestEssentialPermissions() {
        requestPermissionLauncher.launch(essentialPermissions)
    }

    private fun checkEssentialPermissions() {
        val currentlyGranted = areEssentialPermissionsGranted()
        if (currentlyGranted != essentialPermissionsGranted) {
            println("Updating essentialPermissionsGranted state to: $currentlyGranted")
            essentialPermissionsGranted = currentlyGranted
            if (currentlyGranted) {
                // Trigger initial data load *once* when essential permissions become granted
                proceedWithInitialization()
            }
        }
    }

    private fun areEssentialPermissionsGranted(): Boolean {
        return essentialPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Handles request logic for standard permissions triggered from PermissionScreen
    private fun handleStandardPermissionRequest(permissionsToRequest: List<String>) {
        val permissionsNeededNow = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeededNow.isEmpty()) return // Already granted

        val showRationale = permissionsNeededNow.any {
            ActivityCompat.shouldShowRequestPermissionRationale(this, it) // Use ActivityCompat here
        }

        if (showRationale) {
            permissionsToRequestFromDialog = permissionsNeededNow
            showPermissionRationaleDialog = true
        } else {
            requestPermissionLauncher.launch(permissionsNeededNow.toTypedArray())
        }
    }

    // Handles result of standard permission request
    private fun handlePermissionDenial(permissionsResult: Map<String, Boolean>) {
        println("Permissions Denied/Granted: $permissionsResult")
        val permanentlyDenied = permissionsResult.entries.any { (perm, granted) ->
            !granted && !ActivityCompat.shouldShowRequestPermissionRationale(this, perm)
        }
        if (permanentlyDenied) {
            println("Permissions Permanently Denied")
            showSettingsDialog = true
        } else {
            showPermissionDeniedMessage("Some permissions were denied. Certain features might be limited.")
        }
    }


    /** Logic to run AFTER essential permissions are granted */
    private fun proceedWithInitialization() {
        println("Proceeding with initialization (essential permissions granted).")
    }


    // --- Other Helper/Lifecycle Methods ---
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun showPermissionDeniedMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // TODO: Optionally disable UI features that require the permissions
    }

}

@Composable
fun PermissionRationaleDialog(
    permissions: List<String>, // Receive the specific permissions needing rationale
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // ... (Dialog Implementation as shown in the previous step, using the 'permissions' list to build text) ...

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissions Required") },
        text = {
            Text(
                "To play audio from your device and show playback controls in notifications, " +
                        "this app needs access to your audio files and permission to post notifications. " +
                        "Please grant these permissions."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Grant")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SettingsRedirectDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissions Required") },
        text = {
            Text(
                "Audio and/or Notification permissions were permanently denied. " +
                        "To enable audio playback features, please grant the required permissions " +
                        "in the app settings."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
