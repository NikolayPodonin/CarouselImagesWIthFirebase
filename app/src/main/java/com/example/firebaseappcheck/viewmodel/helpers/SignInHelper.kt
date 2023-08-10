package com.example.firebaseappcheck.viewmodel.helpers

import android.app.Activity
import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SignInHelper {

    val isSignInFlow: StateFlow<Boolean>

    val intentSenderRequestFlow: SharedFlow<IntentSenderRequest?>

    val onErrorFlow: SharedFlow<Throwable?>

    val currentUser: FirebaseUser?

    fun checkIfSignedIn()

    fun startSignIn(activity: Activity)

    fun tryAuthWithSignInResults(intent: Intent?)
}