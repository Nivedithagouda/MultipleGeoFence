package com.nivi.multiplegeofence.ui.route

// MapViewModel.kt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nivi.multiplegeofence.data.model.LatLngWithCustomer
import com.nivi.multiplegeofence.repository.GeofenceRepository

class RouteMapViewModel(private val mapRepository: GeofenceRepository) : ViewModel() {

    private val _pointsWithCustomers = MutableLiveData<List<LatLngWithCustomer>>()
    val pointsWithCustomers: LiveData<List<LatLngWithCustomer>> get() = _pointsWithCustomers

    fun fetchPointsWithCustomers() {
        _pointsWithCustomers.value = mapRepository.getPointsWithCustomers()
    }
}
