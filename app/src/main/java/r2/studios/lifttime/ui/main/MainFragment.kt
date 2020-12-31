package r2.studios.lifttime.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.main_fragment.*
import r2.studios.lifttime.StartLiftTimeService
import r2.studios.lifttime.R
import r2.studios.lifttime.RestTimerService


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    lateinit private var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.min.observe(this, Observer {
                min -> etMin.setText(viewModel.min.value.toString().padStart(2, '0'))
        })

        viewModel.sec.observe(this, Observer {
                sec -> etSec.setText(viewModel.sec.value.toString().padStart(2, '0'))
        })

        viewModel.serviceRunning.observe(this, Observer {
                serviceRunning -> switchGymTime.isChecked = viewModel.serviceRunning.value!!
        })


        val sharedPref = activity?.getSharedPreferences(getString(R.string.shared_pref_name), Context.MODE_PRIVATE) ?: return

        viewModel.setMin(sharedPref.getInt(getString(R.string.min_key), 0))
        viewModel.setSec(sharedPref.getInt(getString(R.string.sec_key), 0))
        viewModel.setServiceRunning(sharedPref.getBoolean(getString(R.string.service_key), false))

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        etMin.addTextChangedListener(object: TextWatcher {
            private var ignoreChange = false
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text: Editable?) {
                if (!ignoreChange) {
                    ignoreChange = true
                    etMin.setText(text.toString().padStart(2, '0'))
                    viewModel.setMin(Integer.parseInt(text.toString()))
                    ignoreChange = false
                }
            }
        })

        etSec.addTextChangedListener(object: TextWatcher {
            private var ignoreChange = false
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text: Editable?) {
                if (!ignoreChange) {
                    ignoreChange = true
                    etSec.setText(text.toString().padStart(2, '0'))
                    viewModel.setSec(Integer.parseInt(text.toString()))
                    ignoreChange = false

                }
            }
        })

        switchGymTime.setOnClickListener {
            Log.d("MainFragment", "run service")
            if (viewModel.serviceRunning.value!!) {
                stopService()

                // stop any timer that was started before
                try {
                    val timerServiceIntent = Intent(context, RestTimerService::class.java)
                    context?.stopService(timerServiceIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else
                startService()

            viewModel.changeServiceStatus()
        }
    }

    fun startService() {
        activity?.baseContext?.let { context ->
            val serviceIntent = Intent(context, StartLiftTimeService::class.java)
            serviceIntent.putExtra("MIN", viewModel.min.value)
            serviceIntent.putExtra("SEC", viewModel.sec.value)

            ContextCompat.startForegroundService(context, serviceIntent)
        }

    }

    fun stopService() {
        val serviceIntent = Intent(activity?.baseContext, StartLiftTimeService::class.java)
        activity?.stopService(serviceIntent)
    }


    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onPause() {
        super.onPause()
        // save data to restore the time and service running state
        val sharedPref = activity?.getSharedPreferences(getString(R.string.shared_pref_name), Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            viewModel.min.value?.let { min -> putInt(getString(R.string.min_key), min) }
            viewModel.sec.value?.let { sec -> putInt(getString(R.string.sec_key), sec) }
            viewModel.serviceRunning.value?.let { serviceRunning -> putBoolean(getString(R.string.service_key), serviceRunning) }
            apply()
        }
    }

}