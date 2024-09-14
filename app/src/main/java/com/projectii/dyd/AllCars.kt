package com.projectii.dyd

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class AllCars : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var searchButton: Button
    private var selectedCategory: String? = null
    private lateinit var availabilityView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_cars)

        val backButton : ImageView = findViewById(R.id.backButton)

        backButton.setOnClickListener{
            finish()
        }



        // Initialize Spinner and Button and Linear Layout
        categorySpinner = findViewById(R.id.spinner_filter1)
        searchButton = findViewById(R.id.btn_search)

        availabilityView = findViewById(R.id.viewbox)

        // Set up the filter options
        setupSpinner()

        // Set up search button click listener
        searchButton.setOnClickListener {
            if (isNetworkAvailable()) {
                updateSelectedCategory()
                fetchFilteredVehiclesData()
            } else {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch data from Firestore initially
        if (isNetworkAvailable()) {
            fetchFilteredVehiclesData()
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinner() {
        // Define category options
        val categoryOptions = arrayOf("All Categories", "Electric Cars", "SUV Cars", "Petrol Cars", "Diesel Cars", "Hydrogen Cars") // Add more categories as needed

        // Set up adapter for the Spinner
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryOptions)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter
    }

    private fun updateSelectedCategory() {
        selectedCategory = categorySpinner.selectedItem?.toString()
    }

    private fun fetchFilteredVehiclesData() {
        val db = FirebaseFirestore.getInstance()
        val vehiclesRef = db.collection("Vehicles")

        // Create a query based on the default vehicle type and selected category
        var query = vehiclesRef
            .whereEqualTo("Vehicle Type", "Cars")
            .whereEqualTo("Vehicle Booker ID", null) // Add condition to check for null Vehicle Booker ID

        // Apply the category filter only if a specific category is selected
        selectedCategory?.let {
            if (it != "All Categories") {
                query = query.whereEqualTo("Category", it) // Assuming "Category" is the field name in firestore
            }
        }

        query.get()
            .addOnSuccessListener { result ->
                // Get the viewbox and parent layout
                val parentLayout = findViewById<LinearLayout>(R.id.car_collections_in_allcarpage)

                // Clear previous results
                parentLayout.removeAllViews()

                // Check if the result is empty
                if (result.isEmpty) {
                    // Show the viewbox if no data is available
                    availabilityView.visibility = View.VISIBLE
                } else {
                    // Hide the viewbox if data is present
                    availabilityView.visibility = View.GONE

                    // Loop through each document in the result
                    for (document in result) {
                        val modelName = document.getString("Model Name")
                        val location = document.getString("Location")
//                        val price = document.getString("Amount")
                        val vehicleID = document.getString("Vehicle ID")
                        val vehicleImgURL = document.getString("Images")

                        // Inflate the single_vehicle_view layout
                        val inflater = LayoutInflater.from(this)
                        val carLayout = inflater.inflate(R.layout.single_vehicle_view, null) as LinearLayout

                        // Get references to the views in single_vehicle_view
                        val modelNameTextView = carLayout.findViewById<TextView>(R.id.modelNameTextView)
                        val locationTextView = carLayout.findViewById<TextView>(R.id.locationTextView)
                        val vehicleImg = carLayout.findViewById<ImageView>(R.id.VehicleImageView)

                        // Set the text for each view
                        modelNameTextView.text = modelName
                        locationTextView.text = location

                        // Load the vehicle image using Glide
                        Glide.with(this@AllCars)
                            .load(vehicleImgURL)
                            .into(vehicleImg)

                        // Add the dynamically created view to the parent layout
                        parentLayout.addView(carLayout, 0)  // Add to the top of the parent layout

                        // Handle vehicle selection click
                        val goToChose: LinearLayout = carLayout.findViewById(R.id.ChooseVehicle)
                        goToChose.setOnClickListener {
                            val vehicleId2 = vehicleID
                            val userID = intent.getStringExtra("userId")

                            val intent1 = Intent(this, SingleVehicleChoice::class.java)
                            intent1.putExtra("vehicleId", vehicleId2)
                            intent1.putExtra("UserID", userID)
                            startActivity(intent1)
                            finish()
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching vehicles: ${exception.message}")
            }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
