package com.example.healthhubguc.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthhubguc.Medication
import com.example.healthhubguc.MedicationsAdapterD
import com.example.healthhubguc.R
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MedicationsFragmentD : Fragment(R.layout.fragment_medications_d) {

    private lateinit var addMedicationButton: Button
    private lateinit var medicationNameEditText: EditText
    private lateinit var medicationQuantityEditText: EditText
    private lateinit var medicationPriceEditText: EditText
    private lateinit var recyclerView: RecyclerView

    private lateinit var database: FirebaseDatabase
    private lateinit var medicationsReference: DatabaseReference
    private val medications = mutableListOf<Medication>()
    private lateinit var adapter: MedicationsAdapterD

    private var selectedMedicationId: String? = null

    private lateinit var selectImageButton: Button
    private lateinit var captureImageButton: Button
    private lateinit var imageView: ImageView
    private var selectedImageUri: Uri? = null
    private var currentPhotoPath: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_medications_d, container, false)

        // Initialize medication management UI elements
        addMedicationButton = rootView.findViewById(R.id.add_medication_button)
        medicationNameEditText = rootView.findViewById(R.id.medication_name_edit_text)
        medicationQuantityEditText = rootView.findViewById(R.id.medication_quantity_edit_text)
        medicationPriceEditText = rootView.findViewById(R.id.medication_price_edit_text)
        recyclerView = rootView.findViewById(R.id.recyclerView)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MedicationsAdapterD(medications) { medication ->
            populateFieldsForEditing(medication)
        }
        recyclerView.adapter = adapter

        // Firebase Database reference for medications
        database = FirebaseDatabase.getInstance()
        medicationsReference = database.reference.child("medications")

        // Load medications from Firebase
        loadMedicationsFromDatabase()

        // Set up add/update medication button
        addMedicationButton.setOnClickListener {
            if (selectedMedicationId == null) {
                addMedicationToDatabase()
            } else {
                updateMedicationInDatabase()
            }
        }

        // Initialize image selection and capture UI elements
        selectImageButton = rootView.findViewById(R.id.select_image_button)
        captureImageButton = rootView.findViewById(R.id.capture_image_button)
        imageView = rootView.findViewById(R.id.medication_image)

        // Button to select an image from the gallery
        selectImageButton.setOnClickListener { openGallery() }

        // Button to capture an image using the camera
        captureImageButton.setOnClickListener { openCamera() }

        return rootView
    }

    private fun loadMedicationsFromDatabase() {
        medicationsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                medications.clear()
                for (medicationSnapshot in snapshot.children) {
                    val medication = medicationSnapshot.getValue(Medication::class.java)
                    if (medication != null) {
                        medication.id = medicationSnapshot.key // store Firebase key
                        medications.add(medication)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load medications", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Convert image to Base64 String
    private fun encodeImageToBase64(uri: Uri): String? {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun addMedicationToDatabase() {
        val name = medicationNameEditText.text.toString()
        val quantity = medicationQuantityEditText.text.toString().toIntOrNull() ?: 0
        val price = medicationPriceEditText.text.toString().toDoubleOrNull() ?: 0.0

        if (name.isNotEmpty() && quantity > 0 && price > 0.0) {
            val imageBase64 = selectedImageUri?.let { encodeImageToBase64(it) }

            val medication = Medication(name, quantity, price, imagePath = imageBase64)
            medicationsReference.push().setValue(medication).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Medication added successfully", Toast.LENGTH_SHORT).show()
                    clearInputFields()
                } else {
                    Toast.makeText(context, "Failed to add medication", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMedicationInDatabase() {
        val name = medicationNameEditText.text.toString()
        val quantity = medicationQuantityEditText.text.toString().toIntOrNull() ?: 0
        val price = medicationPriceEditText.text.toString().toDoubleOrNull() ?: 0.0

        if (name.isNotEmpty() && quantity > 0 && price > 0.0 && selectedMedicationId != null) {
            val imageBase64 = selectedImageUri?.let { encodeImageToBase64(it) }
            val updatedMedication = Medication(name, quantity, price, id = selectedMedicationId, imagePath = imageBase64)
            medicationsReference.child(selectedMedicationId!!).setValue(updatedMedication).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Medication updated successfully", Toast.LENGTH_SHORT).show()
                    clearInputFields()
                    selectedMedicationId = null
                    addMedicationButton.text = "Add Medication"
                } else {
                    Toast.makeText(context, "Failed to update medication", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearInputFields() {
        medicationNameEditText.text.clear()
        medicationQuantityEditText.text.clear()
        medicationPriceEditText.text.clear()
        imageView.setImageURI(null) // Clear the image
    }

    private fun populateFieldsForEditing(medication: Medication) {
        medicationNameEditText.setText(medication.name)
        medicationQuantityEditText.setText(medication.quantity.toString())
        medicationPriceEditText.setText(medication.price.toString())
        selectedMedicationId = medication.id
        addMedicationButton.text = "Update Medication"
    }

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data!!.data
                imageView.setImageURI(selectedImageUri)
                imageView.visibility = View.VISIBLE
            }
        }

    private val captureImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val file = File(currentPhotoPath)
                selectedImageUri = Uri.fromFile(file)
                imageView.setImageURI(selectedImageUri)
                imageView.visibility = View.VISIBLE
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.let {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.healthhubguc.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                captureImageLauncher.launch(takePictureIntent)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }
}
