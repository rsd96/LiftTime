package r2.studios.lifttime

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.*


/**
 * Created by Mohamed Ramshad on 20/12/2020.
 */
class StartLiftTimeService: Service() {

    val screenWakeReceiver = ScreenWakeReceiver()
    private val START_LIFT_NOTIFICATION_ID = 1234
    val CHANNEL_ID = "ForegroundServiceChannel"
    val CHANNEL_NAME = "Lift Time Notification"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        Log.d("BroadCast", "onstartCommand...")

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createNotificationChannel(CHANNEL_ID, CHANNEL_NAME)
        }

        val notificationIntent = Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        val notification: Notification = notificationBuilder
                .setContentTitle(getText(R.string.notification_title))
                .setContentText("Lift Time ON!")
                .setSmallIcon(R.drawable.ic_notif_gym)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .build()


        // register the broadcast receiver here so that it is only active during the notification lifetime


        val screenFilter = IntentFilter()
        screenFilter.addAction(Intent.ACTION_SCREEN_ON)
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenWakeReceiver, screenFilter)


        startForeground(START_LIFT_NOTIFICATION_ID, notification)


        return START_NOT_STICKY
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_DEFAULT
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("Service", "stopping service")
        stopForeground(true)
        unregisterReceiver(screenWakeReceiver)

    }
}