package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import com.udacity.util.sendNotification



class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private var downloadUrl: String? = null
    private lateinit var fileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            if (downloadUrl.isNullOrEmpty()) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.choose_download_file),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                custom_button.buttonState = ButtonState.Loading
                download()
            }
        }

        createChannel(
            CHANNEL_ID,
            getString(R.string.channel_name),
            getString(R.string.channel_description)
        )
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            val notificationManager = getSystemService(NotificationManager::class.java)
            var status: String? = null
            var downloadStatus: Int? = null

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val query = id?.let { DownloadManager.Query().setFilterById(it) }
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            }

            when (downloadStatus) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    status = "SUCCESS"
                }
                DownloadManager.STATUS_FAILED -> {
                    status = "FAIL"
                }
            }

            if (context != null) {
                notificationManager.sendNotification(
                    fileName,
                    status,
                    getString(R.string.messageBody),
                    CHANNEL_ID, context
                )
            }

            custom_button.buttonState = ButtonState.Completed
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object
    {
        private const val CHANNEL_ID = "channelId"
    }

    fun onRadioClick(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            when (view.getId()) {
                R.id.glide_button ->
                    if (checked) {
                        fileName = getString(R.string.glide_button)
                        downloadUrl = getString(R.string.glide_repo)
                    }
                R.id.loadapp_button ->
                    if (checked) {
                        fileName = getString(R.string.loadapp_button)
                        downloadUrl = getString(R.string.loadapp_repo)
                    }
                R.id.retrofit_button ->
                    if (checked) {
                        fileName = getString(R.string.retrofit_button)
                        downloadUrl = getString(R.string.retrofit_repo)
                    }
            }
        }
    }

    private fun createChannel(channelId: String, channelName: String, channelDescription: String){
        //************Create notification channel here
        // TODO: Step 1.6 START create a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                // TODO: Step 2.4 change importance
                NotificationManager.IMPORTANCE_HIGH
            )
            // TODO: Step 2.6 disable badges for this channel
            .apply { setShowBadge(false) }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = channelDescription

            //Attach the channel to the notification manager
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        // TODO: Step 1.6 END create a channel
    }

}
