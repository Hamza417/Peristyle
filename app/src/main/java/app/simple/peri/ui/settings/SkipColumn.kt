package app.simple.peri.ui.settings

import DescriptionPreference
import SecondaryHeader
import SwitchPreference
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences

@Composable
fun SkipColumn() {
    SecondaryHeader(title = stringResource(R.string.skip))
    DescriptionPreference(description = stringResource(R.string.skip_when_condition_summary))

    SwitchPreference(
            title = stringResource(R.string.skip_when_portrait),
            checked = MainComposePreferences.getDontChangeWhenPortrait(),
            topPadding = 4.dp
    ) {
        MainComposePreferences.setDontChangeWhenPortrait(it)
    }

    SwitchPreference(
            title = stringResource(R.string.skip_when_landscape),
            checked = MainComposePreferences.getDontChangeWhenLandscape(),
            topPadding = 4.dp
    ) {
        MainComposePreferences.setDontChangeWhenLandscape(it)
    }
}