package com.projectii.dyd

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class EditProfile : AppCompatActivity() {

    private lateinit var overlayView: View

    private lateinit var DetailsEditLayout: LinearLayout
    private lateinit var PasswordEditsLayout: LinearLayout
    private lateinit var photosEditLayout: LinearLayout
    private lateinit var updateDetailsbtn: TextView
    private lateinit var updatePasswordbtn: TextView
    private lateinit var updateImagesbtn: TextView

    private lateinit var profilePic: ImageView
    private lateinit var citizenshipPic: ImageView
    private lateinit var licensePic: ImageView
    private lateinit var nidPic: ImageView

    private lateinit var profilePicButton: Button
    private lateinit var citizenshipPicButton: Button
    private lateinit var licensePicButton: Button
    private lateinit var nidPicButton: Button
    private lateinit var saveImagesButton: Button

    private lateinit var currentPasswordEditText: EditText
    private lateinit var changeDetailsButton: Button
    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var changePasswordButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private lateinit var imageToUpdate: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.edit_profile)

        val userID = intent.getStringExtra("userId")

        overlayView = findViewById(R.id.overlayView)


        DetailsEditLayout = findViewById(R.id.DetailsEditLayout)
        PasswordEditsLayout = findViewById(R.id.PasswordEditsLayout)
        photosEditLayout = findViewById(R.id.photosEditLayout)
        updateDetailsbtn = findViewById(R.id.updateDetailsbtn)
        updatePasswordbtn = findViewById(R.id.updatePasswordbtn)
        updateImagesbtn = findViewById(R.id.updateImagesbtn)

        profilePic = findViewById(R.id.imageProfile)
        citizenshipPic = findViewById(R.id.imageCitizenship)
        licensePic = findViewById(R.id.imageLicense)
        nidPic = findViewById(R.id.imageNID)

        profilePicButton = findViewById(R.id.Profilepic)
        citizenshipPicButton = findViewById(R.id.Citizenshippic)
        licensePicButton = findViewById(R.id.Licensepic)
        nidPicButton = findViewById(R.id.NIDpic)
        saveImagesButton = findViewById(R.id.saveImages)

        // Initialize UI elements for editing details
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText)
        changeDetailsButton = findViewById(R.id.changeDetails)

        // Initialize UI elements for changing password
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        changePasswordButton = findViewById(R.id.changePassword)

        // Set default visibility
        profilePic.visibility = View.GONE
        citizenshipPic.visibility = View.GONE
        licensePic.visibility = View.GONE
        nidPic.visibility = View.GONE

        // Set up buttons
        updateDetailsbtn.setOnClickListener {
            DetailsEditLayout.visibility = View.VISIBLE
            PasswordEditsLayout.visibility = View.GONE
            photosEditLayout.visibility = View.GONE
            updateDetailsbtn.setTextColor(ContextCompat.getColor(this, R.color.blue))
            updatePasswordbtn.setTextColor(ContextCompat.getColor(this, R.color.black))
            updateImagesbtn.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        updatePasswordbtn.setOnClickListener {
            DetailsEditLayout.visibility = View.GONE
            PasswordEditsLayout.visibility = View.VISIBLE
            photosEditLayout.visibility = View.GONE
            updateDetailsbtn.setTextColor(ContextCompat.getColor(this, R.color.black))
            updatePasswordbtn.setTextColor(ContextCompat.getColor(this, R.color.blue))
            updateImagesbtn.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        updateImagesbtn.setOnClickListener {
            DetailsEditLayout.visibility = View.GONE
            PasswordEditsLayout.visibility = View.GONE
            photosEditLayout.visibility = View.VISIBLE
            updateDetailsbtn.setTextColor(ContextCompat.getColor(this, R.color.black))
            updatePasswordbtn.setTextColor(ContextCompat.getColor(this, R.color.black))
            updateImagesbtn.setTextColor(ContextCompat.getColor(this, R.color.blue))
        }

        // Image selection handlers
        profilePicButton.setOnClickListener { selectImage(profilePic) }
        citizenshipPicButton.setOnClickListener { selectImage(citizenshipPic) }
        licensePicButton.setOnClickListener { selectImage(licensePic) }
        nidPicButton.setOnClickListener { selectImage(nidPic) }

        saveImagesButton.setOnClickListener {
            overlayView.visibility = View.VISIBLE

            if (userID != null) {
                uploadImages(userID)
            }
        }

        // Change details on button click
        changeDetailsButton.setOnClickListener {
            val fullName = findViewById<EditText>(R.id.fullNameEditText).text.toString().trim()
            val phoneNumber = findViewById<EditText>(R.id.phoneNumberEditText).text.toString().trim()
            val location = findViewById<EditText>(R.id.locationEditText).text.toString().trim()
            val enteredPassword = currentPasswordEditText.text.toString().trim()
            overlayView.visibility = View.VISIBLE

            // Check if password field is filled
            if (enteredPassword.isEmpty()) {
                showToast("Password is required.")
                overlayView.visibility = View.GONE
                return@setOnClickListener
            }

            // Only update if the password is correct
            if (userID != null) {
                verifyPasswordAndChangeDetails(userID, fullName, phoneNumber, location, enteredPassword)
            }
        }

        // Change password on button click
        changePasswordButton.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            overlayView.visibility = View.VISIBLE

            // Check if all fields are filled
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showToast("All fields are required.")
                overlayView.visibility = View.GONE
                return@setOnClickListener
            }

            // Only update password if the old password is correct and the new passwords match
            if (userID != null) {
                verifyAndChangePassword(userID, oldPassword, newPassword, confirmPassword)
            }
        }
    }

    private fun selectImage(imageView: ImageView) {
        imageToUpdate = imageView
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imageResultLauncher.launch(intent)
    }

    private val imageResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    imageToUpdate.setImageURI(uri)
                    imageToUpdate.visibility = View.VISIBLE
                    // Store the URI in the ImageView's tag for later use
                    imageToUpdate.tag = uri
                }
            }
        }

    private fun uploadImages(userID: String) {
        // Upload ProfilePic

        profilePic.drawable?.let {
            val uri = profilePic.tag as? Uri
            uploadImage(userID, "ProfilePic", uri, (profilePic.drawable as BitmapDrawable).bitmap) {
                profilePic.visibility = View.GONE
                profilePic.setImageDrawable(null)
            }
        }

        // Upload CitizenshipPic
        citizenshipPic.drawable?.let {

            val uri = citizenshipPic.tag as? Uri
            uploadImage(userID, "CitizenshipPic", uri, (citizenshipPic.drawable as BitmapDrawable).bitmap) {
                citizenshipPic.visibility = View.GONE
                citizenshipPic.setImageDrawable(null)

            }
        }

        // Upload LicensePic
        licensePic.drawable?.let {

            val uri = licensePic.tag as? Uri
            uploadImage(userID, "LicensePic", uri, (licensePic.drawable as BitmapDrawable).bitmap) {
                licensePic.visibility = View.GONE
                licensePic.setImageDrawable(null)

            }
        }

        // Upload NidPic
        nidPic.drawable?.let {

            val uri = nidPic.tag as? Uri
            uploadImage(userID, "NidPic", uri, (nidPic.drawable as BitmapDrawable).bitmap) {
                nidPic.visibility = View.GONE
                nidPic.setImageDrawable(null)

            }
        }
    }

    private fun uploadImage(userID: String, imageName: String, uri: Uri?, bitmap: Bitmap, onSuccess: () -> Unit) {
        val storageRef = storage.reference.child("users/$userID/$imageName.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = uri?.let { storageRef.putFile(it) } ?: storageRef.putBytes(data)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                saveImageUriToFirestore(userID, imageName, downloadUri.toString(), onSuccess)
            }
        }.addOnFailureListener {
            showToast("Failed to upload $imageName.")
            overlayView.visibility = View.GONE

        }
    }

    private fun saveImageUriToFirestore(userID: String, imageName: String, uri: String, onSuccess: () -> Unit) {
        val userRef = db.collection("users").document(userID)
        val updateData = mapOf(imageName to uri)

        userRef.update(updateData)
            .addOnSuccessListener {
                showToast("$imageName updated successfully.")
                overlayView.visibility = View.GONE

                onSuccess()
            }
            .addOnFailureListener {
                showToast("Failed to update $imageName in Firestore.")
                overlayView.visibility = View.GONE

                finish()
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun verifyPasswordAndChangeDetails(userID: String, fullName: String?, phoneNumber: String?, location: String?, enteredPassword: String) {
        // Fetch the user's document from Firestore
        db.collection("users").document(userID)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val storedPassword = document.getString("Password")

                    // Check if entered password matches the stored password
                    if (storedPassword == enteredPassword) {
                        val updates = mutableMapOf<String, Any>()

                        // Add fields to update only if they are not empty
                        if (!fullName.isNullOrEmpty()) updates["Full Name"] = fullName
                        if (!phoneNumber.isNullOrEmpty()) updates["Phone"] = phoneNumber
                        if (!location.isNullOrEmpty()) updates["Location"] = location

                        // If there are fields to update, proceed
                        if (updates.isNotEmpty()) {
                            db.collection("users").document(userID)
                                .update(updates)
                                .addOnSuccessListener {
                                    showToast("Details updated successfully.")
                                    overlayView.visibility = View.GONE


                                }
                                .addOnFailureListener { exception ->
                                    showToast("Failed to update details: ${exception.message}")
                                    overlayView.visibility = View.GONE

                                    finish()
                                }
                        } else {
                            showToast("No changes made.")
                            overlayView.visibility = View.GONE

                        }
                    } else {
                        showToast("Incorrect password. Please try again.")
                        overlayView.visibility = View.GONE

                    }
                } else {
                    showToast("User not found.")
                    finish()}
            }
            .addOnFailureListener { exception ->
                showToast("Failed to retrieve user data: ${exception.message}")

                finish()
            }
    }


    private fun verifyAndChangePassword(userID: String, oldPassword: String, newPassword: String, confirmPassword: String) {
        // Fetch the user's email from Firestore using the provided userID
        db.collection("users").document(userID)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get the user's email from the Firestore document
                    val email = document.getString("Email")

                    if (email != null) {
                        val user = FirebaseAuth.getInstance().currentUser

                        // Re-authenticate the user using the email and old password
                        val credential = EmailAuthProvider.getCredential(email, oldPassword)
                        user?.reauthenticate(credential)
                            ?.addOnSuccessListener {
                                // Check if the new password matches the confirm password
                                if (newPassword == confirmPassword) {

                                    // Update the password in Firebase Authentication
                                    user.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            showToast( "Password updated successfully")


                                            db.collection("users").document(userID)
                                                .update("Password", newPassword)
                                                .addOnSuccessListener {
                                                }
                                                .addOnFailureListener {
                                                }
                                            overlayView.visibility = View.GONE
                                        }
                                        .addOnFailureListener {
                                            showToast("Error occured Please Try Again Later")
                                            overlayView.visibility = View.GONE

                                        }

                                } else {
                                    showToast("New password and confirm password do not match")
                                    overlayView.visibility = View.GONE

                                }
                            }
                            ?.addOnFailureListener {
                                showToast("Error occured Please Try Again Later")
                                overlayView.visibility = View.GONE

                            }
                    } else {
                        showToast("Error occured Please Try Again Later")
                        overlayView.visibility = View.GONE

                    }
                } else {
                    showToast("Error occured Please Try Again Later")
                    overlayView.visibility = View.GONE

                }
            }
            .addOnFailureListener { 
                showToast("Error occured Please Try Again Later")
                overlayView.visibility = View.GONE

            }
    }


    private fun enableEdgeToEdge() {
        // Code to enable edge-to-edge UI
    }
}
