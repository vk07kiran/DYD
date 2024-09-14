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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.f1soft.esewapaymentsdk.EsewaConfiguration
import com.f1soft.esewapaymentsdk.EsewaPayment
import com.f1soft.esewapaymentsdk.ui.screens.EsewaPaymentActivity
import com.google.firebase.firestore.FirebaseFirestore

class BookVehicles : AppCompatActivity() {

    private lateinit var registerActivity: ActivityResultLauncher<Intent>
    private lateinit var firestore: FirebaseFirestore

    private lateinit var overlayView: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.book_vehicle)


        val backButton : ImageView = findViewById(R.id.backButton)

        backButton.setOnClickListener{
            finish()
        }


        overlayView = findViewById(R.id.overlayView)


        val userID = intent.getStringExtra("UserID")
        val vehicleID = intent.getStringExtra("vehicleId")

        firestore = FirebaseFirestore.getInstance()

        if (vehicleID != null) {
            fetchVehicleDetails(vehicleID)
        }

        if (userID != null) {
            fetchOwnerContact(userID)
        }

        // Register activity result callback before any asynchronous operations
        registerActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ::onResultCallback
        )

        val esewaPaymentButton: Button = findViewById(R.id.esewaPayment)
        esewaPaymentButton.setOnClickListener {
            if (userID != null && vehicleID != null) {
                overlayView.visibility = View.VISIBLE

                if (isInternetAvailable()) {
                    checkAndProceedWithEsewaPayment(userID, vehicleID)
                } else {
                    showToast("No internet connection")
                    overlayView.visibility = View.GONE

                }
            } else {
                showToast("User ID or Vehicle ID is missing")
                overlayView.visibility = View.GONE

                finish()
            }
        }

        val paidBookButton: Button = findViewById(R.id.PaidBook)
        paidBookButton.setOnClickListener {
            overlayView.visibility = View.VISIBLE

            if (userID != null && vehicleID != null) {
                if (isInternetAvailable()) {
                    checkAndUpdateUserData(userID, vehicleID)
                } else {
                    showToast("No internet connection")
                    overlayView.visibility = View.GONE

                }
            } else {
                showToast("User ID is missing")
                overlayView.visibility = View.GONE

                finish()
            }
        }
    }

    private fun fetchVehicleDetails(vehicleID: String) {
        val vehicleRef = firestore.collection("Vehicles").document(vehicleID)

        vehicleRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val modelName = document.getString("Model Name")
                    val pricePerDay = document.getString("Amount")
                    val vehicleLocation = document.getString("Location")

                    findViewById<TextView>(R.id.modelName).text = modelName
                    findViewById<TextView>(R.id.pricePerDay).text = pricePerDay
                    findViewById<TextView>(R.id.vehicleLocation).text = vehicleLocation
                } else {
                    showToast("No vehicle found with the given Vehicle ID.")
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error fetching vehicle data: ${exception.message}")
            }
    }

    private fun fetchOwnerContact(userID: String) {
        val userRef = firestore.collection("users").document(userID)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val ownerContact = document.getString("Phone")
                    findViewById<TextView>(R.id.ownerContact).text = ownerContact
                } else {
                    showToast("No user found with the given User ID.")
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error fetching owner contact: ${exception.message}")
            }
    }

    private fun checkAndProceedWithEsewaPayment(userID: String, vehicleID: String) {
        val userRef = firestore.collection("users").document(userID)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val bookedVehicle = document.getString("Booked Vehicle")

                    if (bookedVehicle.isNullOrEmpty()) {
                        firestore.collection("Vehicles").document(vehicleID).get()
                            .addOnSuccessListener { vehicleDoc ->
                                if (vehicleDoc.exists()) {
                                    val vehicleBookerId = vehicleDoc.getString("Vehicle Booker ID")
                                    if (vehicleBookerId.isNullOrEmpty()) {
                                        val amount = vehicleDoc.getString("Amount") ?: "0.0"
                                        val esewaNumb = vehicleDoc.getString("Esewa Number") ?: "Unknown Esewa"

                                        val eSewaConfiguration = EsewaConfiguration(
                                            clientId = "JB0BBQ4aD0UqIThFJwAKBgAXEUkEGQUBBAwdOgABHD4DChwUAB0R",
                                            secretKey = "BhwIWQQADhIYSxILExMcAgFXFhcOBwAKBgAXEQ==",
                                            environment = EsewaConfiguration.ENVIRONMENT_TEST
                                        )

                                        val eSewaPayment = EsewaPayment(
                                            amount,
                                            esewaNumb,
                                            userID
                                        )

                                        registerActivity.launch(
                                            Intent(this, EsewaPaymentActivity::class.java).apply {
                                                putExtra(EsewaConfiguration.ESEWA_CONFIGURATION, eSewaConfiguration)
                                                putExtra(EsewaPayment.ESEWA_PAYMENT, eSewaPayment)
                                            }
                                        )
                                    } else {
                                        showToast("This vehicle is already booked by another user.")
                                        overlayView.visibility = View.GONE

                                    }
                                } else {
                                    showToast("Vehicle details not found.")
                                    overlayView.visibility = View.GONE

                                }
                            }
                            .addOnFailureListener { e ->
                                showToast("Error fetching vehicle data: ${e.message}")
                                overlayView.visibility = View.GONE

                            }
                    } else {
                        showToast("Can't book more than one Vehicle at a time.")
                        overlayView.visibility = View.GONE

                    }
                } else {
                    showToast("No user found with the given User ID.")
                    overlayView.visibility = View.GONE

                }
            }
            .addOnFailureListener { exception ->
                showToast("Error fetching user data: ${exception.message}")
                overlayView.visibility = View.GONE

            }
    }


    private fun onResultCallback(result: ActivityResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                result.data?.getStringExtra(EsewaPayment.EXTRA_RESULT_MESSAGE)?.let { s ->
                    Toast.makeText(this, "SUCCESSFUL PAYMENT", Toast.LENGTH_SHORT).show()
                    overlayView.visibility = View.GONE

                }

                val userID = intent.getStringExtra("UserID") ?: return
                saveBookedVehicle(userID)

                val intent = Intent(this, BookSuccess::class.java)
                startActivity(intent)
                finish()
            }
            RESULT_CANCELED -> {
                Toast.makeText(this, "Canceled By User", Toast.LENGTH_SHORT).show()
                overlayView.visibility = View.GONE

            }
            EsewaPayment.RESULT_EXTRAS_INVALID -> {
                result.data?.getStringExtra(EsewaPayment.EXTRA_RESULT_MESSAGE)?.let { s ->
                    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveBookedVehicle(userID: String) {
        val vehicleID = intent.getStringExtra("vehicleId")
        val userRef = firestore.collection("users").document(userID)
        val vehicleRef = firestore.collection("Vehicles").document(vehicleID!!)

        // Update user's booked vehicle and payment status
        firestore.runBatch { batch ->
            batch.update(userRef, mapOf(
                "Booked Vehicle" to vehicleID,
                "Payment Status" to "Paid",
                "Booking Time" to System.currentTimeMillis()
            ))

            // Update vehicle's booker ID
            batch.update(vehicleRef, mapOf(
                "Vehicle Booker ID" to userID
            ))
        }.addOnSuccessListener {
            Toast.makeText(this, "Vehicle booked successfully!", Toast.LENGTH_SHORT).show()
            overlayView.visibility = View.GONE

        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error updating data: ${e.message}", Toast.LENGTH_SHORT).show()
            overlayView.visibility = View.GONE

        }
    }


    private fun checkAndUpdateUserData(userID: String, vehicleID: String) {
        val userRef = firestore.collection("users").document(userID)

        userRef.get()
            .addOnSuccessListener { document ->
                val bookedVehicle = document.getString("Booked Vehicle")
                if (bookedVehicle.isNullOrEmpty()) {
                    // Check if the vehicle is already booked by another user
                    firestore.collection("Vehicles").document(vehicleID).get()
                        .addOnSuccessListener { vehicleDoc ->
                            if (vehicleDoc.exists()) {
                                val vehicleBookerId = vehicleDoc.getString("Vehicle Booker ID")
                                if (vehicleBookerId.isNullOrEmpty()) {
                                    // Proceed to update user data
                                    updateUserData(userID)
                                } else {
                                    showToast("This vehicle is already booked by another user.")
                                    overlayView.visibility = View.GONE

                                }
                            } else {
                                showToast("Vehicle details not found.")
                                overlayView.visibility = View.GONE

                            }
                        }
                        .addOnFailureListener { e ->
                            showToast("Error fetching vehicle data: ${e.message}")
                            overlayView.visibility = View.GONE

                        }
                } else {
                    showToast("Can't book more than one Vehicle at a time.")
                    overlayView.visibility = View.GONE

                }
            }
            .addOnFailureListener { exception ->
                showToast("Error fetching user data: ${exception.message}")
                overlayView.visibility = View.GONE

            }
    }


    private fun updateUserData(userID: String) {
        val vehicleID = intent.getStringExtra("vehicleId")
        val userRef = firestore.collection("users").document(userID)
        val vehicleRef = firestore.collection("Vehicles").document(vehicleID!!)

        // Additional data to add to the user
        val bookingTime = System.currentTimeMillis() // Example of booking time

        // Perform a batch update to update both the user and vehicle documents
        firestore.runBatch { batch ->
            // Update user's "Booked Vehicle" and add more data like "Booking Time"
            batch.update(userRef, mapOf(
                "Booked Vehicle" to vehicleID,
                "Booking Time" to bookingTime // Add your extra data here
            ))

            // Update vehicle's "Booker ID" field
            batch.update(vehicleRef, "Vehicle Booker ID", userID)
        }.addOnSuccessListener {
            Toast.makeText(this, "Vehicle Booked!!!", Toast.LENGTH_SHORT).show()
            overlayView.visibility = View.GONE

            val intent = Intent(this, BookSuccess::class.java)
            startActivity(intent)
            finish()
        }.addOnFailureListener { exception ->
            showToast("Error updating data: ${exception.message}")
            overlayView.visibility = View.GONE
 
        }
    }



    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
