package com.rktmkalita.echo.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

class Media3PlayerAdapter private constructor(context: Context) {
    private var controller: MediaController? = null
    private val pendingActions = ArrayDeque<MediaController.() -> Unit>()
    private var completionListener: (() -> Unit)? = null
    private var readyListener: (() -> Unit)? = null
    private var playingChangedListener: ((Boolean) -> Unit)? = null
    private val controllerFuture: ListenableFuture<MediaController>

    init {
        val token = SessionToken(context.applicationContext, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context.applicationContext, token).buildAsync()
        controllerFuture.addListener({
            controller = runCatching { controllerFuture.get() }.getOrNull()
            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    playingChangedListener?.invoke(isPlaying)
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) completionListener?.invoke()
                    if (playbackState == Player.STATE_READY) readyListener?.invoke()
                }
            })
            controller?.let { connected ->
                while (pendingActions.isNotEmpty()) pendingActions.removeFirst().invoke(connected)
            }
        }, context.mainExecutor)
    }

    val isPlaying: Boolean get() = controller?.isPlaying == true
    val duration: Int get() = (controller?.duration ?: 0L).coerceAtLeast(0L).toInt()

    fun getCurrentPosition(): Int = (controller?.currentPosition ?: 0L).coerceAtLeast(0L).toInt()

    private fun enqueue(action: MediaController.() -> Unit) {
        controller?.let(action) ?: pendingActions.addLast(action)
    }

    fun setDataSource(context: Context?, uri: Uri) {
        enqueue { setMediaItem(MediaItem.fromUri(uri)) }
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
    }

    fun setOnReadyListener(listener: () -> Unit) {
        readyListener = listener
    }

    fun setOnPlayingChangedListener(listener: (Boolean) -> Unit) {
        playingChangedListener = listener
        controller?.let { listener(it.isPlaying) }
    }

    fun prepare() = enqueue { prepare() }
    fun start() = enqueue { play() }
    fun pause() = enqueue { pause() }
    fun stop() = enqueue { stop() }
    fun reset() = enqueue { clearMediaItems() }
    fun seekTo(position: Int) = enqueue { seekTo(position.toLong()) }
    fun setAudioStreamType(type: Int) = Unit
    fun release() {
        controller?.release()
        controller = null
        pendingActions.clear()
    }

    companion object {
        @Volatile private var shared: Media3PlayerAdapter? = null

        fun get(context: Context): Media3PlayerAdapter = shared ?: synchronized(this) {
            shared ?: Media3PlayerAdapter(context).also { shared = it }
        }
    }
}
