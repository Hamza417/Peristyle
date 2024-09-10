package app.simple.peri.compose.screens

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.simple.peri.BuildConfig
import app.simple.peri.R
import app.simple.peri.compose.dialogs.ShowWarningDialog
import app.simple.peri.utils.PermissionUtils
import app.simple.peri.utils.PermissionUtils.isBatteryOptimizationDisabled
import app.simple.peri.utils.PermissionUtils.requestIgnoreBatteryOptimizations

private val commonPadding = 16.dp

@Composable
fun Setup(context: Context, navController: NavController? = null) {
    Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(commonPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopHeader(context.getString(R.string.setup),
                  modifier = Modifier.padding(commonPadding))

        Permissions(context = context, navController = navController, modifier = Modifier
            .padding(commonPadding)
            .wrapContentHeight())

        Folder(context = context, navController = navController, modifier = Modifier
            .padding(commonPadding)
            .weight(1F))
    }
}

@Composable
fun TopHeader(title: String, modifier: Modifier = Modifier) {
    Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = title,
                textAlign = TextAlign.Start,
                fontSize = 32.sp, // Set the font size
                modifier = Modifier.weight(1f), // Set the weight
                fontWeight = FontWeight.Bold, // Make the text bold
        )
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
                modifier = Modifier.padding(bottom = commonPadding, start = commonPadding, end = commonPadding)
        )

        Card(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            showExternalPermissionDialog = true
                        } else {
                            try {
                                val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                                context.startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
                            } catch (ignored: ActivityNotFoundException) {

                            }
                        }
                    } else {
                        if (PermissionUtils.checkStoragePermission(context)) {
                            requestPermissionLauncher = false
                            showExternalPermissionDialog = true
                        } else {
                            requestPermissionLauncher = true
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
                modifier = Modifier.padding(bottom = commonPadding, start = commonPadding, end = commonPadding)
        )

        Card(
                onClick = {

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
fun PermissionText(text: String, summary: String = "") {
    Text(
            text = text,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(top = commonPadding, start = commonPadding, end = commonPadding)
                .fillMaxWidth(),
            fontWeight = FontWeight.Medium
    )

    Text(
            text = summary,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(bottom = commonPadding, start = commonPadding, end = commonPadding)
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

    // Launch the permission request
    requestPermissionLauncher.launch(
            arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
    )
}
