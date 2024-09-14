package com.projectii.dyd


import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.projectii.dyd.R

class PublicProfile : AppCompatActivity() {

    private lateinit var personalinfo: LinearLayout
    private lateinit var documents: LinearLayout
    private lateinit var personalinfobtn: TextView
    private lateinit var documentsbtn: TextView
    private lateinit var fullNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var paymentTextView: TextView

    private lateinit var profileImageView: ImageView
    private lateinit var citizenshipImageView: ImageView
    private lateinit var licenseImageView: ImageView
    private lateinit var nidImageView: ImageView

//    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.public_profile)

        // Retrieve vehicleID and bookerID from the intent
        val vehicleID = intent.getStringExtra("vehicelID")
        val bookerID = intent.getStringExtra("Booker ID")

        if (bookerID != null) {
            fetchUserDetails(bookerID)
        } else {
            showToast("No Booker ID provided")
        }

        val backButton : ImageView = findViewById(R.id.backButton)

        backButton.setOnClickListener{
            finish()
        }


        // Initialize views using findViewById
        personalinfo = findViewById(R.id.personalinfo)
        documents = findViewById(R.id.documents)
        personalinfobtn = findViewById(R.id.personalinfobtn)
        documentsbtn = findViewById(R.id.documentsbtn)
        fullNameTextView = findViewById(R.id.user_fullname)
        emailTextView = findViewById(R.id.user_email)
        phoneTextView = findViewById(R.id.user_phone)
        locationTextView = findViewById(R.id.user_address)
        paymentTextView = findViewById(R.id.paymentStatus)

        profileImageView = findViewById(R.id.profileImage)
        citizenshipImageView = findViewById(R.id.citizenshipImage)
        licenseImageView = findViewById(R.id.licenseImage)
        nidImageView = findViewById(R.id.nidImage)

        setupUI()

        // Optionally handle userID if needed
        val userID = intent.getStringExtra("userId")

    }

    private fun setupUI() {
        // Making personal info visible
        personalinfo.visibility = View.VISIBLE
        documents.visibility = View.GONE

        personalinfobtn.setOnClickListener {
            personalinfo.visibility = View.VISIBLE
            documents.visibility = View.GONE
            personalinfobtn.setTextColor(ContextCompat.getColor(this, R.color.blue))
            documentsbtn.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        documentsbtn.setOnClickListener {
            personalinfo.visibility = View.GONE
            documents.visibility = View.VISIBLE
            personalinfobtn.setTextColor(ContextCompat.getColor(this, R.color.black))
            documentsbtn.setTextColor(ContextCompat.getColor(this, R.color.blue))
        }
    }

    private fun fetchUserDetails(userID: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userID)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val fullName = document.getString("Full Name")
                    val email = document.getString("Email")
                    val phone = document.getString("Phone")
                    val location = document.getString("Location")
                    val profilePicUrl = document.getString("ProfilePic")
                    val citizenshipPicUrl = document.getString("CitizenshipPic")
                    val licensePicUrl = document.getString("LicensePic")
                    val nidPicUrl = document.getString("NidPic")
                    val paymentStatus = document.getString("Payment Status")

                    // Set the values to the TextViews
                    fullNameTextView.text = fullName ?: "Name not available"
                    emailTextView.text = email ?: "Email not available"
                    phoneTextView.text = phone ?: "Phone not available"
                    locationTextView.text = location ?: "Location not available"
                    paymentTextView.text = paymentStatus ?: "Not Paid"

                    // Load images using Glide
                    Glide.with(this)
                        .load(profilePicUrl)
                        .into(profileImageView)

                    Glide.with(this)
                        .load(citizenshipPicUrl)
                        .into(citizenshipImageView)

                    Glide.with(this)
                        .load(licensePicUrl)
                        .into(licenseImageView)

                    Glide.with(this)
                        .load(nidPicUrl)
                        .into(nidImageView)
                } else {
                    // Handle case where document does not exist
                    fullNameTextView.text = "No user found with the given ID"
                    emailTextView.text = ""
                    phoneTextView.text = ""
                    locationTextView.text = ""
                }
            }
            .addOnFailureListener { exception ->
                // Handle the failure case
                fullNameTextView.text = "Error fetching user data"
                emailTextView.text = ""
                phoneTextView.text = ""
                locationTextView.text = ""
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
