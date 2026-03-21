package app.simple.peri.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.BuildConfig
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.ui.commons.COMMON_PADDING
import app.simple.peri.ui.commons.ClickablePreference
import app.simple.peri.ui.commons.DescriptionPreference
import app.simple.peri.ui.commons.FolderBrowser
import app.simple.peri.ui.commons.InitDisplayDimension
import app.simple.peri.ui.commons.SecondaryHeader
import app.simple.peri.ui.commons.TopHeader
import app.simple.peri.ui.dialogs.common.ShowWarningDialog
import app.simple.peri.ui.nav.Routes
import app.simple.peri.ui.theme.LocalBarsSize
import app.simple.peri.utils.PermissionUtils
import app.simple.peri.utils.PermissionUtils.isBatteryOptimizationDisabled
import app.simple.peri.utils.PermissionUtils.requestIgnoreBatteryOptimizations
import app.simple.peri.viewmodels.ComposeWallpaperViewModel

@Composable
fun Setup(context: Context, navController: NavController? = null) {
    InitDisplayDimension()

    var showSetupIncompleteDialog by remember { mutableStateOf(false) }

    if (showSetupIncompleteDialog) {
        ShowWarningDialog(
                title = context.getString(R.string.setup),
                warning = context.getString(R.string.setup_incomplete),
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
                        top = COMMON_PADDING + LocalBarsSize.current.statusBarHeight,
                        start = COMMON_PADDING,
                        end = COMMON_PADDING,
                        bottom = 8.dp + LocalBarsSize.current.navigationBarHeight),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                TopHeader(title = context.getString(R.string.setup),
                          modifier = Modifier.padding(COMMON_PADDING),
                          isHideSettings = true,
                          isHideAutoWallpaper = true)

                Permissions(context = context, navController = navController, modifier = Modifier
                    .wrapContentHeight())

                Folder(context = context, navController = navController, modifier = Modifier)
            }
        }

        HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
        )

        Button(
                onClick = {
                    if (isSetupComplete(context)) {
                        navController?.navigate(Routes.HOME) {
                            popUpTo(Routes.SETUP) {
                                inclusive = true
                            }
                        }
                    } else {
                        showSetupIncompleteDialog = true
                    }
                },
                modifier = Modifier
                    .padding(
                            top = COMMON_PADDING,
                            start = COMMON_PADDING,
                            end = COMMON_PADDING,
                            bottom = COMMON_PADDING + LocalBarsSize.current.navigationBarHeight
                    )
                    .fillMaxWidth(),
        ) {
            Text(text = context.getString(R.string.continue_button),
                 fontWeight = FontWeight.Bold,
                 fontSize = 18.sp,
                 modifier = Modifier
                     .padding(12.dp)
            )
        }
    }
}

@Composable
fun Permissions(modifier: Modifier, context: Context, navController: NavController? = null) {
    var showExternalPermissionDialog by remember { mutableStateOf(false) }
    var showBatteryOptimizationDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var requestPermissionLauncher by remember { mutableStateOf(false) }
    var requestMediaImages by remember { mutableStateOf(false) }
    var requestNotificationPermission by remember { mutableStateOf(false) }

    var storageGranted by remember {
        mutableStateOf(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager()
                else PermissionUtils.checkStoragePermission(context)
        )
    }
    var batteryGranted by remember { mutableStateOf(context.isBatteryOptimizationDisabled()) }
    var mediaGranted by remember {
        mutableStateOf(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) PermissionUtils.checkMediaImagesPermission(context)
                else true
        )
    }
    var notificationGranted by remember {
        mutableStateOf(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) NotificationManagerCompat.from(context).areNotificationsEnabled()
                else true
        )
    }

    // Re-check all permission states when the activity resumes from system settings
    val activity = LocalActivity.current
    val lifecycleOwner = activity as? LifecycleOwner
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager()
                else PermissionUtils.checkStoragePermission(context)
                batteryGranted = context.isBatteryOptimizationDisabled()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mediaGranted = PermissionUtils.checkMediaImagesPermission(context)
                    notificationGranted = NotificationManagerCompat.from(context).areNotificationsEnabled()
                }
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose { lifecycleOwner?.lifecycle?.removeObserver(observer) }
    }

    if (showExternalPermissionDialog) {
        ShowWarningDialog(
                title = context.getString(R.string.external_storage),
                warning = context.getString(R.string.permission_granted),
                onDismiss = {
                    showExternalPermissionDialog = false
                }
        )
    }

    if (showBatteryOptimizationDialog) {
        ShowWarningDialog(
                title = context.getString(R.string.battery_optimization),
                warning = context.getString(R.string.permission_granted),
                onDismiss = {
                    showBatteryOptimizationDialog = false
                }
        )
    }

    if (showNotificationDialog) {
        ShowWarningDialog(
                title = context.getString(R.string.notifications),
                warning = context.getString(R.string.permission_granted),
                onDismiss = {
                    showNotificationDialog = false
                }
        )
    }

    if (requestPermissionLauncher) {
        RequestStoragePermissions { granted ->
            requestPermissionLauncher = false
            if (granted) storageGranted = true
        }
    }

    if (requestNotificationPermission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RequestNotificationPermission { granted ->
                requestNotificationPermission = false
                if (granted) notificationGranted = true
            }
        }
    }

    if (requestMediaImages) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RequestReadMediaImagesPermission { granted ->
                requestMediaImages = false
                if (granted) mediaGranted = true
            }
        }
    }

    Column(
            modifier = modifier
    ) {
        SecondaryHeader(title = stringResource(R.string.permissions))

        ClickablePreference(
                title = context.getString(R.string.external_storage),
                description = context.getString(R.string.external_storage_summary),
                statusText = if (storageGranted) context.getString(R.string.permission_granted)
                else context.getString(R.string.permission_not_granted),
        ) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    if (Environment.isExternalStorageManager()) {
                        storageGranted = true
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
                        storageGranted = true
                        requestPermissionLauncher = false
                        showExternalPermissionDialog = true
                    } else {
                        requestPermissionLauncher = true
                        showExternalPermissionDialog = false
                    }
                }
            }
        }

        ClickablePreference(
                title = context.getString(R.string.battery_optimization),
                description = context.getString(R.string.battery_optimization_summary),
                statusText = if (batteryGranted) context.getString(R.string.permission_granted)
                else context.getString(R.string.permission_not_granted),
                onClick = {
                    if (context.isBatteryOptimizationDisabled()) {
                        batteryGranted = true
                        showBatteryOptimizationDialog = true
                    } else {
                        context.requestIgnoreBatteryOptimizations()
                    }
                }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ClickablePreference(
                    title = context.getString(R.string.allow_media_access),
                    description = context.getString(R.string.external_storage_summary),
                    statusText = if (mediaGranted) context.getString(R.string.permission_granted)
                    else context.getString(R.string.permission_not_granted),
                    onClick = {
                        if (PermissionUtils.checkMediaImagesPermission(context)) {
                            mediaGranted = true
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ClickablePreference(
                    title = context.getString(R.string.notifications),
                    description = context.getString(R.string.notifications_summary),
                    statusText = if (notificationGranted) context.getString(R.string.permission_granted)
                    else context.getString(R.string.permission_not_granted),
                    onClick = {
                        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                            notificationGranted = true
                            showNotificationDialog = true
                        } else {
                            requestNotificationPermission = true
                        }
                    }
            )
        }
    }
}

@Composable
fun Folder(modifier: Modifier, context: Context, navController: NavController? = null) {
    var launchDirectoryPermission by remember { mutableStateOf(false) }
    var showDirectoryPermissionDialog by remember { mutableStateOf(false) }
    val composeWallpaperViewModel: ComposeWallpaperViewModel = viewModel(LocalActivity.current as ComponentActivity)
    var directories by remember { mutableStateOf(MainComposePreferences.getAllowedPaths().joinToString("\n")) }

    if (launchDirectoryPermission) {
        FolderBrowser(
                onCancel = {
                    launchDirectoryPermission = false
                },
                onStorageGranted = {
                    launchDirectoryPermission = false
                    showDirectoryPermissionDialog = true
                    MainComposePreferences.addWallpaperPath(it)
                    directories = MainComposePreferences.getAllowedPaths().joinToString("\n")
                    composeWallpaperViewModel.refresh()
                }
        )
    }

    if (showDirectoryPermissionDialog) {
        ShowWarningDialog(
                title = context.getString(R.string.folder),
                warning = MainComposePreferences.getAllowedPaths().joinToString("\n"),
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
                    launchDirectoryPermission = true
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
fun RequestStoragePermissions(onResult: (Boolean) -> Unit = {}) {
    val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        Log.d("Permission", "READ_EXTERNAL_STORAGE ${if (granted) "granted" else "denied"}")
        onResult(granted)
    }

    LaunchedEffect(Unit) {
        launcher.launch(
                arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RequestNotificationPermission(onResult: (Boolean) -> Unit = {}) {
    val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("Permission", "POST_NOTIFICATIONS ${if (isGranted) "granted" else "denied"}")
        onResult(isGranted)
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RequestReadMediaImagesPermission(onResult: (Boolean) -> Unit = {}) {
    val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("Permission", "READ_MEDIA_IMAGES ${if (isGranted) "granted" else "denied"}")
        onResult(isGranted)
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.READ_MEDIA_IMAGES)
    }
}

fun isSetupComplete(context: Context): Boolean {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            MainComposePreferences.getAllowedPaths().isNotEmpty()
                    && Environment.isExternalStorageManager()
                    && context.isBatteryOptimizationDisabled()
                    && PermissionUtils.checkMediaImagesPermission(context)
                    && PermissionUtils.checkNotificationPermission(context)
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            MainComposePreferences.getAllowedPaths().isNotEmpty()
                    && Environment.isExternalStorageManager()
                    && context.isBatteryOptimizationDisabled()
                    && PermissionUtils.checkStoragePermission(context)
        }

        else -> {
            MainComposePreferences.getAllowedPaths().isNotEmpty()
                    && PermissionUtils.checkStoragePermission(context)
                    && context.isBatteryOptimizationDisabled()
        }
    }
}
