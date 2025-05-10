package r2.studios.lifttime.ui.main

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieDrawable
import r2.studios.lifttime.R
import r2.studios.lifttime.RestTimerService
import r2.studios.lifttime.StartLiftTimeService
import r2.studios.lifttime.databinding.MainFragmentBinding


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    var animationQue = mutableListOf<Triple<Int, Int, Boolean>>()
    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)


        // place cursor of min/sec edittext to end to allow time entry properly
        viewModel.min.observe(this, Observer {
            if (it == 0)
                binding.etMin.setSelection(0)
            else
                binding.etMin.setSelection(binding.etMin.text.length)
        })

        viewModel.sec.observe(this, Observer {
            if (it == 0)
                binding.etSec.setSelection(0)
            else
                binding.etSec.setSelection(binding.etSec.text.length)
        })

        viewModel.serviceRunning.observe(this, Observer {serviceRunning->
            binding.switchGymTime.setOnCheckedChangeListener(null)

            binding.switchGymTime.isChecked = serviceRunning

            if (!serviceRunning) {

                animationQue.add(Triple(160, 174, false))
                animationQue.add(Triple(0,74, true))
                stopService()

                // stop any timer that was started before
                try {
                    val timerServiceIntent = Intent(context, RestTimerService::class.java)
                    context?.stopService(timerServiceIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                startService()
                // animate the character doing push ups when turned on

                animationQue.add(Triple(75, 91, false))
                animationQue.add(Triple(92, 159, true))
            }

            binding.switchGymTime.setOnCheckedChangeListener { compoundButton, b ->
                viewModel.changeServiceStatus()
            }



        })

        viewModel.activeTime.observe(this) {
            when (viewModel.activeTime.value) {
                "min" -> binding.keypad.field = binding.etMin
                "sec" -> binding.keypad.field = binding.etSec
                else -> binding.keypad.field = null
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTimerEditText()
        //handleSwitchPressendEvent()
        loadSavedData()
        playAnimation(0, 74, true)
        animationHandler()
    }

    /***
     * Play lottie view animation
     */

    fun playAnimation(startFrame: Int, endFrame: Int, repeat: Boolean) {
        binding.lottieImage.setMinAndMaxFrame(startFrame, endFrame)
        if (repeat)
            binding.lottieImage.repeatCount = LottieDrawable.INFINITE
        else
            binding.lottieImage.repeatCount = 1

        binding.lottieImage.playAnimation()
    }

    private fun animationHandler() {
        binding.lottieImage.addAnimatorListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {
                if (animationQue.isNotEmpty()) {
                    val (startFrame, endFrame, repeat) = animationQue[0]
                    playAnimation(startFrame, endFrame, repeat)
                    animationQue.removeAt(0)
                }
            }

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationRepeat(p0: Animator) {
                Log.d("Animationg", "onanimationrepeat")
                if (animationQue.isNotEmpty()) {
                    Log.d("Animationg", animationQue[0].toString())
                    val (startFrame, endFrame, repeat) = animationQue[0]
                    playAnimation(startFrame, endFrame, repeat)
                    animationQue.removeAt(0)
                }
            }
        })
    }

    /***
     * Re-populate views with saved data from shared prefs
     */

    private fun loadSavedData() {

        Log.d("MainFragment", "Loading data...")
        val sharedPref = activity?.getSharedPreferences(getString(R.string.shared_pref_name), Context.MODE_PRIVATE) ?: return

        viewModel.min.value = sharedPref.getInt(getString(R.string.min_key), 0)
        viewModel.sec.value = sharedPref.getInt(getString(R.string.sec_key), 0)
        viewModel.setServiceRunning(sharedPref.getBoolean(getString(R.string.service_key), false))

    }


    /***
     * Highlight either the min or sec edittext when clicked and set it as the currect active edittext
     */
    private fun handleTimerEditText() {
        binding.etMin.setOnClickListener {
            binding.etMin.setTextColor(ResourcesCompat.getColor(resources, R.color.colorAccent, null))
            binding.etSec.setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, null))
            viewModel.setActiveTime("min")
        }

        binding.etSec.setOnClickListener {
            binding.etSec.setTextColor(ResourcesCompat.getColor(resources, R.color.colorAccent, null))
            binding.etMin.setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, null))
            viewModel.setActiveTime("sec")
        }
    }

    /***
     * Start the service to watch for screen on/off and pass time min and sec values
     */
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


    override fun onPause() {
        super.onPause()
        Log.d("MainFragment", "Saving data...")
        // save data to restore the time and service running state
        val sharedPref = activity?.getSharedPreferences(getString(R.string.shared_pref_name), Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            viewModel.min.value?.let { min -> putInt(getString(R.string.min_key), min) }
            viewModel.sec.value?.let { sec -> putInt(getString(R.string.sec_key), sec) }
            viewModel.serviceRunning.value?.let { serviceRunning -> putBoolean(getString(R.string.service_key), serviceRunning) }
            apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.lottieImage.cancelAnimation()
    }

}