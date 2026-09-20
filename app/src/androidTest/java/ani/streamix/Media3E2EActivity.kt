package ani.streamix

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.kakaanime.providerv2.AniLabStreamCandidate
import java.util.concurrent.CountDownLatch

@OptIn(UnstableApi::class)
class Media3E2EActivity : Activity() {
    private var player: ExoPlayer? = null

    companion object {
        @Volatile var candidate: AniLabStreamCandidate? = null
        @Volatile var firstFrameLatch = CountDownLatch(1)
        @Volatile var playbackError: PlaybackException? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firstFrameLatch = CountDownLatch(1)

        val root = FrameLayout(this)
        val playerView = PlayerView(this).apply {
            useController = true
            keepScreenOn = true
        }
        root.addView(playerView, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        ))
        setContentView(root)

        val c = candidate ?: error("Provider candidate was not supplied")
        val headers = linkedMapOf<String, String>().apply {
            putAll(c.headers)
            if (c.referer.isNotBlank()) this["Referer"] = c.referer
            if (c.cookies.isNotBlank()) this["Cookie"] = c.cookies
        }

        val dataSource = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(headers)

        val mediaSourceFactory = DefaultMediaSourceFactory(dataSource)
        val mediaItem = MediaItem.Builder()
            .setUri(c.url)
            .apply {
                when (c.type) {
                    com.kakaanime.providerv2.AniLabStreamType.HLS ->
                        setMimeType(MimeTypes.APPLICATION_M3U8)
                    com.kakaanime.providerv2.AniLabStreamType.DASH ->
                        setMimeType(MimeTypes.APPLICATION_MPD)
                    else -> Unit
                }
            }
            .build()

        val exo = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        player = exo
        playerView.player = exo

        exo.addAnalyticsListener(object : AnalyticsListener {
            override fun onRenderedFirstFrame(
                eventTime: AnalyticsListener.EventTime,
                output: Any,
                renderTimeMs: Long,
            ) {
                android.util.Log.i(
                    "SamehadakuMedia3E2E",
                    "FIRST_FRAME rendered; type=${c.type}; quality=${c.quality}",
                )
                firstFrameLatch.countDown()
            }
        })

        exo.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                playbackError = error
                android.util.Log.e(
                    "SamehadakuMedia3E2E",
                    "PLAYER_ERROR: ${error.errorCodeName}: ${error.message}",
                    error,
                )
            }
        })

        android.util.Log.i(
            "SamehadakuMedia3E2E",
            "MEDIA3_START type=${c.type}; quality=${c.quality}; provider=${c.providerId}; extractor=${c.extractorId}; url=${c.url}",
        )

        exo.setMediaItem(mediaItem)
        exo.prepare()
        exo.play()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}
