package com.hima.ai.presentation.report.newreport

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.HimaIconButton
import com.hima.ai.core.designsystem.component.HimaPrimaryButton
import com.hima.ai.core.designsystem.component.HimaTextArea
import com.hima.ai.core.designsystem.component.HimaTextField
import com.hima.ai.core.designsystem.component.HimaTextLink
import com.hima.ai.core.designsystem.component.ImagePickerCard
import com.hima.ai.core.designsystem.component.ScreenHeader
import com.hima.ai.core.designsystem.component.SelectedImageCard
import com.hima.ai.core.designsystem.component.StepIndicator
import com.hima.ai.core.designsystem.theme.HimaRadius
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.core.location.SaudiPlaces
import com.hima.ai.core.location.hasLocationPermission
import com.hima.ai.data.mock.ManualLocation

/**
 * New report — attach evidence, confirm the auto-detected location, add an
 * optional note, then hand off to the AI. The primary action stays disabled
 * until a photo exists, so the ranger can't reach a dead end.
 */
@Composable
fun NewReportScreen(
    onBackClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewReportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHimaColors.current
    val context = LocalContext.current
    var locationPermissionDenied by rememberSaveable { mutableStateOf(false) }
    var showLocationPicker by rememberSaveable { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        if (context.hasLocationPermission()) {
            locationPermissionDenied = false
            onAnalyzeClick()
        } else {
            locationPermissionDenied = true
        }
    }
    // A hand-picked location makes the GPS permission irrelevant for this
    // report, so don't prompt for something the submission won't use.
    val hasManualLocation = uiState.manualLocation != null
    val analyzeWithLocation = remember(context, onAnalyzeClick, hasManualLocation) {
        {
            if (hasManualLocation || context.hasLocationPermission()) {
                onAnalyzeClick()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Spacer(Modifier.height(50.dp))
        ScreenHeader(
            title = stringResource(R.string.new_report_title),
            onBackClick = onBackClick,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            StepIndicator(
                currentStep = uiState.currentStep,
                modifier = Modifier.padding(top = 6.dp, bottom = 26.dp),
            )

            Text(
                text = stringResource(R.string.new_report_step_attach),
                style = HimaTextStyles.t,
                color = colors.ink,
                modifier = Modifier.padding(bottom = 11.dp),
            )

            val imageUri = uiState.imageUri
            if (imageUri == null) {
                ImagePickerCard(
                    onCaptureClick = onCaptureClick,
                    onGalleryClick = onGalleryClick,
                )
            } else {
                SelectedImageCard(imageUri = imageUri, onChangeClick = viewModel::onClearImage)
            }

            Text(
                text = stringResource(R.string.new_report_location),
                style = HimaTextStyles.t,
                color = colors.ink,
                modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
            )
            LocationField(
                manualLocation = uiState.manualLocation,
                onEditClick = { showLocationPicker = true },
            )

            Row(
                modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.new_report_description),
                    style = HimaTextStyles.t,
                    color = colors.ink,
                )
                Text(
                    text = stringResource(R.string.new_report_description_optional),
                    style = HimaTextStyles.m,
                    color = colors.sage,
                )
            }
            HimaTextArea(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                hint = stringResource(R.string.new_report_description_hint),
            )

            AnimatedVisibility(
                visible = uiState.canAnalyze,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column {
                    HimaPrimaryButton(
                        text = stringResource(R.string.new_report_analyze),
                        onClick = analyzeWithLocation,
                        leadingIconRes = R.drawable.ic_spark,
                        modifier = Modifier.padding(top = 26.dp),
                    )
                    if (locationPermissionDenied) {
                        Text(
                            text = stringResource(R.string.analysis_location_required),
                            style = HimaTextStyles.m,
                            color = colors.severityCritical,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    if (showLocationPicker) {
        ManualLocationPicker(
            selected = uiState.manualLocation,
            onSelect = viewModel::onManualLocationChange,
            onDismiss = { showLocationPicker = false },
        )
    }
}

/**
 * The report's location: normally the auto-detected GPS fix, shown as a
 * resolved value rather than an input. Tapping the pencil opens a picker that
 * overrides it for this one report — a demo aid, so a report can be filed for
 * anywhere in the country regardless of where the device actually is.
 *
 * When an override is active the row says so explicitly, so it can't be
 * mistaken for a real GPS reading.
 */
@Composable
private fun LocationField(
    manualLocation: ManualLocation?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHimaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HimaRadius.field))
            .background(colors.bg2)
            .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_field_pin),
            contentDescription = null,
            tint = colors.green,
            modifier = Modifier.size(19.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = manualLocation
                    ?.let { stringResource(it.labelRes) }
                    ?: stringResource(R.string.new_report_location_pending),
                style = HimaTextStyles.t.copy(fontSize = 15.sp),
                color = colors.ink,
            )
            if (manualLocation != null) {
                Text(
                    text = stringResource(R.string.new_report_location_custom_badge),
                    style = HimaTextStyles.m.copy(fontSize = 11.5.sp),
                    color = colors.green,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        HimaIconButton(
            iconRes = R.drawable.ic_my_location,
            contentDescription = stringResource(R.string.cd_edit_location),
            onClick = onEditClick,
        )
    }
}

/**
 * The manual-location picker. A filterable list of well-known Saudi places
 * plus an explicit way back to real GPS.
 *
 * A fixed list rather than a geocoded search: Geocoder's name lookup depends on
 * a backend service that is routinely missing on emulators, and this exists
 * specifically so the location can be set reliably during a demo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualLocationPicker(
    selected: ManualLocation?,
    onSelect: (ManualLocation?) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalHimaColors.current
    var query by rememberSaveable { mutableStateOf("") }
    // Matched against the localised name, so searching works in whichever
    // language the app is currently showing.
    val places = SaudiPlaces.all.map { it to stringResource(it.labelRes) }
    val matches = places.filter { (_, label) -> label.contains(query.trim(), ignoreCase = true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = HimaRadius.sheet, topEnd = HimaRadius.sheet),
    ) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text(
                text = stringResource(R.string.new_report_location_picker_title),
                style = HimaTextStyles.h2,
                color = colors.ink,
            )
            HimaTextField(
                value = query,
                onValueChange = { query = it },
                hint = stringResource(R.string.new_report_location_search_hint),
                modifier = Modifier.padding(top = 14.dp),
            )
            if (selected != null) {
                HimaTextLink(
                    text = stringResource(R.string.new_report_location_use_gps),
                    onClick = {
                        onSelect(null)
                        onDismiss()
                    },
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
            if (matches.isEmpty()) {
                Text(
                    text = stringResource(R.string.new_report_location_none_found),
                    style = HimaTextStyles.b,
                    color = colors.sage,
                    modifier = Modifier.padding(top = 18.dp),
                )
            }
            LazyColumn(Modifier.padding(top = 8.dp).heightIn(max = 320.dp)) {
                items(matches, key = { it.first.labelRes }) { (place, label) ->
                    val isSelected = selected?.labelRes == place.labelRes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(HimaRadius.field))
                            .clickable {
                                onSelect(place)
                                onDismiss()
                            }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_field_pin),
                            contentDescription = null,
                            tint = if (isSelected) colors.green else colors.sage,
                            modifier = Modifier.size(17.dp),
                        )
                        Text(
                            text = label,
                            style = HimaTextStyles.t.copy(fontSize = 15.sp),
                            color = if (isSelected) colors.green else colors.ink,
                        )
                    }
                }
            }
        }
    }
}
