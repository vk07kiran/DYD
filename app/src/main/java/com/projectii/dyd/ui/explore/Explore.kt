package com.projectii.dyd.ui.explore

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.projectii.dyd.AddBike
import com.projectii.dyd.AddBycycle
import com.projectii.dyd.AddCars
import com.projectii.dyd.LoginPage
import com.projectii.dyd.MyVehicle
import com.projectii.dyd.R
import com.projectii.dyd.SingleVehicleChoice
import com.projectii.dyd.databinding.FragmentExploreBinding

class Explore : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        val root: View = binding.root

        swipeRefreshLayout = binding.root.findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            reloadFragment()  // Reload the fragment when the user swipes down
        }


        val userID = activity?.intent?.getStringExtra("userId") // Receive ID of Logged In user

        if (!isConnected()) {
            redirectToLogin()
        } else {
            setupUI(userID)
            fetchFilteredVehiclesData(userID)
        }

        return root
    }

    private fun reloadFragment() {
        parentFragmentManager.beginTransaction().detach(this).commit()  // First detach the fragment
        parentFragmentManager.beginTransaction().attach(this).commit()  // Then attach the fragment again
        swipeRefreshLayout.isRefreshing = false  // Stop the refreshing animation
    }

    private fun setupUI(userID: String?) {
        val goToAddBycycle: ImageView = binding.bycycleImage
        goToAddBycycle.setOnClickListener {
            val intent = Intent(activity, AddBycycle::class.java)
            intent.putExtra("userId", userID)
            startActivity(intent)
        }

        val goToAddBike: ImageView = binding.bikeImage
        goToAddBike.setOnClickListener {
            val intent = Intent(activity, AddBike::class.java)
            intent.putExtra("userId", userID)
            startActivity(intent)
        }

        val goToAddCars: ImageView = binding.carImage
        goToAddCars.setOnClickListener {
            val intent = Intent(activity, AddCars::class.java)
            intent.putExtra("userId", userID)
            startActivity(intent)
        }
    }

    private fun fetchFilteredVehiclesData(userID: String?) {
        val db = FirebaseFirestore.getInstance()
        val vehiclesRef = db.collection("Vehicles")

        // Create a query based on the UserID
        val query = vehiclesRef.whereEqualTo("UserID", userID)

        query.get()
            .addOnSuccessListener { result ->
                // Clear previous results
                val parentLayout = binding.myVehiclesCollections
                parentLayout.removeAllViews()

                for (document in result) {
                    val modelName = document.getString("Model Name")
                    val location = document.getString("Location")
//                    val price = document.getString("Amount")
                    val vehicleID = document.getString("Vehicle ID")
                    val vehicleImgURL = document.getString("Images")

                    // Inflate the single_vehicle_view layout
                    val inflater = LayoutInflater.from(context)
                    val carLayout = inflater.inflate(R.layout.single_vehicle_view, null) as LinearLayout

                    // Get references to the views in single_vehicle_view
                    val modelNameTextView = carLayout.findViewById<TextView>(R.id.modelNameTextView)
                    val locationTextView = carLayout.findViewById<TextView>(R.id.locationTextView)
                    val vehicleImg = carLayout.findViewById<ImageView>(R.id.VehicleImageView)

                    // Set the text for each view
                    modelNameTextView.text = modelName
                    locationTextView.text = location

                    Glide.with(this@Explore)
                        .load(vehicleImgURL)
                        .into(vehicleImg)

                    // Add the dynamically created view to the parent layout
                    parentLayout.addView(carLayout, 0)  // Add to the top of the parent layout

                    val goToChose: LinearLayout = carLayout.findViewById(R.id.ChooseVehicle)
                    goToChose.setOnClickListener {
                        val vehicleId2 = vehicleID

                        // Create an Intent to navigate to SingleVehicleChoice activity and send the vehicle ID
                        val intent = Intent(activity, MyVehicle::class.java)
                        intent.putExtra("vehicleId", vehicleId2)
                        intent.putExtra("userId",userID)
                        startActivity(intent)
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching vehicles: ${exception.message}")
                redirectToLogin()
            }
    }

    private fun redirectToLogin() {
        Toast.makeText(activity, "No internet connection", Toast.LENGTH_SHORT).show()
//        val intent = Intent(activity, LoginPage::class.java)
//        startActivity(intent)
//        activity?.finish()
    }

    private fun isConnected(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
