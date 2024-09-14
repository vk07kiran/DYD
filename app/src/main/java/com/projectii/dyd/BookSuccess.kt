package com.projectii.dyd


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class BookSuccess : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.book_success)

        // Initialize views
        val continueButton: Button = findViewById(R.id.continueButton)


        // Set onClickListener for the Continue button
        continueButton.setOnClickListener {
//            val intent = Intent(this, LoggedIN::class.java)
//            startActivity(intent)
              finish() // Optional: close the current activity
//            finishAffinity()
        }
    }
}
