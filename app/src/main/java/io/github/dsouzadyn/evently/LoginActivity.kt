package io.github.dsouzadyn.evently

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.auth0.android.jwt.JWT

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.longToast


class LoginActivity : AppCompatActivity() {

    private val APP_TAG = "LOGIN_ACTIVITY"
    private val REQUEST_SIGNUP = 0

    data class User(val email: String= "", val id: String = "", val roll_number: Int, val semester: Int, val username: String = "", val confirmed: Boolean, val role: Int)
    data class Token(val jwt: String = "", val user: User) {
        class Deserializer: ResponseDeserializable<Token> {
            override fun deserialize(content: String): Token? = Gson().fromJson(content, Token::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtn.setOnClickListener {
            loginHandler()
        }
        signUpLink.setOnClickListener {
            signUpLinkHandler()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == Activity.RESULT_OK) {
                longToast("User Registered Successfully")
            }
        }
    }

    private fun loginHandler() {
        loginBtn.isEnabled = false
        if(!validate()) {
            onLoginFailed()
            return
        }
        val progressDialog = indeterminateProgressDialog("Loading...")
        progressDialog.show()

        "${getString(R.string.base_api_url)}/auth/local".httpPost()
                .body("identifier=${loginEmail.text.toString()}&password=${loginPassword.text.toString()}")
                .responseObject(Token.Deserializer()) {_, _, result ->
                    val (token, error) = result
                    if (error == null) {
                        Log.d(APP_TAG, token?.jwt)
                        Log.d(APP_TAG, token?.user?.email)
                        val sharedPref = getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString(getString(R.string.token_key), token?.jwt)
                        editor.putString(getString(R.string.uid_key), token?.user?.id)
                        Log.d("UNAME", token?.user?.username)
                        editor.putString(getString(R.string.uname_key), token?.user?.username)
                        if(token?.user?.confirmed == true)
                            editor.putString(getString(R.string.conf_key), "CONF")
                        editor.putInt(getString(R.string.urole_key), token?.user?.role!!)
                        editor.apply()
                        progressDialog.dismiss()
                        onLoginSuccess()
                    } else {
                        Log.d(APP_TAG, error!!.message)
                        progressDialog.dismiss()
                        onLoginFailed()
                    }
                }
    }



    private fun signUpLinkHandler() {
        intent = Intent(applicationContext, SignUpActivity::class.java)
        startActivityForResult(intent, REQUEST_SIGNUP)
    }

    private fun onLoginFailed() {
        longToast("Failed to Login")
        loginBtn.isEnabled = true
    }

    private fun onLoginSuccess() {
        this.setResult(420)
        loginBtn.isEnabled = true
        this.finish()
    }

    private fun validate(): Boolean {

        var valid = true

        val email = loginEmail.text.toString()
        val password = loginPassword.text.toString()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.error = "Enter a valid email address"
            valid = false
        } else {
            loginEmail.error = null
        }

        if (password.isEmpty() || password.length < 4 || password.length > 16) {
            loginPassword.error = "The password should be between 4 and 16 characters"
            valid = false
        } else {
            loginPassword.error = null
        }

        return valid
    }
}
