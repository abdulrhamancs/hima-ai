package com.hima.ai.presentation.report.capture

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hima.ai.R
import com.hima.ai.core.designsystem.component.HimaPrimaryButton
import com.hima.ai.core.designsystem.component.HimaSecondaryButton
import com.hima.ai.core.designsystem.component.MinTouchTarget
import com.hima.ai.core.designsystem.theme.HimaTextStyles
import com.hima.ai.core.designsystem.theme.LocalHimaColors
import com.hima.ai.data.mock.CaptureSource
import java.io.File
import java.util.concurrent.Executor

/**
 * Evidence capture. One screen serves both paths so the photo is confirmed the
 * same way whichever way it arrived: the camera path runs an in-app CameraX
 * viewfinder, the gallery path hands straight off to the system photo picker.
 *
 * The viewfinder is deliberately bare — the frame is the interface. The only
 * chrome is a close affordance, corner guides, and the shutter.
 */
@Composable
fun CaptureScreen(
    onConfirmed: () -> Unit,
    onCancelled: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.onImagePicked(uri) else onCancelled()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.onCameraPermissionResult(granted) }

    // The gallery path never shows a viewfinder: open the picker immediately.
    LaunchedEffect(uiState.source) {
        if (uiState.source == CaptureSource.GALLERY && uiState.capturedUri == null) {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.source != CaptureSource.CAMERA) return@LaunchedEffect
        if (context.hasCameraPermission()) {
            viewModel.onCameraPermissionResult(granted = true)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CaptureBackdrop),
    ) {
        val captured = uiState.capturedUri
        when {
            captured != null -> ReviewStep(
                imageUri = captured,
                isFromCamera = uiState.source == CaptureSource.CAMERA,
                onRetry = {
                    viewModel.onDiscard()
                    if (uiState.source == CaptureSource.GALLERY) {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    }
                },
                onUse = {
                    viewModel.onConfirm()
                    onConfirmed()
                },
                onClose = onCancelled,
            )

            uiState.source == CaptureSource.CAMERA && !uiState.cameraPermissionGranted ->
                PermissionStep(
                    onGrant = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onOpenSettings = { context.openAppSettings() },
                    permanentlyDenied = uiState.permissionPermanentlyDenied,
                    onClose = onCancelled,
                )

            uiState.source == CaptureSource.CAMERA -> ViewfinderStep(
                isCapturing = uiState.isCapturing,
                errorRes = uiState.errorRes,
                onCapture = viewModel::onCaptureRequested,
                onCaptured = viewModel::onImageCaptured,
                onError = viewModel::onCaptureFailed,
                onClose = onCancelled,
            )
        }
    }
}

/** The live camera. Chrome is limited to what a gloved hand needs mid-report. */
@Composable
private fun ViewfinderStep(
    isCapturing: Boolean,
    errorRes: Int?,
    onCapture: () -> Unit,
    onCaptured: (Uri) -> Unit,
    onError: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            update = { previewView ->
                val providerFuture = ProcessCameraProvider.getInstance(context)
                providerFuture.addListener({
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build()
                        .also { it.surfaceProvider = previewView.surfaceProvider }
                    provider.unbindAll()
                    runCatching {
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture,
                        )
                    }
                }, context.mainExecutor())
            },
        )

        // Release the camera when leaving, so returning to the report screen
        // doesn't leave the sensor bound.
        DisposableEffect(Unit) {
            onDispose {
                runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
            }
        }

        FramingGuides(Modifier.fillMaxSize())

        CloseButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 46.dp, start = 12.dp),
        )

        AnimatedVisibility(
            visible = errorRes != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 104.dp, start = 32.dp, end = 32.dp),
        ) {
            if (errorRes != null) CaptureNotice(text = stringResource(errorRes))
        }

        ShutterButton(
            enabled = !isCapturing,
            onClick = {
                onCapture()
                imageCapture.takePhoto(context, onCaptured, onError)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 54.dp),
        )
    }
}

/** Confirm step, shared by both paths so a photo is accepted the same way. */
@Composable
private fun ReviewStep(
    imageUri: Uri,
    isFromCamera: Boolean,
    onRetry: () -> Unit,
    onUse: () -> Unit,
    onClose: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            model = imageUri,
            contentDescription = stringResource(R.string.cd_evidence_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        CloseButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 46.dp, start = 12.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
        ) {
            HimaPrimaryButton(
                text = stringResource(R.string.capture_use_photo),
                onClick = onUse,
                leadingIconRes = R.drawable.ic_check,
            )
            Spacer(Modifier.height(10.dp))
            HimaSecondaryButton(
                text = stringResource(
                    if (isFromCamera) R.string.capture_retake else R.string.capture_choose_another,
                ),
                onClick = onRetry,
                containerColor = Color.White.copy(alpha = 0.92f),
            )
        }
    }
}

/** Shown when the camera path has no permission yet. */
@Composable
private fun PermissionStep(
    onGrant: () -> Unit,
    onOpenSettings: () -> Unit,
    permanentlyDenied: Boolean,
    onClose: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        CloseButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 46.dp, start = 12.dp),
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_camera),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(30.dp),
            )
            Text(
                text = stringResource(R.string.capture_permission_title),
                style = HimaTextStyles.h2.copy(fontSize = 17.sp),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = stringResource(R.string.capture_permission_body),
                style = HimaTextStyles.b,
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            HimaPrimaryButton(
                text = stringResource(
                    if (permanentlyDenied) R.string.capture_permission_settings
                    else R.string.capture_permission_grant,
                ),
                onClick = if (permanentlyDenied) onOpenSettings else onGrant,
            )
        }
    }
}

/**
 * Four corner marks framing the shot. This is the only brand gesture in the
 * viewfinder — it reads as a field instrument's sight rather than decoration,
 * and it tells the ranger where the frame actually ends under glare.
 */
@Composable
private fun FramingGuides(modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Canvas(modifier.padding(horizontal = 24.dp, vertical = 136.dp)) {
        val arm = 26.dp.toPx()
        val stroke = 2.dp.toPx()
        val w = size.width
        val h = size.height
        // Hairline white under the green keeps the marks legible against both
        // bright sand and dark canopy without adding a scrim over the frame.
        listOf(Color.White.copy(alpha = 0.35f) to stroke * 2f, colors.green to stroke).forEach { (color, width) ->
            listOf(
                Offset(0f, 0f) to listOf(Offset(arm, 0f), Offset(0f, arm)),
                Offset(w, 0f) to listOf(Offset(w - arm, 0f), Offset(w, arm)),
                Offset(0f, h) to listOf(Offset(arm, h), Offset(0f, h - arm)),
                Offset(w, h) to listOf(Offset(w - arm, h), Offset(w, h - arm)),
            ).forEach { (corner, arms) ->
                arms.forEach { end ->
                    drawLine(color, corner, end, strokeWidth = width, cap = StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
private fun ShutterButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalHimaColors.current
    Box(
        modifier = modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (enabled) 0.22f else 0.10f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(if (enabled) Color.White else Color.White.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            // A single green ring is the whole brand cue on the shutter.
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(colors.green.copy(alpha = if (enabled) 1f else 0.4f)),
            )
        }
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(MinTouchTarget)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = stringResource(R.string.cd_close),
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun CaptureNotice(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            style = HimaTextStyles.m.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

/** Near-black rather than pure black, so the viewfinder sits in the app's warmth. */
private val CaptureBackdrop = Color(0xFF121712)

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.mainExecutor(): Executor = ContextCompat.getMainExecutor(this)

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}

/**
 * Writes to the app's cache: evidence only needs to survive until the report is
 * submitted, and staying out of shared storage means no storage permission and
 * nothing left in the user's gallery.
 */
private fun ImageCapture.takePhoto(
    context: Context,
    onCaptured: (Uri) -> Unit,
    onError: () -> Unit,
) {
    val file = File(context.cacheDir, "hima_evidence_${System.currentTimeMillis()}.jpg")
    val options = ImageCapture.OutputFileOptions.Builder(file).build()
    takePicture(
        options,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onCaptured(output.savedUri ?: Uri.fromFile(file))
            }

            override fun onError(exception: ImageCaptureException) {
                onError()
            }
        },
    )
}
