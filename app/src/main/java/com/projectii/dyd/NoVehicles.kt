package com.projectii.dyd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView

class NoVehicles : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.no_vehicles)

        val backButton : ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener{
            finish()
        }

        val continueButton: Button = findViewById(R.id.bookButton)
        continueButton.setOnClickListener {
            finish()
        }
    }
}
