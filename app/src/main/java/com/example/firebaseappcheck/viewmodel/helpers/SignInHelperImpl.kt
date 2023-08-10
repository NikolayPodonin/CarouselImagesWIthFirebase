package com.example.firebaseappcheck.viewmodel.helpers

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SignInHelperImpl: SignInHelper {
    private var oneTapClient: SignInClient? = null
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _isSignInFlow = MutableStateFlow(false)
    override val isSignInFlow: StateFlow<Boolean> = _isSignInFlow

    private val _intentSenderRequestFlow = MutableSharedFlow<IntentSenderRequest?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val intentSenderRequestFlow = _intentSenderRequestFlow

    private val _onErrorFlow = MutableSharedFlow<Throwable?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val onErrorFlow = _onErrorFlow

    override val currentUser: FirebaseUser? = auth.currentUser

    override fun checkIfSignedIn() {
        val currentUser = auth.currentUser
        _isSignInFlow.value = currentUser != null

        // Look for a pending auth result
        val pending = auth.pendingAuthResult
        pending?.addOnSuccessListener { authResult ->
            _isSignInFlow.value = authResult.user != null
        }?.addOnFailureListener { e ->
            _isSignInFlow.value = false
            _onErrorFlow.tryEmit(e)
        }
    }

    override fun startSignIn(activity: Activity) {
        val signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId("502575844478-47ahta39b7ldjl4ra6s3r70t7d3tp5s6.apps.googleusercontent.com")
                    // Show all accounts on the device.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .setAutoSelectEnabled(true)
            .build()
        oneTapClient = Identity.getSignInClient(activity)
        oneTapClient?.beginSignIn(signUpRequest)
            ?.addOnSuccessListener(activity) { result ->
                try {
                    _intentSenderRequestFlow.tryEmit(
                        IntentSenderRequest.Builder(result.pendingIntent).build()
                    )
                } catch (e: IntentSender.SendIntentException) {
                    _onErrorFlow.tryEmit(e)
                }
            }
            ?.addOnFailureListener(activity) { e ->
                // No Google Accounts found. Just continue presenting the signed-out UI.
                _onErrorFlow.tryEmit(e)
            }
    }

    override fun tryAuthWithSignInResults(intent: Intent?) {
        try {
            val credential = oneTapClient?.getSignInCredentialFromIntent(intent)
            val idToken = credential?.googleIdToken
            when {
                idToken != null -> {
                    // Got an ID token from Google. Use it to authenticate
                    // with your backend.
                    val authCred = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(authCred)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Sign-in with Google credentials succeeded
                                _isSignInFlow.value = true
                                // Now you have a signed-in Firebase user
                            } else {
                                // Sign-in with Google credentials failed
                                _onErrorFlow.tryEmit(null)
                            }
                        }
                }
                else -> {
                    // Shouldn't happen.
                    _onErrorFlow.tryEmit(null)
                }
            }
        } catch (e: ApiException) {
            _onErrorFlow.tryEmit(e)
        }
    }
}