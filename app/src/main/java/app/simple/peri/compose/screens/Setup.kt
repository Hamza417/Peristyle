package app.simple.peri.compose.screens

import ClickablePreference
import DescriptionPreference
import SecondaryHeader
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
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import app.simple.peri.BuildConfig
import app.simple.peri.R
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.RequestDirectoryPermission
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.dialogs.common.ShowWarningDialog
import app.simple.peri.compose.nav.Routes
import app.simple.peri.utils.PermissionUtils
import app.simple.peri.utils.PermissionUtils.isBatteryOptimizationDisabled
import app.simple.peri.utils.PermissionUtils.requestIgnoreBatteryOptimizations

@Composable
fun Setup(context: Context, navController: NavController? = null) {
    var showSetupIncompleteDialog by remember { mutableStateOf(false) }
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }

    statusBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.statusBars()).top
    navigationBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    displayDimension.width = LocalView.current.width
    displayDimension.height = LocalView.current.height

    val statusBarHeightPx = statusBarHeight
    val statusBarHeightDp = with(LocalDensity.current) { statusBarHeightPx.toDp() }
    val navigationBarHeightPx = navigationBarHeight
    val navigationBarHeightDp = with(LocalDensity.current) { navigationBarHeightPx.toDp() }
    val topPadding = 8.dp + statusBarHeightDp
    val bottomPadding = 8.dp + navigationBarHeightDp

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
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1F),
                contentPadding = PaddingValues(
                        top = topPadding,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = bottomPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                TopHeader(context.getString(R.string.setup),
                          modifier = Modifier.padding(COMMON_PADDING), isSettings = true)

                Permissions(context = context, navController = navController, modifier = Modifier
                    .wrapContentHeight())

                Folder(context = context, navController = navController, modifier = Modifier
                )
            }
        }

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
    var requestMediaImages by remember { mutableStateOf(false) }

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

    if (requestMediaImages) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RequestReadMediaImagesPermission {
                requestMediaImages = false
                showExternalPermissionDialog = false
            }
        }
    }

    Column(
            modifier = modifier
    ) {
        SecondaryHeader(title = stringResource(R.string.permissions))

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ClickablePreference(
                    title = context.getString(R.string.external_storage),
                    description = context.getString(R.string.external_storage_summary),
            ) {
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
            }
        }

        ClickablePreference(
                title = context.getString(R.string.battery_optimization),
                description = context.getString(R.string.battery_optimization_summary),
                onClick = {
                    if (context.isBatteryOptimizationDisabled()) {
                        showBatteryOptimizationDialog = true
                    } else {
                        context.requestIgnoreBatteryOptimizations()
                    }
                }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ClickablePreference(
                    title = context.getString(R.string.allow_media_access),
                    description = context.getString(R.string.external_storage_summary),
                    onClick = {
                        if (PermissionUtils.checkMediaImagesPermission(context)) {
                            requestMediaImages = false
                            showExternalPermissionDialog = true
                        } else {
                            requestMediaImages = true
                            showExternalPermissionDialog = false
                        }
                    }
            )

            Row(
                    verticalAlignment = Alignment.Top,
            ) {
                Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surfaceTint,
                        modifier = Modifier
                            .padding(start = COMMON_PADDING, bottom = 8.dp, top = 12.dp)
                            .size(16.dp)
                )
                DescriptionPreference(description = context.getString(R.string.allow_media_access_info))
            }
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
        SecondaryHeader(title = context.getString(R.string.folder))

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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RequestReadMediaImagesPermission(onCancel: () -> Unit) {
    val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle the result of the permission request
        if (isGranted) {
            Log.d("Permission", "READ_EXTERNAL_STORAGE granted")
        } else {
            Log.d("Permission", "READ_EXTERNAL_STORAGE denied")
            onCancel()
        }
    }

    // Ensure the launcher is initialized before launching the permission request
    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
    }
}

fun isSetupComplete(context: Context): Boolean {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM -> {
            context.contentResolver.persistedUriPermissions.isNotEmpty()
                    && context.isBatteryOptimizationDisabled()
                    && PermissionUtils.checkMediaImagesPermission(context)
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            context.contentResolver.persistedUriPermissions.isNotEmpty()
                    && Environment.isExternalStorageManager()
                    && context.isBatteryOptimizationDisabled()
        }

        else -> {
            context.contentResolver.persistedUriPermissions.isNotEmpty()
                    && PermissionUtils.checkStoragePermission(context)
                    && context.isBatteryOptimizationDisabled()
        }
    }
}
