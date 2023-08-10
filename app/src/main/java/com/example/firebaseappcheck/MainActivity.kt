package com.example.firebaseappcheck

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.firebaseappcheck.ui.theme.FirebaseAppCheckTheme
import com.example.firebaseappcheck.viewmodel.MainViewModel


class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FirebaseAppCheckTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    viewModel = viewModel()
                    viewModel.checkIfSignedIn()

                    val isSignIn by viewModel.isSignInFlow.collectAsState()
                    SignInOrGetImages(isSignIn = isSignIn)
                }
            }
        }
    }

    @Composable
    fun SignInOrGetImages(isSignIn: Boolean) {
        Box {
            if (isSignIn) {
                viewModel.initFirestore()

                val makeText by viewModel.makeToastFlow.collectAsState(initial = null)
                val imagesList by viewModel.imagesFlow.collectAsState()

                MakeToast(message = makeText)

                ImageCarousel(imagesList)
                ImagePicker { selectedImageUri ->
                    viewModel.uploadImageToFirebaseStorage(selectedImageUri)
                }
                val uploadProgress by viewModel.uploadProgressFlow.collectAsState(initial = 0)
                ProgressText(uploadProgress)
            } else {
                val intentSenderRequest by viewModel.intentSenderRequestFlow.collectAsState(initial = null)
                GoogleSignInButton(intentSenderRequest) {
                    it?.let {
                        viewModel.tryAuthWithSignInResults(it)
                    }
                }
            }
        }
    }

    @Composable
    private fun BoxScope.ProgressText(uploadProgress: Int) {
        if (uploadProgress in 1..99) {
            Text(
                text = "$uploadProgress% uploaded",
                color = Color.Black,
                modifier = Modifier.Companion.align(Alignment.Center)
            )
        }
    }

    @Composable
    fun BoxScope.GoogleSignInButton(
        intentSenderRequest: IntentSenderRequest?,
        onSignInComplete: (Intent?) -> Unit
    ) {
        intentSenderRequest?.let {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = {
                    onSignInComplete(it.data)
                }
            )
            SideEffect {
                launcher.launch(intentSenderRequest)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        ) {
            Button(
                onClick = { viewModel.startSignIn(this@MainActivity) }
            ) {
                Text("Sign In with Google")
            }
        }
    }

    @Composable
    private fun MakeToast(message: String?) {
        message?.let {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun ImagePicker(
    onImageSelected: (Uri) -> Unit
) {
    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { onImageSelected(it) }
        }
    )

    Column(
        modifier = Modifier.fillMaxHeight()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        activityResultLauncher.launch("image/*")
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Pick Image")
                }
            }
        }
    }
}

@Composable
fun ImageCarousel(imagesList: List<String>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(imagesList) { imageUrl ->
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .size(coil.size.Size.ORIGINAL) // Set the target size to load the image at.
                        .build()
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        }
    }
}