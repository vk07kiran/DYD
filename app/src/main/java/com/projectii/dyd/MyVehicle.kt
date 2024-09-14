package com.projectii.dyd

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class MyVehicle : AppCompatActivity() {
    private lateinit var ModelNameTextView: TextView
    private lateinit var CategoryTextView: TextView
    private lateinit var PriceTextView: TextView
    private lateinit var LocationTextView: TextView
    private lateinit var VehicleImage: ImageView
    private lateinit var RenterProfileLayout: LinearLayout
    private lateinit var ViewRenterTextView: TextView

    private lateinit var deleteButton : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.my_vehicle)

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
        RenterProfileLayout = findViewById(R.id.renterProfile)
        ViewRenterTextView = findViewById(R.id.viewRenter)
        deleteButton = findViewById(R.id.deleteNow)


        // Fetch and display vehicle data if internet is available
        if (isInternetAvailable()) {
            fetchVehicleData()
        } else {
            showToast("No internet connection")
        }

        // Set click listener for delete button
        deleteButton.setOnClickListener {
            deleteVehicleData()
        }

    }

    private fun fetchVehicleData() {
        val db = FirebaseFirestore.getInstance()
        val vehicleRef = db.collection("Vehicles")

        // Retrieve vehicle ID from intent extra
        val vehicleID = intent.getStringExtra("vehicleId")

        // Retrieve userID
        val userID = intent.getStringExtra("UserID")

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
                    val vehicleBookerId = document.getString("Vehicle Booker ID")

                    // Update UI with retrieved data
                    ModelNameTextView.text = modelName
                    CategoryTextView.text = category
                    PriceTextView.text = "$price/Day"
                    LocationTextView.text = location

                    // Load vehicle image using Glide library
                    Glide.with(this@MyVehicle)
                        .load(imageUrl)
                        .into(VehicleImage)

                    // Check if the vehicle has a booker ID and show the RenterProfileLayout if it does
                    if (vehicleBookerId != null) {
                        RenterProfileLayout.visibility = LinearLayout.VISIBLE
                        deleteButton.visibility = Button.GONE

                        ViewRenterTextView.setOnClickListener {
                            val intent = Intent(this, PublicProfile::class.java)
                            intent.putExtra("Booker ID", vehicleBookerId)
                            intent.putExtra("vehicelID",vehicleID)
                            startActivity(intent)
                        }

                    } else {
                        RenterProfileLayout.visibility = LinearLayout.GONE
                    }
                } else {
                    println("No vehicle found with the given Vehicle ID.")
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching vehicle data: ${exception.message}")
            }
    }

    private fun deleteVehicleData() {
        val db = FirebaseFirestore.getInstance()
        val vehicleRef = db.collection("Vehicles")
        val userRef = db.collection("users")

        // Retrieve vehicle ID and userID from intent extras
        val vehicleID = intent.getStringExtra("vehicleId")
        val userID = intent.getStringExtra("userId")

        // Delete the vehicle from the Vehicles collection
        vehicleRef.whereEqualTo("Vehicle ID", vehicleID)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        vehicleRef.document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                showToast("Vehicle data deleted successfully")

                                // After deleting from Vehicles, delete vehicleID from the user's document
                                deleteUserVehicle(userID, vehicleID)
                            }
                            .addOnFailureListener { exception ->
                                println("Error deleting vehicle data: ${exception.message}")
                            }
                    }
                } else {
                    println("No vehicle found with the given Vehicle ID.")
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching vehicle data: ${exception.message}")
            }
    }

    private fun deleteUserVehicle(userID: String?, vehicleID: String?) {
        val userRef = FirebaseFirestore.getInstance().collection("users")

        userID?.let { uid ->
            vehicleID?.let { vid ->
                userRef.document(uid)
                    .update("Booked Vehicle", null)
                    .addOnSuccessListener {
                        showToast("Vehicle removed")
                        finish() // Optionally, you can close the activity after deletion
                    }
                    .addOnFailureListener { exception ->
                        println("Error updating user's data: ${exception.message}")
                    }
            } ?: run {
                println("Vehicle ID is null, cannot delete vehicle ID from user's record.")
            }
        } ?: run {
            println("User ID is null, cannot delete vehicle ID from user's record.")
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
