package com.projectii.dyd

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class AddBike : AppCompatActivity() {


    private lateinit var etFullModelName: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etNumberPlate: EditText
    private lateinit var etPurchasedDate: EditText
    private lateinit var etAmount : EditText
    private lateinit var etLocation : EditText
    private lateinit var etEsewa : EditText

    private lateinit var addBikeButton: Button
    private lateinit var imageView: ImageView
    private lateinit var buttonSelectImage: Button

    private val db = FirebaseFirestore.getInstance()
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_bike)


        val cancelButton : TextView = findViewById(R.id.canclebtn)

        cancelButton.setOnClickListener{
            finish()
        }


        etFullModelName = findViewById(R.id.fullBikeModelNameEditText)
        spinnerCategory = findViewById(R.id.spinner_filter3)
        etNumberPlate = findViewById(R.id.NumberPlateEditText)
        etPurchasedDate = findViewById(R.id.BikePurchasedDateEditText)
        etAmount = findViewById(R.id.amountEditText)
        etLocation = findViewById(R.id.LocationEditText)
        etEsewa = findViewById(R.id.esewaPlateEditText)

        addBikeButton = findViewById(R.id.addBike_button)
        imageView = findViewById(R.id.imageView)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)

        setupSpinner()

        buttonSelectImage.setOnClickListener {
            openFileChooser()
        }

        addBikeButton.setOnClickListener {
            addBike()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.bikes_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun openFileChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
            imageView.visibility = View.VISIBLE // Show imageView
        } else {
            imageView.visibility = View.GONE // Hide imageView
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun addBike() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }
        val modelName = etFullModelName.text.toString().trim()
        val categoryName = spinnerCategory.selectedItem.toString()
        val numberPlate = etNumberPlate.text.toString().trim()
        val purchasedDate = etPurchasedDate.text.toString().trim()
        val amount = etAmount.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val esewa = etEsewa.text.toString().trim()
        val overlayView = findViewById<View>(R.id.overlayView)
        overlayView.visibility = View.VISIBLE
        if (modelName.isEmpty() || amount.isEmpty() || location.isEmpty() || numberPlate.isEmpty() || purchasedDate.isEmpty() || categoryName.isEmpty() ||esewa.isEmpty()) {
            overlayView.visibility = View.GONE
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            uploadImageAndSaveVehicle(modelName, categoryName, numberPlate, purchasedDate, amount, location,esewa)
        } else {
            overlayView.visibility = View.GONE
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageAndSaveVehicle(modelName: String, categoryName: String, numberPlate: String, purchasedDate: String, amount: String, location: String, esewa: String) {
        val overlayView = findViewById<View>(R.id.overlayView)
        overlayView.visibility = View.VISIBLE

        val fileReference = storageReference.child("uploads/${System.currentTimeMillis()}.jpg")
        imageUri?.let {
            fileReference.putFile(it)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        saveVehicleToFirestore(modelName, categoryName, numberPlate, purchasedDate, amount, location,esewa, uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    overlayView.visibility = View.GONE
                    Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveVehicleToFirestore(modelName: String, categoryName: String, numberPlate: String, purchasedDate: String, amount: String, location: String, esewa:String, imageUrl: String) {
        val overlayView = findViewById<View>(R.id.overlayView)
        overlayView.visibility = View.VISIBLE
        val vehicleID = UUID.randomUUID().toString()
        val userID = intent.getStringExtra("userId")

        val vehicleMap = hashMapOf(
            "Vehicle ID" to vehicleID,
            "Vehicle Type" to "Bikes",
            "Model Name" to modelName,
            "Category" to categoryName,
            "Number Plate" to numberPlate,
            "Purchased Date" to purchasedDate,
            "Location" to location,
            "Amount" to amount,
            "Images" to imageUrl,
            "UserID" to userID,
            "Vehicle Booker ID" to null,
            "Esewa Number" to esewa
        )

        db.collection("Vehicles").document(vehicleID).set(vehicleMap)
            .addOnSuccessListener {
                overlayView.visibility = View.GONE
                Toast.makeText(this, "Vehicle added successfully!", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener { e ->
                overlayView.visibility = View.GONE
                Toast.makeText(this, "Error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        etFullModelName.text.clear()
        etNumberPlate.text.clear()
        etPurchasedDate.text.clear()
        etAmount.text.clear()
        etLocation.text.clear()
        etEsewa.text.clear()
        imageView.setImageResource(android.R.color.darker_gray)
    }
}
