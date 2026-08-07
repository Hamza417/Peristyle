package app.simple.peri.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.ui.commons.SecondaryHeader
import app.simple.peri.ui.commons.SwitchPreference

@Composable
fun NotificationColumn() {
    SecondaryHeader(title = stringResource(R.string.notifications))

    SwitchPreference(
            title = stringResource(R.string.notifications),
            description = stringResource(R.string.notifications_summary),
            checked = MainComposePreferences.getAutoWallpaperNotification(),
    ) {
        MainComposePreferences.setAutoWallpaperNotification(it)
    }

    SwitchPreference(
            title = stringResource(R.string.delete_button),
            description = stringResource(R.string.delete_button_summary),
            checked = MainComposePreferences.isNotificationDeleteButtonEnabled(),
    ) {
        MainComposePreferences.setNotificationDeleteButtonEnabled(it)
    }
}