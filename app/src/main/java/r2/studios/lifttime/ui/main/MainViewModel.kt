package r2.studios.lifttime.ui.main

import android.util.Log
import android.widget.EditText
import androidx.databinding.Bindable
import androidx.databinding.BindingConversion
import androidx.databinding.InverseMethod
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    object Convertor{
        @InverseMethod("paddedStringToInt")
        @JvmStatic
        fun intToPaddedString(value: Int):String {
            Log.d("ViewModel - int", value.toString())
            var formattedString = ""
            formattedString = value.toString().padStart(2, '0')
            if (Integer.parseInt(formattedString) > 59)
                formattedString = "59"
            return formattedString
        }

        @JvmStatic
        fun paddedStringToInt(value: String): Int {
            Log.d("ViewModel- string", value.toString())
            return Integer.parseInt(value)
        }

    }

    // whether min or sec time is selected for editing
    private val _activeTime = MutableLiveData<String>()
    val min = MutableLiveData<Int>()
    val sec = MutableLiveData<Int>()
    private val _serviceRunning = MutableLiveData<Boolean>()
    val serviceRunning : LiveData<Boolean>
        get() = _serviceRunning

    init {
//        test.value = false
        min.value = 0
        sec.value = 0
        _serviceRunning.value = false
        _activeTime.value = "none"
    }

    val activeTime : LiveData<String>
        get() = _activeTime

    fun setServiceRunning(serviceRunning: Boolean){
        _serviceRunning.value = serviceRunning
    }

    fun setActiveTime(activeTime: String) {
        _activeTime.value = activeTime
    }

    fun changeServiceStatus() {
        _serviceRunning.value = _serviceRunning.value != true
    }

//    fun addMin() {
//        _min.value = _min.value?.plus(1)
//    }
//
//    fun addSec() {
//        _sec.value = _sec.value?.plus(1)
//    }
}