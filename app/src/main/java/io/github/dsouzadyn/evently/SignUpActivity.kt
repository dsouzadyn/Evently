package io.github.dsouzadyn.evently

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import com.github.kittinunf.fuel.httpPost
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.longToast

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        signUpBtn.setOnClickListener {
            signUpHandler()
        }
    }

    private fun signUpHandler() {
        signUpBtn.isEnabled = false
        if (!validate()) {
            onSignUpFailed()
            return
        }
        val progressDialog = indeterminateProgressDialog("Loading...")
        progressDialog.show()
        val name = signUpName.text.toString()
        val email = signUpEmail.text.toString()
        val password = signUpPassword.text.toString()
        val branch = signUpBranch.selectedItem.toString()
        val semester = signUpSemester.selectedItem.toString()
        val rollNumber = signUpRollNumber.text.toString()
        val college = signUpCollege.selectedItem.toString()
        val bodyString = "username=$name&email=$email&password=$password&" +
                "branch=$branch&semester=$semester&roll_number=$rollNumber&" +
                "college=$college"
        "http://192.168.1.6:1337/auth/local/register".httpPost()
                .body(bodyString)
                .responseString {_, response, result ->
                    val (_, error) = result
                    if (error == null) {
                        progressDialog.dismiss()
                        onSignUpSuccess()
                    } else {
                        progressDialog.dismiss()
                        onSignUpFailed()
                    }
                }
    }

    private fun onSignUpFailed() {
        longToast("Failed to Register User")
        signUpBtn.isEnabled = true
    }

    private fun onSignUpSuccess() {
        signUpBtn.isEnabled = true
        setResult(RESULT_OK, null)
        this.finish()
    }

    private fun validate(): Boolean {
        var valid = true

        val name = signUpName.text.toString()
        val email = signUpEmail.text.toString()
        val password = signUpPassword.text.toString()
        val branch = signUpBranch.selectedItem.toString()
        val semester = signUpSemester.selectedItem.toString().toInt()
        val college = signUpSemester.selectedItem.toString()

        if (name.isEmpty() || name.length < 4) {
            valid = false
            signUpName.error = "Name must have atleast 4 characters"
        } else {
            signUpName.error = null
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            valid = false
            signUpEmail.error = "Please enter a valid email address"
        } else {
            signUpEmail.error = null
        }

        if (password.isEmpty() || password.length < 4 || password.length > 16) {
            valid = false
            signUpPassword.error = "The password must be between 4 and 16 characters"
        } else {
            signUpPassword.error = null
        }

        if (branch.isEmpty()) {
            valid = false
            alert("Select a branch")
        }

        if (semester == null) {
            valid = false
            alert("Select a semester")
        }

        return valid
    }
}
