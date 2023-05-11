package com.example.videocallingapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService


class DisplayActivity : AppCompatActivity() {

    lateinit var userName: String
    var userId: Int = 11

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.ACCESS_WIFI_STATE,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.INTERNET,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == Activity.RESULT_OK){
            Toast.makeText(this, "permission granted!", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)
        userName = intent.getStringExtra("user_name").toString()
        addCallFragment()
    }

    private fun getPermissions() {
        if (Build.VERSION.SDK_INT <= 29) {
            if (ContextCompat.checkSelfPermission(this@DisplayActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this@DisplayActivity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1025)
            }
        } else {
            // for Android Versions >= 30
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(
                        String.format(
                            "package:%s", arrayOf(applicationContext.packageName)
                        )
                    )
                    activityResultLauncher.launch(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    activityResultLauncher.launch(intent)
                }
            }
        }
    }

    private fun addCallFragment() {
        val application = application
        val appID: Long = 1918767577
        val appSign: String = "e9146d1df0407b2115aca330153c0567bcaeb504914729f5ac8c45b882268f07"
        val callID: String = "1024"
        val userID: String = userId.toString()
        val userName: String = userName

//         You can also use GroupVideo/GroupVoice/OneOnOneVoice to make more types of calls.
        val config: ZegoUIKitPrebuiltCallConfig = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
        val fragment: ZegoUIKitPrebuiltCallFragment = ZegoUIKitPrebuiltCallFragment.newInstance(
            appID, appSign, callID, userID, userName, config
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitNow()
    }

    override fun onResume() {
        super.onResume()
        getPermissions()
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
//        val permissionsToRequest = mutableListOf<String>()
//        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO),1100)
            }
//        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1100) {
            var allPermissionsGranted = true
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (!allPermissionsGranted) {
                // Handle permission denied
            }
        }
    }
}