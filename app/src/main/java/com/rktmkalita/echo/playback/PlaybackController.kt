package com.rktmkalita.echo.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

object PlaybackController {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    var controller: MediaController? = null
        private set

    fun connect(context: Context) {
        if (controllerFuture != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, token).buildAsync().also { future ->
            future.addListener({
                controller = runCatching { future.get() }.getOrNull()
            }, context.mainExecutor)
        }
    }

    fun release() {
        controller?.release()
        controller = null
        controllerFuture = null
    }
}
