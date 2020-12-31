package r2.studios.lifttime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

/**
 * Created by Mohamed Ramshad on 28/12/2020.
 */
class RestTimerService: Service() {

    private val TIMER_NOTIFICATION_ID = 4321
    private val ALARM_NOTIFICATION_ID = 6789
    val TIMER_CHANNEL_ID = "TimerChannel"
    val TIMER_CHANNEL_NAME = "Timer"
    val ALARM_CHANNEL_ID = "AlarmChannel"
    val ALARM_CHANNEL_NAME = "Alarm"
    lateinit var timer : CountDownTimer

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Get minute and seconds passed as extras
        val extras = intent?.extras
        val min : Int = extras?.get("MIN") as Int
        val sec : Int = extras?.get("SEC") as Int

        val currentTime = System.currentTimeMillis()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createNotificationChannel(TIMER_CHANNEL_ID, TIMER_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            createNotificationChannel(ALARM_CHANNEL_ID, ALARM_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        }

        // Get time in milliseconds
        val timeInMilli = ((min * 60 * 1000) + (sec * 1000)).toLong()
        val timeToCountDown = currentTime + timeInMilli

        val notificationBuilder = NotificationCompat.Builder(this, TIMER_CHANNEL_ID)
        val notification: Notification = notificationBuilder
            .setContentTitle(getText(R.string.notification_title))
            .setContentText("${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}")
            .setSmallIcon(R.drawable.ic_notif_gym)
            .setTicker(getText(R.string.ticker_text))
            .build()
        

        startForeground(TIMER_NOTIFICATION_ID, notification)
        // countdwon timer
        timer = object : CountDownTimer(timeInMilli, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // update notification every seconds
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                var numMessages = 0
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60

                // update notification every seconds
                notificationBuilder.setContentText("${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}")
                    .setNumber(++numMessages)
                    .setNotificationSilent()
                notificationManager.notify(
                    TIMER_NOTIFICATION_ID,
                    notificationBuilder.build()
                )
            }

            override fun onFinish() {
                // finish rest time, alert user
//                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//                notificationBuilder.setContentText("LIFT TIME!").setOngoing(false)
//                notificationManager.notify(
//                    TIMER_NOTIFICATION_ID,
//                    notificationBuilder.build()
//                )

                stopForeground(true)

                val alarmNotificationBuilder = NotificationCompat.Builder(this@RestTimerService, ALARM_CHANNEL_ID)
                val alarmNotification: Notification = alarmNotificationBuilder
                    .setContentTitle(getText(R.string.notification_title))
                    .setContentText("Rest time over!")
                    .setSmallIcon(R.drawable.ic_notif_gym)
                    .setTicker(getText(R.string.ticker_text))
                    .build()

                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(ALARM_NOTIFICATION_ID, alarmNotification)
            }
        }.start()

        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String, importance: Int): String{
        val chan = NotificationChannel(
            channelId,
            channelName, importance
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}