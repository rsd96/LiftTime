package r2.studios.lifttime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Created by Mohamed Ramshad on 28/12/2020.
 */
class ScreenWakeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.SCREEN_ON") {
            try {
                val timerServiceIntent = Intent(context, RestTimerService::class.java)
                val pref = context?.getSharedPreferences(context.resources.getString(R.string.shared_pref_name), Context.MODE_PRIVATE)
                pref?.let {
                    val min = pref.getInt(context.resources.getString(R.string.min_key), 0)
                    val sec = pref.getInt(context.resources.getString(R.string.sec_key), 0)
                    timerServiceIntent.putExtra("MIN", min)
                    timerServiceIntent.putExtra("SEC", sec)
                    context.startService(timerServiceIntent)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


        if (intent?.action == "android.intent.action.SCREEN_OFF") {
            try {
                val timerServiceIntent = Intent(context, RestTimerService::class.java)
                context?.stopService(timerServiceIntent)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }
}