package com.example.recordtrack

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient

open class BaseActivity : AppCompatActivity() {
     lateinit var mGoogleSignInClient: GoogleSignInClient
     lateinit var sharedPreferences : SharedPreferences

}