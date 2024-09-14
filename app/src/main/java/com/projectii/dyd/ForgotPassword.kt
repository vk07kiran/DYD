package com.projectii.dyd


import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var overlayView: View
    private lateinit var etEmail: TextView
    private lateinit var etResetButton: Button
    private lateinit var backPage: ImageButton


    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password) // Assuming this is the layout file

        etEmail = findViewById(R.id.emailEditText)
        etResetButton = findViewById(R.id.resetButton)
        overlayView = findViewById(R.id.overlayView)

        auth = FirebaseAuth.getInstance()


        backPage = findViewById(R.id.goback)
        backPage.setOnClickListener {
            finish()
        }

        etResetButton.setOnClickListener{

            overlayView.visibility = View.VISIBLE

            handleReset()
        }
    }

    private fun handleReset(){
        val sendEmail = etEmail.text.toString()

        if (sendEmail.isEmpty()){
            Toast.makeText(this,"Please Enter Your Email",Toast.LENGTH_SHORT).show()
            overlayView.visibility = View.GONE
            return
        }

        auth.sendPasswordResetEmail(sendEmail)
            .addOnSuccessListener {
                Toast.makeText(this, "Please Check Your Email", Toast.LENGTH_SHORT).show()
                overlayView.visibility = View.GONE

                clearInputs()
            }
            .addOnFailureListener{
                Toast.makeText(this, "Error occurred, Please Try Again Later.", Toast.LENGTH_SHORT).show()
                overlayView.visibility = View.GONE

                clearInputs()

            }
    }
    private fun clearInputs() {
        etEmail.text = ""
    }

}
