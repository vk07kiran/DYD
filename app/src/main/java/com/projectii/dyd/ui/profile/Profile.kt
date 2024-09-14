package com.projectii.dyd.ui.profile

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.projectii.dyd.AddCars
import com.projectii.dyd.AllCars
import com.projectii.dyd.EditProfile
import com.projectii.dyd.LoginPage
import com.projectii.dyd.R
import com.projectii.dyd.databinding.FragmentProfileBinding

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var personalinfo: LinearLayout
    private lateinit var documents: LinearLayout
    private lateinit var personalinfobtn: TextView
    private lateinit var documentsbtn: TextView
    private lateinit var fullNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var locationTextView: TextView

    private lateinit var profileImageView: ImageView
    private lateinit var citizenshipImageView: ImageView
    private lateinit var licenseImageView: ImageView
    private lateinit var nidImageView: ImageView

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        swipeRefreshLayout = binding.root.findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            reloadFragment()  // Reload the fragment when the user swipes down
        }


        val userID = activity?.intent?.getStringExtra("userId")

        val goToEditProfile:TextView = binding.editProfileButton
        goToEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfile::class.java)
            intent.putExtra("userId", userID)
            startActivity(intent)
        }

        val logOut:TextView = binding.logoutButton
        logOut.setOnClickListener {
//            val intent = Intent(requireContext(), LoginPage::class.java)
//            startActivity(intent)
            activity?.finish()
        }

        if (!isConnected()) {
            redirectToLogin()
        } else {
            setupUI()
            fetchUserData(userID)
        }

        return root
    }
    private fun reloadFragment() {
        parentFragmentManager.beginTransaction().detach(this).commit()  // First detach the fragment
        parentFragmentManager.beginTransaction().attach(this).commit()  // Then attach the fragment again
        swipeRefreshLayout.isRefreshing = false  // Stop the refreshing animation
    }

    private fun setupUI() {
        personalinfo = binding.personalinfo
        documents = binding.documents
        personalinfobtn = binding.personalinfobtn
        documentsbtn = binding.documentsbtn
        fullNameTextView = binding.userFullname
        emailTextView = binding.userEmail
        phoneTextView = binding.userPhone
        locationTextView = binding.userAddress

        profileImageView = binding.profileImage
        citizenshipImageView = binding.citizenshipImage
        licenseImageView = binding.licenseImage
        nidImageView = binding.nidImage

        // Making personal info visible
        personalinfo.visibility = View.VISIBLE
        documents.visibility = View.GONE

        personalinfobtn.setOnClickListener {
            personalinfo.visibility = View.VISIBLE
            documents.visibility = View.GONE
            personalinfobtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
            documentsbtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        documentsbtn.setOnClickListener {
            personalinfo.visibility = View.GONE
            documents.visibility = View.VISIBLE
            personalinfobtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            documentsbtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
        }


    }

    private fun fetchUserData(userID: String?) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("users")

        // Query to fetch the user with UserID
        userID?.let {
            usersRef.whereEqualTo("UserID", it).get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val fullName = document.getString("Full Name")
                        val email = document.getString("Email")
                        val phone = document.getString("Phone")
                        val address = document.getString("Location")
                        val profilepic = document.getString("ProfilePic")
                        val citizenship = document.getString("CitizenshipPic")
                        val license = document.getString("LicensePic")
                        val nid = document.getString("NidPic")

                        fullNameTextView.text = fullName
                        emailTextView.text = email
                        phoneTextView.text = phone
                        locationTextView.text = address


                        Glide.with(this)
                            .load(profilepic)
                        //    .apply(RequestOptions().placeholder(R.drawable.placeholder)) // placeholder image
                            .into(profileImageView)

                        Glide.with(this)
                            .load(citizenship)
                       //     .apply(RequestOptions().placeholder(R.drawable.placeholder)) // placeholder image
                            .into(citizenshipImageView)

                        Glide.with(this)
                            .load(license)
                        //    .apply(RequestOptions().placeholder(R.drawable.placeholder)) // placeholder image
                            .into(licenseImageView)

                        Glide.with(this)
                            .load(nid)
                        //    .apply(RequestOptions().placeholder(R.drawable.placeholder)) // placeholder image
                            .into(nidImageView)

                    }
                }
                .addOnFailureListener { exception ->
                    println("Error fetching user: ${exception.message}")
                    redirectToLogin()
                }
        }
    }

    private fun redirectToLogin() {
        Toast.makeText(activity, "No internet connection...", Toast.LENGTH_SHORT).show()
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
