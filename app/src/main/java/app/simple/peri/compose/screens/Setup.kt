package app.simple.peri.compose.screens

import DescriptionPreference
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.simple.peri.BuildConfig
import app.simple.peri.R
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.RequestDirectoryPermission
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.dialogs.settings.ShowWarningDialog
import app.simple.peri.compose.nav.Routes
import app.simple.peri.utils.PermissionUtils
import app.simple.peri.utils.PermissionUtils.isBatteryOptimizationDisabled
import app.simple.peri.utils.PermissionUtils.requestIgnoreBatteryOptimizations

@Composable
fun Setup(context: Context, navController: NavController? = null) {
    var showSetupIncompleteDialog by remember { mutableStateOf(false) }

    if (showSetupIncompleteDialog) {
        ShowWarningDialog(
                title = context.getString(R.string.setup),
                warning = context.getString(R.string.setup_incomplete),
                context = context,
                onDismiss = {
                    showSetupIncompleteDialog = false
                }
        )
    }

    Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(COMMON_PADDING)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopHeader(context.getString(R.string.setup),
                  modifier = Modifier.padding(COMMON_PADDING))

        Permissions(context = context, navController = navController, modifier = Modifier
            .padding(COMMON_PADDING)
            .wrapContentHeight())

        Folder(context = context, navController = navController, modifier = Modifier
            .padding(COMMON_PADDING)
            .weight(1F))

        Button(
                onClick = {
                    if (isSetupComplete(context)) {
                        navController?.navigate(Routes.HOME)
                    } else {
                        showSetupIncompleteDialog = true
                    }
                },
                modifier = Modifier
                    .padding(COMMON_PADDING)
                    .fillMaxWidth(),
        ) {
            Text(text = context.getString(R.string.continue_button),
                 fontWeight = FontWeight.Bold,
                 fontSize = 18.sp,
                 modifier = Modifier.padding(12.dp))
        }
    }
}

@Composable
fun Permissions(modifier: Modifier, context: Context, navController: NavController? = null) {
    var showExternalPermissionDialog by remember { mutableStateOf(false) }
    var showBatteryOptimizationDialog by remember { mutableStateOf(false) }
    var requestPermissionLauncher by remember { mutableStateOf(false) }

    if (showExternalPermissionDialog) {
        ShowWarningDialog(
                title = context.getString(R.string.external_storage),
                warning = context.getString(R.string.permission_granted),
                context = context,
                onDismiss = {
                    showExternalPermissionDialog = false
                }
        )
    }

    if (showBatteryOptimizationDialog) {
        ShowWarningDialog(
                title = context.getString(R.string.battery_optimization),
                warning = context.getString(R.string.permission_granted),
                context = context,
                onDismiss = {
                    showBatteryOptimizationDialog = false
                }
        )
    }

    if (requestPermissionLauncher) {
        RequestStoragePermissions()
    }

    Column(
            modifier = modifier
    ) {
        Text(
                text = "Permissions",
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                fontWeight = FontWeight.Bold
        )

        HorizontalDivider(
                modifier = Modifier.padding(bottom = COMMON_PADDING, start = COMMON_PADDING, end = COMMON_PADDING)
        )

        Card(
                onClick = {
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                            if (Environment.isExternalStorageManager()) {
                                showExternalPermissionDialog = true
                            } else {
                                try {
                                    val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                                    context.startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
                                } catch (ignored: ActivityNotFoundException) {

                                }
                            }
                        }

                        else -> {
                            if (PermissionUtils.checkStoragePermission(context)) {
                                requestPermissionLauncher = false
                                showExternalPermissionDialog = true
                            } else {
                                requestPermissionLauncher = true
                                showExternalPermissionDialog = false
                            }
                        }
                    }
                },
                colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                ),
        ) {
            PermissionText(
                    context.getString(R.string.external_storage),
                    context.getString(R.string.external_storage_summary))
        }

        Card(
                onClick = {
                    if (context.isBatteryOptimizationDisabled()) {
                        showBatteryOptimizationDialog = true
                    } else {
                        context.requestIgnoreBatteryOptimizations()
                    }
                },
                colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                ),
        ) {
            PermissionText(
                    context.getString(R.string.battery_optimization),
                    context.getString(R.string.battery_optimization_summary))
        }
    }
}

@Composable
fun Folder(modifier: Modifier, context: Context, navController: NavController? = null) {
    var launchDirectoryPermission by remember { mutableStateOf(false) }
    var showDirectoryPermissionDialog by remember { mutableStateOf(false) }

    if (launchDirectoryPermission) {
        RequestDirectoryPermission {
            showDirectoryPermissionDialog = false
            launchDirectoryPermission = false
        }
    }

    if (showDirectoryPermissionDialog) {
        ShowWarningDialog(
                title = context.getString(R.string.folder),
                warning = context.contentResolver.persistedUriPermissions.first().uri.toString(),
                context = context,
                onDismiss = {
                    showDirectoryPermissionDialog = false
                }
        )
    }

    Column(
            modifier = modifier
    ) {
        Text(
                text = context.getString(R.string.folder),
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                fontWeight = FontWeight.Bold
        )

        HorizontalDivider(
                modifier = Modifier.padding(bottom = COMMON_PADDING, start = COMMON_PADDING, end = COMMON_PADDING)
        )

        Card(
                onClick = {
                    if (context.contentResolver.persistedUriPermissions.isEmpty()) {
                        launchDirectoryPermission = true
                    } else {
                        showDirectoryPermissionDialog = true
                    }
                },
                colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                ),
        ) {
            Text(
                    text = context.getString(R.string.select_folder),
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(top = COMMON_PADDING, start = COMMON_PADDING, end = COMMON_PADDING)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.Medium
            )

            Text(
                    text = context.getString(R.string.select_folder_summary),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(bottom = COMMON_PADDING, start = COMMON_PADDING, end = COMMON_PADDING)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.Normal
            )
        }

        Row(
                verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surfaceTint,
                    modifier = Modifier
                        .padding(start = COMMON_PADDING, bottom = 8.dp)
                        .size(16.dp)
            )
            DescriptionPreference(description = context.getString(R.string.select_folder_info))
        }
    }
}

@Composable
fun PermissionText(text: String, summary: String = "") {
    Text(
            text = text,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(top = COMMON_PADDING, start = COMMON_PADDING, end = COMMON_PADDING)
                .fillMaxWidth(),
            fontWeight = FontWeight.Medium
    )

    Text(
            text = summary,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(bottom = COMMON_PADDING, start = COMMON_PADDING, end = COMMON_PADDING)
                .fillMaxWidth(),
            fontWeight = FontWeight.Normal
    )
}

@Composable
fun RequestStoragePermissions() {
    val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle the result of the permission request
        permissions.forEach { (permission, isGranted) ->
            if (isGranted) {
                Log.d("Permission", "$permission granted")
            } else {
                Log.d("Permission", "$permission denied")
            }
        }
    }

    // Ensure the launcher is initialized before launching the permission request
    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(
            arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }
}

fun isSetupComplete(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.contentResolver.persistedUriPermissions.isNotEmpty()
                && Environment.isExternalStorageManager()
                && context.isBatteryOptimizationDisabled()
    } else {
        context.contentResolver.persistedUriPermissions.isNotEmpty()
                && PermissionUtils.checkStoragePermission(context)
                && context.isBatteryOptimizationDisabled()
    }
}
