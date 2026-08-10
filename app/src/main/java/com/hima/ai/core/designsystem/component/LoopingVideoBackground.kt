package com.hima.ai.core.designsystem.component

import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.hima.ai.R

/**
 * A silent, looping, edge-to-edge background video (e.g. Splash), cropped to
 * fill (CSS `object-fit: cover` equivalent). Inflated from
 * res/layout/player_view_texture.xml, which forces a TextureView instead of
 * PlayerView's default SurfaceView — a SurfaceView is composited outside the
 * normal view hierarchy and renders solid black when a RenderEffect-based
 * Modifier.blur() is applied to it. The player is released on dispose.
 */
@Composable
fun LoopingVideoBackground(@RawRes videoRes: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri("android.resource://${context.packageName}/$videoRes"))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            prepare()
            playWhenReady = true
        }
    }

    // Without this the player keeps decoding while the app is backgrounded,
    // burning battery on a screen nobody is looking at.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    AndroidView(
        // Keep the native video surface behind Compose content for both
        // rendering and hit-testing; otherwise it can swallow taps on the
        // splash CTA and language control while remaining visually behind it.
        modifier = modifier.zIndex(-1f),
        factory = { ctx ->
            val inflationParent = FrameLayout(ctx)
            val playerView = LayoutInflater.from(ctx)
                .inflate(R.layout.player_view_texture, inflationParent, false) as PlayerView
            // PlayerView handles touch gestures even with its controller hidden.
            // Disable the native view for input so Compose controls layered over
            // the video (CTA and language toggle) receive taps normally.
            playerView.isEnabled = false
            playerView.isFocusable = false
            playerView.player = exoPlayer
            playerView
        },
    )
}
