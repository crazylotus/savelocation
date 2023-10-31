package com.example.recordtrack

import io.realm.annotations.PrimaryKey
import java.io.Serializable

class LocationDetails : Serializable {

    var id: Long = 0
    var userId: String? = null
    var longitude: Double? = null
    var latitude: Double? = null
    var location: String? = null
}