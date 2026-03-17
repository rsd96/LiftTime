package r2.studios.lifttime.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    val min = MutableLiveData<Int>()
    val sec = MutableLiveData<Int>()
    private val _serviceRunning = MutableLiveData<Boolean>()
    val serviceRunning : LiveData<Boolean>
        get() = _serviceRunning

    init {
        min.value = 0
        sec.value = 0
        _serviceRunning.value = false
    }

    fun setServiceRunning(serviceRunning: Boolean){
        _serviceRunning.value = serviceRunning
    }

    fun changeServiceStatus() {
        _serviceRunning.value = _serviceRunning.value != true
    }
}
