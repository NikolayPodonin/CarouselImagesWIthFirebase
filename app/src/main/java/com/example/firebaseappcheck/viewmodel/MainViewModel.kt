package com.example.firebaseappcheck.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseappcheck.viewmodel.helpers.SignInHelper
import com.example.firebaseappcheck.viewmodel.helpers.SignInHelperImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    signInHelper: SignInHelper = SignInHelperImpl()
): ViewModel(), SignInHelper by signInHelper {

    private lateinit var db: FirebaseFirestore

    private val _imagesFlow = MutableStateFlow<List<String>>(emptyList())
    val imagesFlow: StateFlow<List<String>> = _imagesFlow

    private val _makeToastFlow = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val makeToastFlow: Flow<String> = _makeToastFlow

    private val _uploadProgressFlow = MutableSharedFlow<Int>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uploadProgressFlow = _uploadProgressFlow

    init {
        viewModelScope.launch {
            signInHelper.onErrorFlow.collectLatest {
                onError(it)
            }
        }
    }

    fun initFirestore() {
        db = FirebaseFirestore.getInstance()
        takeImagesFromFirestore()
    }

    private fun takeImagesFromFirestore() {
        db.collection("images")
            .get()
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                val imageUrls: MutableList<String> = ArrayList()
                for (documentSnapshot in queryDocumentSnapshots) {
                    documentSnapshot.getString("imageUrl")?.let {
                        imageUrls.add(it)
                    }
                }
                _imagesFlow.value = imageUrls
            }
            .addOnFailureListener { e -> onError(e) }
    }

    // Upload image to Firestore
    fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
        val imageRef: StorageReference = storageRef.child("images/${currentUser?.uid}")
        imageRef.putFile(imageUri)
            .addOnProgressListener {
                _uploadProgressFlow.tryEmit(
                    (it.bytesTransferred * 100 / it.totalByteCount).toInt()
                )
            }
            .addOnSuccessListener { _ ->
                // Image uploaded successfully, now store its URL in Firestore
                imageRef.downloadUrl.addOnSuccessListener { uri -> saveImageUriToFirestore(uri.toString()) }
            }
            .addOnFailureListener { e -> onError(e) }
    }

    // Save image URL to Firestore
    private fun saveImageUriToFirestore(imageUrl: String) {
        val imageMap: MutableMap<String, Any> = mutableMapOf()
        imageMap["imageUrl"] = imageUrl
        db.collection("images").add(imageMap)
            .addOnSuccessListener { _ ->
                _makeToastFlow.tryEmit("Image saved")
                takeImagesFromFirestore()
            }
            .addOnFailureListener { e -> onError(e) }
    }

    private fun onError(e: Throwable? = null) {
        _makeToastFlow.tryEmit("Something went wrong")
    }
}