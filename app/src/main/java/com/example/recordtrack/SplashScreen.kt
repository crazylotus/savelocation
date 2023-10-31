package com.example.recordtrack

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log

class SplashScreen : BaseActivity(){


    lateinit var userId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("application", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("user_id","").toString()

        Log.e("splash","user id ----> $userId")


        Handler().postDelayed(Runnable {
            var intent : Intent
            if(userId.equals(""))
             intent = Intent(this,LoginActivity::class.java)
            else
              intent = Intent(this,MainActivity::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Apply activity transition
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            } else {
                // Swap without transition
                startActivity(intent)
            }
            finish()
        },3000)

    }
}