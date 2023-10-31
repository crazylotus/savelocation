package com.example.recordtrack

import android.app.Application
import io.realm.BuildConfig
import io.realm.Realm
import io.realm.RealmConfiguration



class RealmDB : Application() {
    override fun onCreate() {
        super.onCreate()

        // on below line we are
        // initializing our realm database.

        // on below line we are
        // initializing our realm database.

        Realm.init(this)
        val config: RealmConfiguration = RealmConfiguration.Builder() // below line is to allow write
            // data to database on ui thread.
            .allowWritesOnUiThread(true) // below line is to delete realm
            // if migration is needed.
            .deleteRealmIfMigrationNeeded() // at last we are calling a method to build.
            .build()
        // on below line we are setting
        // configuration to our realm database.
        // on below line we are setting
        // configuration to our realm database.
        Realm.setDefaultConfiguration(config)


    }
}