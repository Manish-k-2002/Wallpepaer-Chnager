package com.example.wallpaperchanger

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Handles fetching a wallpaper image from the network.
 *
 * Picsum (https://picsum.photos) is used because it needs no API key and
 * returns a random photo at whatever size you ask for. Swap the BASE_URL
 * logic here if you later want Unsplash (which requires a free API key).
 */
object ImageRepository {

    /**
     * Downloads a random image sized to [width] x [height] and decodes it
     * into a Bitmap. Runs on the IO dispatcher because network calls must
     * never happen on the main/UI thread in Android.
     */
    suspend fun fetchRandomWallpaper(width: Int, height: Int): Bitmap = withContext(Dispatchers.IO) {
        // The "?random=<timestamp>" query param stops Picsum/any cache
        // from returning the exact same image every time.
        val url = URL("https://picsum.photos/$width/$height?random=${System.currentTimeMillis()}")
        val connection = url.openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000

        try {
            connection.connect()
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("Server returned HTTP ${connection.responseCode}")
            }
            connection.inputStream.use { stream ->
                BitmapFactory.decodeStream(stream)
                    ?: throw IllegalStateException("Could not decode image data")
            }
        } finally {
            connection.disconnect()
        }
    }
}
