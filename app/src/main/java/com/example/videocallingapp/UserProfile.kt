package com.example.videocallingapp

import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.videocallingapp.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserProfile : AppCompatActivity() {
    lateinit var binding : ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile)

        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        binding.userName.text = firebaseAuth.currentUser?.displayName.toString()
        Glide.with(this).load(firebaseAuth.currentUser?.photoUrl).into(binding.imageView)
        binding.signOut.setOnClickListener {
            firebaseAuth.signOut()
            finish()
        }
    }
}