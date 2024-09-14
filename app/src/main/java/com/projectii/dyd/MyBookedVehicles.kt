package com.projectii.dyd

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class MyBookedVehicles : AppCompatActivity() {
    private lateinit var ModelNameTextView: TextView
    private lateinit var CategoryTextView: TextView
    private lateinit var PriceTextView: TextView
    private lateinit var LocationTextView: TextView
    private lateinit var VehicleImage: ImageView
    private lateinit var removeNowButton: Button

    private lateinit var ownerContactView: TextView

    private lateinit var overlayView: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.my_booked_vehicles)

        overlayView = findViewById(R.id.overlayView)


        val backButton : ImageView = findViewById(R.id.backButton)

        backButton.setOnClickListener{
            finish()
        }

        // Initialize views
        ModelNameTextView = findViewById(R.id.VehicleModelName)
        CategoryTextView = findViewById(R.id.VehicleCategory)
        PriceTextView = findViewById(R.id.VehiclePrice)
        LocationTextView = findViewById(R.id.VehicleLocation)
        VehicleImage = findViewById(R.id.VehicleImageShow)
        removeNowButton = findViewById(R.id.removeNow)

        ownerContactView = findViewById(R.id.ownerContact)

        // Fetch and display vehicle data if internet is available
        if (isInternetAvailable()) {
            fetchUserVehicleID()
        } else {
            showToast("No internet connection")
        }

        // Set onClick listener for the removeNow button
        removeNowButton.setOnClickListener {
            removeBookedVehicle()
        }
    }

    private fun fetchUserVehicleID() {
        val db = FirebaseFirestore.getInstance()
        val UsersID = intent.getStringExtra("userId")

        if (UsersID != null) {
            // Query Firestore to get vehicleID from the users table
            db.collection("users").document(UsersID).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val vehicleID = document.getString("Booked Vehicle")
                        if (vehicleID != null) {
                            fetchVehicleData(vehicleID)
                        } else {
                            showToast("No vehicles Booked.")
                            val intent = Intent(this, NoVehicles::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        showToast("User not found.")
                        finish()
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Error fetching user data: ${exception.message}")
                    finish()
                }
        } else {
            showToast("User ID is missing.")
        }
    }

    private fun fetchVehicleData(vehicleID: String) {
        val db = FirebaseFirestore.getInstance()
        val vehicleRef = db.collection("Vehicles")

        val owner = db.collection("users")

        // Query Firestore for the specific vehicle
        vehicleRef.whereEqualTo("Vehicle ID", vehicleID)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val modelName = document.getString("Model Name")
                    val category = document.getString("Category")
                    val price = document.getString("Amount")
                    val location = document.getString("Location")
                    val imageUrl = document.getString("Images")
                    val ownerContact = document.getString("Esewa Number")

                    // Update UI with retrieved data
                    ModelNameTextView.text = modelName
                    CategoryTextView.text = category
                    PriceTextView.text = "Price: $price/Day"
                    LocationTextView.text = location
                    ownerContactView.text = ownerContact

                    // Load vehicle image using Glide library
                    Glide.with(this@MyBookedVehicles)
                        .load(imageUrl)
                        .into(VehicleImage)
                } else {
                    showToast("No vehicle found.")
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error occurred: ${exception.message}")
                finish()
            }
    }

    private fun removeBookedVehicle() {
        val db = FirebaseFirestore.getInstance()
        val userID = intent.getStringExtra("userId")

        if (userID != null) {
            // Get the user's document to retrieve the "Booked Vehicle" field
            db.collection("users").document(userID).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val vehicleID = document.getString("Booked Vehicle")

                        if (vehicleID != null) {
                            // Prepare the fields to be updated for users collection
                            val userUpdates = hashMapOf<String, Any?>(
                                "Booked Vehicle" to null,      // Remove the "Booked Vehicle" field
                                "Payment Status" to null,      // Clear the payment status
                                "Booking Time" to null         // Clear the booking time
                            )

                            // Firestore batch for atomic updates
                            db.runBatch { batch ->
                                // Update the user's document
                                val userRef = db.collection("users").document(userID)
                                batch.update(userRef, userUpdates)

                                // Update the vehicle's document to set "Booker ID" to null
                                val vehicleRef = db.collection("Vehicles").document(vehicleID)
                                batch.update(vehicleRef, "Vehicle Booker ID", null)
                            }.addOnSuccessListener {
                                showToast("Vehicle booking removed successfully.")
                                finish()
                            }.addOnFailureListener { exception ->
                                showToast("Error occurred: ${exception.message}")
                                finish()
                            }
                        } else {
                            showToast("No vehicles booked.")
                            finish()
                        }
                    } else {
                        showToast("Error Occurred.")
                        finish()
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Error Occurred: ${exception.message}")
                    finish()
                }
        } else {
            showToast("Error Occurred.")
            finish()
        }
    }



    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
