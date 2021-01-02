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
            return formattedString
        }

        @JvmStatic
        fun paddedStringToInt(value: String): Int {
            Log.d("ViewModel- string", value.toString())
            return Integer.parseInt(value)
        }

    }

    private val _min = MutableLiveData<Int>()
    private val _sec = MutableLiveData<Int>()
    private val _serviceRunning = MutableLiveData<Boolean>()
    private val _activeTime = MutableLiveData<String>()
    val min = MutableLiveData<Int>()
//        get() = _min

    val sec = MutableLiveData<String>()
//        get() = _sec

    init {
        min.value = 0
        sec.value = "00"
        _serviceRunning.value = false
        _activeTime.value = "none"
    }


    val serviceRunning : LiveData<Boolean>
        get() = _serviceRunning

    val activeTime : LiveData<String>
        get() = _activeTime


    fun setMin(min: Int){
        _min.value = min
    }

    fun setSec(sec: Int){
        _sec.value = sec
    }

    fun setServiceRunning(serviceRunning: Boolean){
        _serviceRunning.value = serviceRunning
    }

    fun setActiveTime(activeTime: String) {
        _activeTime.value = activeTime
    }

    fun changeServiceStatus() {
        _serviceRunning.value = _serviceRunning.value != true
    }

    fun addMin() {
        _min.value = _min.value?.plus(1)
    }

    fun addSec() {
        _sec.value = _sec.value?.plus(1)
    }
}