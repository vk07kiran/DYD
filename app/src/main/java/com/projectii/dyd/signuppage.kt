package com.projectii.dyd

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class signuppage : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etUserName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var backPage: ImageButton
    private lateinit var overlayView: View

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signuppage)

        // Initialize views
        initializeViews()

        // Set click listener for the back button
        backPage.setOnClickListener {
            finish()
        }

        // Set click listener for the register button
        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun initializeViews() {
        etFullName = findViewById(R.id.fullNameEditText)
        etUserName = findViewById(R.id.userNameEditText)
        etPhone = findViewById(R.id.phoneEditText)
        etEmail = findViewById(R.id.emailEditText)
        etPassword = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.register)
        backPage = findViewById(R.id.goback)
        overlayView = findViewById(R.id.overlayView)
    }



    private fun registerUser() {
        val UFullName = etFullName.text.toString().trim()
        val UName = etUserName.text.toString().trim()
        val UPhone = etPhone.text.toString().trim()
        val UEmail = etEmail.text.toString().trim()
        val UPassword = etPassword.text.toString().trim()

        overlayView.visibility = View.VISIBLE

        // Validate input fields
        if (!validateInputs(UFullName, UName, UPhone, UEmail, UPassword)) {
            overlayView.visibility = View.GONE
            return
        }

        // Check network availability
        if (!isNetworkAvailable()) {
            showToast("No internet connection")
            overlayView.visibility = View.GONE
            return
        }

        // Create a user with email and password in Firebase Authentication
        auth.createUserWithEmailAndPassword(UEmail, UPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    handleUserRegistrationSuccess(UFullName, UName, UPhone, UEmail, UPassword)
                } else {
                    showToast("Failed: ${task.exception?.message}")
                    overlayView.visibility = View.GONE
                }
            }
    }

    private fun validateInputs(fullName: String, userName: String, phone: String, email: String, password: String): Boolean {
        return when {
            fullName.isEmpty() || userName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                showToast("Please fill in all fields")
                false
            }
            else -> true
        }
    }

    private fun handleUserRegistrationSuccess(fullName: String, userName: String, phone: String, email: String, password: String) {
        val user = auth.currentUser
        if (user != null) {
            val userID = user.uid

            // Create a map with user data including UserID
            val userMap = hashMapOf(
                "UserID" to userID,
                "Full Name" to fullName,
                "User Name" to userName,
                "Phone" to phone,
                "Email" to email,
                "Password" to password
            )

            // Add user data to Firestore
            db.collection("users").document(userID).set(userMap)
                .addOnSuccessListener {
                    showToast("Account created successfully! Check your Email.")
                    clearInputs()
                    overlayView.visibility = View.GONE

                    auth.currentUser?.sendEmailVerification()
                        ?.addOnSuccessListener {
                        }
                }
                .addOnFailureListener { e ->
                    showToast("Error occurred: ${e.message}")
                    overlayView.visibility = View.GONE
                }
        } else {
            showToast("User not found after registration")
            overlayView.visibility = View.GONE
            finish()
        }
    }

    private fun clearInputs() {
        etFullName.text.clear()
        etUserName.text.clear()
        etPhone.text.clear()
        etEmail.text.clear()
        etPassword.text.clear()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
