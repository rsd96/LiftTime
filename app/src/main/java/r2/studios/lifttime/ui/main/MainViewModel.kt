package r2.studios.lifttime.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {



    private val _min = MutableLiveData<Int>()
    private val _sec = MutableLiveData<Int>()
    private val _serviceRunning = MutableLiveData<Boolean>()


    init {
        _min.value = 0
        _sec.value = 0
        _serviceRunning.value = false
    }

    val min : LiveData<Int>
        get() = _min

    val sec : LiveData<Int>
        get() = _sec

    val serviceRunning : LiveData<Boolean>
        get() = _serviceRunning


    fun setMin(min: Int){
        _min.value = min
    }

    fun setSec(sec: Int){
        _sec.value = sec
    }

    fun setServiceRunning(serviceRunning: Boolean){
        _serviceRunning.value = serviceRunning
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