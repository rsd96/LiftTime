package r2.studios.lifttime.ui.main

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

        viewModel.serviceRunning.observe(this, Observer { serviceRunning ->
            binding.switchGymTime.setOnCheckedChangeListener(null)
            binding.switchGymTime.isChecked = serviceRunning

            if (!serviceRunning) {
                animationQue.add(Triple(160, 174, false))
                animationQue.add(Triple(0, 74, true))
                stopService()

                try {
                    val timerServiceIntent = Intent(context, RestTimerService::class.java)
                    context?.stopService(timerServiceIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                startService()
                animationQue.add(Triple(75, 91, false))
                animationQue.add(Triple(92, 159, true))
            }

            binding.switchGymTime.setOnCheckedChangeListener { _, _ ->
                viewModel.changeServiceStatus()
            }
        })
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
        setupNumberPickers()
        loadSavedData()
        playAnimation(0, 74, true)
        animationHandler()
    }

    private fun setupNumberPickers() {
        binding.npMin.apply {
            minValue = 0
            maxValue = 59
            wrapSelectorWheel = true
            setOnValueChangedListener { _, _, newVal ->
                viewModel.min.value = newVal
            }
        }

        binding.npSec.apply {
            minValue = 0
            maxValue = 59
            wrapSelectorWheel = true
            setOnValueChangedListener { _, _, newVal ->
                viewModel.sec.value = newVal
            }
        }

        viewModel.min.observe(viewLifecycleOwner) {
            if (binding.npMin.value != it) binding.npMin.value = it ?: 0
        }

        viewModel.sec.observe(viewLifecycleOwner) {
            if (binding.npSec.value != it) binding.npSec.value = it ?: 0
        }
    }

    fun playAnimation(startFrame: Int, endFrame: Int, repeat: Boolean) {
        binding.lottieImage.setMinAndMaxFrame(startFrame, endFrame)
        binding.lottieImage.repeatCount = if (repeat) LottieDrawable.INFINITE else 1
        binding.lottieImage.playAnimation()
    }

    private fun animationHandler() {
        binding.lottieImage.addAnimatorListener(object : Animator.AnimatorListener {
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
                if (animationQue.isNotEmpty()) {
                    val (startFrame, endFrame, repeat) = animationQue[0]
                    playAnimation(startFrame, endFrame, repeat)
                    animationQue.removeAt(0)
                }
            }
        })
    }

    private fun loadSavedData() {
        val sharedPref = activity?.getSharedPreferences(getString(R.string.shared_pref_name), Context.MODE_PRIVATE) ?: return
        viewModel.min.value = sharedPref.getInt(getString(R.string.min_key), 0)
        viewModel.sec.value = sharedPref.getInt(getString(R.string.sec_key), 0)
        viewModel.setServiceRunning(sharedPref.getBoolean(getString(R.string.service_key), false))
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

    override fun onPause() {
        super.onPause()
        val sharedPref = activity?.getSharedPreferences(getString(R.string.shared_pref_name), Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
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
