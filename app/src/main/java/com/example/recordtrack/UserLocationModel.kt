package com.example.recordtrack


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class UserLocationModel : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var userId: String? = null
    var longitude: Double? = null
    var latitude: Double? = null
    var location: String? = null


}