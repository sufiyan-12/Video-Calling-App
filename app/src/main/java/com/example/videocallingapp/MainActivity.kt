package com.example.videocallingapp

import android.R.attr.phoneNumber
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), java.io.Serializable{
    lateinit var layout1: LinearLayout
    lateinit var layout2: LinearLayout
    lateinit var pbar: ProgressBar
    lateinit var mAuth: FirebaseAuth
    lateinit var signInButton: SignInButton
    lateinit var otpBtn: Button
    lateinit var phoneEditText: EditText
    lateinit var otpEditText: EditText
    lateinit var contryCodeTV: TextView
    private val REQUEST_CODE = 101
    private val TAG = "myActivity"
    private var codeSent = false
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var profileBtn: Button
    lateinit var videoCallBtn: Button
    lateinit var myTask: Task<AuthResult>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initVariables()

        if (isUserLoggedIn()) {
            pbar.visibility = View.GONE
            layout1.visibility = View.GONE
            layout2.visibility = View.VISIBLE
        }

        useSignIn()
        verifyPhone()

        profileBtn.setOnClickListener {
            goToPofile()
        }

        videoCallBtn.setOnClickListener {
        if(myTask != null) initiateVideoCall(myTask)
        }
    }

    private fun goToPofile() {
        val intent = Intent(this@MainActivity, UserProfile::class.java)
        startActivity(intent)
    }

    private fun useSignIn() {
        // Initialize Google Sign In client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(resources.getString(R.string.web_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set a click listener on the sign-in button
        signInButton.setOnClickListener {
            layout1.visibility = View.GONE
            pbar.visibility = View.VISIBLE
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, REQUEST_CODE)
        }
    }

    private fun initVariables() {

        layout1 = findViewById(R.id.layout1)
        layout2 = findViewById(R.id.layout2)

        pbar = findViewById(R.id.pbar)
        mAuth = FirebaseAuth.getInstance()

        otpBtn = findViewById(R.id.otpBtn)
        phoneEditText = findViewById(R.id.phoneEditText)

        signInButton = findViewById(R.id.sign_in_button)
        signInButton.setSize(SignInButton.SIZE_STANDARD)
        contryCodeTV = findViewById(R.id.contryCodeTV)
        otpEditText = findViewById(R.id.otpEditText)

        profileBtn = findViewById(R.id.profileBtn)
        videoCallBtn = findViewById(R.id.videoCallBtn)
    }

    private fun verifyPhone() {
        otpBtn.setOnClickListener {
            val phoneNo = phoneEditText.text
            if (!phoneNo.isNullOrEmpty()) {
                if (phoneNo.length < 10) {
                    showToast()
                } else {
                    otpVerification(phoneNo.toString())
                }
            } else if (codeSent) {
                val otpcode: String = otpEditText.text.toString().trim()
                val credential = PhoneAuthProvider.getCredential(storedVerificationId, otpcode)
                signInWithPhoneAuthCredential(credential)
            } else {
                showToast()
            }
            phoneEditText.text.clear()
        }
    }

    private fun otpVerification(phoneNo: String) {
        mAuth.setLanguageCode("en-us")

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+91$phoneNo") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.
//                                Log.d(TAG, "onVerificationCompleted:$credential")
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.
//                                Log.w(TAG, "onVerificationFailed", e)
                    Toast.makeText(
                        this@MainActivity,
                        "VerificationFailed, Please login again!",
                        Toast.LENGTH_SHORT
                    ).show()
                    codeSent = false
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                    } else if (e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                    } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                        // reCAPTCHA verification attempted with null Activity
                    }

                    // Show a message and update the UI
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
//                                Log.d(TAG, "onCodeSent:$verificationId")
                    Toast.makeText(this@MainActivity, "Code Sent!", Toast.LENGTH_SHORT).show()
                    // Save verification ID and resending token so we can use them later

                    contryCodeTV.visibility = View.GONE
                    phoneEditText.visibility = View.GONE
                    otpEditText.visibility = View.VISIBLE
                    codeSent = true
                    otpBtn.text = "Verify"
                    storedVerificationId = verificationId
                    resendToken = token
                }
            }) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(this@MainActivity, "Verification Success!", Toast.LENGTH_SHORT)
                        .show()
                    otpBtn.visibility = View.GONE
                    otpEditText.visibility = View.GONE
                    videoCallBtn.visibility = View.VISIBLE
                    profileBtn.visibility = View.VISIBLE
                    myTask = task
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private fun initiateVideoCall(task: Task<AuthResult>) {
            val user = task.result?.user
            val intent = Intent(this@MainActivity, DisplayActivity::class.java)
            intent.putExtra("user_name", user?.displayName)
            startActivity(intent)
    }


    private fun showToast() {
        Toast.makeText(this, "please enter correct phone number!", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == REQUEST_CODE) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                layout1.visibility = View.VISIBLE
                pbar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Authentication failed.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = mAuth.currentUser
                updateUI(user)
            } else {
                Toast.makeText(this@MainActivity, "Authentication failed.", Toast.LENGTH_SHORT)
                    .show()
                pbar.visibility = View.GONE
                layout1.visibility = View.VISIBLE
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            pbar.visibility = View.GONE
            layout2.visibility = View.VISIBLE
        } else {
            pbar.visibility = View.GONE
            layout1.visibility = View.VISIBLE
            Toast.makeText(this@MainActivity, "Please login again!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        return user != null
    }

    override fun onResume() {
        super.onResume()
        if (isUserLoggedIn()) {
            updateUI(FirebaseAuth.getInstance().currentUser)
        }
    }
}