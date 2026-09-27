package com.example.wallpaperchanger

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wallpaperchanger.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val prefs by lazy { getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE) }

    companion object {
        private const val TAG = "MainActivity"
        private const val PREF_DAILY_ENABLED = "daily_enabled"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Reflect whatever the saved schedule state is when the screen opens
        val dailyEnabled = prefs.getBoolean(PREF_DAILY_ENABLED, false)
        binding.dailySwitch.isChecked = dailyEnabled
        updateScheduleStatusText(dailyEnabled)

        binding.changeNowButton.setOnClickListener { changeWallpaperNow() }

        binding.dailySwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(PREF_DAILY_ENABLED, isChecked).apply()
            if (isChecked) {
                scheduleDailyWork()
            } else {
                cancelDailyWork()
            }
            updateScheduleStatusText(isChecked)
        }
    }

    /** Immediately fetches a new image and sets it as wallpaper, right now. */
    private fun changeWallpaperNow() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val metrics = resources.displayMetrics
                val bitmap: Bitmap = ImageRepository.fetchRandomWallpaper(metrics.widthPixels, metrics.heightPixels)

                binding.previewImage.setImageBitmap(bitmap)
                WallpaperManager.getInstance(applicationContext).setBitmap(bitmap)

                binding.statusText.text = getString(R.string.status_success)
            } catch (e: Exception) {
                Log.e(TAG, "Manual wallpaper change failed", e)
                binding.statusText.text = getString(R.string.status_error)
            } finally {
                setLoading(false)
            }
        }
    }

    /**
     * Registers a periodic WorkManager job that runs every 24 hours.
     * ExistingPeriodicWorkPolicy.UPDATE means flipping the switch off and
     * back on won't create duplicate jobs.
     */
    private fun scheduleDailyWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<WallpaperWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            WallpaperWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun cancelDailyWork() {
        WorkManager.getInstance(applicationContext).cancelUniqueWork(WallpaperWorker.UNIQUE_WORK_NAME)
    }

    private fun updateScheduleStatusText(enabled: Boolean) {
        binding.statusText.text = if (enabled) {
            getString(R.string.status_scheduled)
        } else {
            getString(R.string.status_unscheduled)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.changeNowButton.isEnabled = !loading
        if (loading) binding.statusText.text = getString(R.string.status_downloading)
    }
}
