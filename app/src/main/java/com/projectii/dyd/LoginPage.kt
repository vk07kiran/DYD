package com.projectii.dyd

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var overlayView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.loginpage)

        // Initialize FirebaseAuth and FirebaseFirestore instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        etEmail = findViewById(R.id.emailEditText)
        etPassword = findViewById(R.id.passwordEditText)
        overlayView = findViewById(R.id.overlayView)

        // Set up "Go to Sign Up Page" button click listener
        findViewById<Button>(R.id.signupButton).setOnClickListener {
            startActivity(Intent(this, signuppage::class.java))
        }

        // Go to Forgot Password if password is forgotten
        findViewById<TextView>(R.id.forgotPassword).setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }

        // Set up "Login" button click listener
        findViewById<Button>(R.id.login).setOnClickListener {
            handleLogin()
        }

        // Handle window insets for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun handleLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Show overlay during login process
        overlayView.visibility = View.VISIBLE

        // Validate input fields
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please fill in all fields")
            overlayView.visibility = View.GONE
            return
        }

        // Check network availability
        if (!isNetworkAvailable()) {
            showToast("No internet connection")
            overlayView.visibility = View.GONE
            return
        }

        // Authenticate with Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {


                    val verification = auth.currentUser?.isEmailVerified

                    if (verification==true){
                        val user = auth.currentUser
                        handleSuccessfulLogin()

                    }
                    else
                        showToast("Please Verify Your Email Address.")
                        overlayView.visibility = View.GONE

                } else {
                    showToast("Invalid Username or Password")
                    overlayView.visibility = View.GONE
                }
            }
    }

    private fun handleSuccessfulLogin() {
        val user = auth.currentUser
        if (user != null) {
            val userID = user.uid
            db.collection("users").document(userID).get()
                .addOnSuccessListener { document ->
                    overlayView.visibility = View.GONE



                    if (document.exists()) {
                        val usersID = document.getString("UserID")
                        val intent = Intent(this, LoggedIN::class.java)
                        intent.putExtra("userId", usersID)
                        etEmail.text= null
                        etPassword.text= null
                        startActivity(intent)
                    } else {
                        showToast("Invalid Username or Password")
                    }
                }
                .addOnFailureListener {
                    showToast("Invalid Username or Password")
                    overlayView.visibility = View.GONE
                }
        }
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
