package com.example.google_integ

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var imageView: ShapeableImageView
    private lateinit var name: TextView
    private lateinit var mail: TextView

    // Launch Google Sign-In
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val signInAccount = accountTask.getResult(ApiException::class.java)
                    val authCredential = GoogleAuthProvider.getCredential(signInAccount.idToken, null)
                    auth.signInWithCredential(authCredential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            auth = FirebaseAuth.getInstance()
                            Glide.with(this@MainActivity).load(auth.currentUser?.photoUrl).into(imageView)
                            name.text = auth.currentUser?.displayName
                            mail.text = auth.currentUser?.email
                            Toast.makeText(this@MainActivity, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Failed to sign in: ${task.exception}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Initialize views
        imageView = findViewById(R.id.profileImage)
        name = findViewById(R.id.nameTV)
        mail = findViewById(R.id.mailTV)

        // Configure Google Sign-In
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, options)

        // Sign-In button logic
        val signInButton: com.google.android.gms.common.SignInButton = findViewById(R.id.signIn)
        signInButton.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            activityResultLauncher.launch(intent)
        }

        // Sign-Out button logic
        val signOutButton: MaterialButton = findViewById(R.id.signout)
        signOutButton.setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                Toast.makeText(this@MainActivity, "Signed out successfully!", Toast.LENGTH_SHORT).show()
                name.text = ""
                mail.text = ""
                imageView.setImageResource(R.drawable.ic_launcher_background)
            }
        }

        // Edge-to-edge and window insets handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // If already signed in
        if (auth.currentUser != null) {
            Glide.with(this).load(auth.currentUser?.photoUrl).into(imageView)
            name.text = auth.currentUser?.displayName
            mail.text = auth.currentUser?.email
        }
    }
}
