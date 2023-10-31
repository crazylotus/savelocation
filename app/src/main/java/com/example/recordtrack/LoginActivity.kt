package com.example.recordtrack

import android.R.attr
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.recordtrack.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


class LoginActivity : BaseActivity() {


    private lateinit var binding : ActivityLoginBinding

    lateinit var resultLauncher : ActivityResultLauncher<Intent>

    var RC_SIGN_IN = 111



    lateinit var userId : String

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("application", Context.MODE_PRIVATE);

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        userId = sharedPreferences.getString("user_id","").toString()




        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        val account = GoogleSignIn.getLastSignedInAccount(this)

        binding.signInButton.setSize(SignInButton.SIZE_WIDE);


        binding.signInButton.setOnClickListener {
            Log.e("login","button clicked")
          //  signIn()

            openSomeActivityForResult()
        }

        //updateUI(account)

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        if(resultCode==RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
        Log.e("login","onAcitivity result")
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            var editor = sharedPreferences.edit();
            editor.putString("user_image_url",account.photoUrl.toString())
            editor.putString("user_name", account.displayName)
            editor.putString("user_id", account.id);
            editor.apply();


            var intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()


        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("google login", "signInResult:failed code=" + e.statusCode)
           // updateUI(null)
        }
    }

    fun openSomeActivityForResult() {
        val signInIntent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }



    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        Log.e("login","sign in funcion")
        startActivityForResult(signInIntent, RC_SIGN_IN)
        Log.e("login","function end")
    }
}