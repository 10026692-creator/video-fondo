package com.example.videofondo

import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

class VideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = VideoEngine()

    inner class VideoEngine : Engine() {
        private var player: ExoPlayer? = null
        private var rutaActual: String? = null
        private var holderActual: SurfaceHolder? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            holderActual = holder
            cargarVideo()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            liberar()
            holderActual = null
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                val nueva = rutaGuardada()
                if (nueva != rutaActual) cargarVideo() else player?.play()
            } else {
                player?.pause()
            }
        }

        override fun onDestroy() {
            liberar()
            super.onDestroy()
        }

        private fun rutaGuardada(): String? =
            getSharedPreferences("prefs", MODE_PRIVATE).getString("video_path", null)

        private fun cargarVideo() {
            val holder = holderActual ?: return
            val ruta = rutaGuardada() ?: return
            if (!File(ruta).exists()) return

            liberar()
            rutaActual = ruta
            player = ExoPlayer.Builder(this@VideoWallpaperService).build().apply {
                setVideoSurfaceHolder(holder)
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                setMediaItem(MediaItem.fromUri(Uri.fromFile(File(ruta))))
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                prepare()
                play()
            }
        }

        private fun liberar() {
            player?.release()
            player = null
        }
    }
}
