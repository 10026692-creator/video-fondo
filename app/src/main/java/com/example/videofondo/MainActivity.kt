package com.example.videofondo

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

class MainActivity : ComponentActivity() {

    private val elegirVideo = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) guardarVideo(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnElegir).setOnClickListener {
            elegirVideo.launch("video/*")
        }

        findViewById<Button>(R.id.btnActivar).setOnClickListener {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this@MainActivity, VideoWallpaperService::class.java)
                )
            }
            startActivity(intent)
        }
    }

    // Copia el video a filesDir (NO a cache) para que sobreviva al "borrar caché".
    private fun guardarVideo(uri: Uri) {
        Thread {
            try {
                val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
                val anterior = prefs.getString("video_path", null)

                val destino = File(filesDir, "wallpaper_${System.currentTimeMillis()}.mp4")
                contentResolver.openInputStream(uri)?.use { input ->
                    destino.outputStream().use { input.copyTo(it) }
                }

                prefs.edit().putString("video_path", destino.absolutePath).apply()
                anterior?.let { File(it).delete() }

                runOnUiThread {
                    Toast.makeText(this, "Video guardado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "No se pudo guardar el video", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
