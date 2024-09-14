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

class SingleVehicleChoice : AppCompatActivity() {
    private lateinit var ModelNameTextView: TextView
    private lateinit var CategoryTextView: TextView
    private lateinit var PriceTextView: TextView
    private lateinit var LocationTextView: TextView
    private lateinit var VehicleImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.single_vehicle_choice)

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

        // Fetch and display vehicle data if internet is available
        if (isInternetAvailable()) {
            fetchVehicleData()
        } else {
            showToast("No internet connection")
        }
    }

    private fun fetchVehicleData() {
        val db = FirebaseFirestore.getInstance()
        val vehicleRef = db.collection("Vehicles")
        val userRef = db.collection("users")

        // Retrieve vehicle ID and user ID from intent extra
        val vehicleID = intent.getStringExtra("vehicleId")
        val UsersID = intent.getStringExtra("UserID")

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

                    // Update UI with retrieved data
                    ModelNameTextView.text = modelName
                    CategoryTextView.text = category
                    PriceTextView.text = "Price: Rs. ${price}/Day"
                    LocationTextView.text = location

                    // Load vehicle image using Glide library
                    Glide.with(this@SingleVehicleChoice)
                        .load(imageUrl)
                        .into(VehicleImage)

                    // Check user documents for profile completeness
                    userRef.document(UsersID!!)
                        .get()
                        .addOnSuccessListener { userDocument ->
                            if (userDocument != null) {
                                val profilePic = userDocument.getString("ProfilePic")
                                val licensePic = userDocument.getString("LicensePic")
                                val citizenshipPic = userDocument.getString("CitizenshipPic")
                                val nidPic = userDocument.getString("NidPic")

                                // Check if any required picture is missing
                                if (profilePic == null || licensePic == null || citizenshipPic == null || nidPic == null) {
                                    // Disable the 'bookNow' button and show toast
                                    val goToChose: Button = findViewById(R.id.bookNow)

                                    goToChose.setOnClickListener {
                                        showToast("Upload Document from Account Setting to Book Vehicles")

                                    }

                                } else {
                                    // Allow booking if all documents are present
                                    val goToChose: Button = findViewById(R.id.bookNow)
                                    goToChose.setOnClickListener {
                                        val intent = Intent(this, BookVehicles::class.java)
                                        intent.putExtra("vehicleId", vehicleID)
                                        intent.putExtra("UserID", UsersID)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            println("Error fetching user data: ${exception.message}")
                        }
                } else {
                    println("No vehicle found with the given Vehicle ID.")
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching vehicle data: ${exception.message}")
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
