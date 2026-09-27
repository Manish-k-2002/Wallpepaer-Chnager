package com.example.wallpaperchanger

import android.app.WallpaperManager
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * A CoroutineWorker is the unit of work WorkManager runs on your schedule.
 * Unlike a plain Thread or Handler, WorkManager persists this request to
 * disk, so it survives app restarts, device reboots (once we set that up),
 * and it respects battery-saving constraints automatically.
 */
class WallpaperWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val UNIQUE_WORK_NAME = "daily_wallpaper_change"
        private const val TAG = "WallpaperWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val metrics: DisplayMetrics = applicationContext.resources.displayMetrics
            val bitmap = ImageRepository.fetchRandomWallpaper(metrics.widthPixels, metrics.heightPixels)

            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            wallpaperManager.setBitmap(bitmap)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to change wallpaper", e)
            // retry() tells WorkManager to try again later with backoff,
            // instead of silently giving up on a temporary network blip.
            Result.retry()
        }
    }
}
