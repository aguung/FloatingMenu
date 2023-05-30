package com.devfutech.floatingmenu

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.devfutech.floatingmenu.databinding.ActivityMainBinding
import org.greenrobot.eventbus.EventBus
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.apply {
            btnServiceFloatOverlay.setOnClickListener {
                if (requestPermission(SERVICE_FLOAT_REQUEST_CODE)) {
                    return@setOnClickListener
                }
                showServiceFloat()
            }
            btnActivityFloatPanel.setOnClickListener {
                showActivityFloat()
            }
            btnActivityFloatOverlay.setOnClickListener {
                if (requestPermission(ACTIVITY_SERVICE_FLOAT_REQUEST_CODE)) {
                    return@setOnClickListener
                }
                showActivityServiceFloat()
            }
            btnStopServiceFloatOverlay.setOnClickListener {
                stopService(Intent(this@MainActivity, ServiceFloating::class.java))
            }
            btnSubmit.setOnClickListener {
                if (etInput.text.toString().trim().isNotEmpty()) {
                    EventBus.getDefault()
                        .post(MessageEvent(message = etInput.text.toString().trim()))
                    etInput.text?.clear()
                }
            }
        }
    }

    private fun showServiceFloat() {
        startService(Intent(this, ServiceFloating::class.java))
    }

    private fun showActivityFloat() {
        (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.view_activity_float, null)
    }

    private fun showActivityServiceFloat() {
        (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.view_service_float, null)
    }

    private fun requestPermission(requestCode: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${packageName}")
                )
                CURRENT_REQUEST_CODE = requestCode
                resultLauncher.launch(intent)
                return true
            }
        }
        return false
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    when (CURRENT_REQUEST_CODE) {
                        SERVICE_FLOAT_REQUEST_CODE -> showServiceFloat()
                        ACTIVITY_SERVICE_FLOAT_REQUEST_CODE -> showActivityServiceFloat()
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }else{
                when (CURRENT_REQUEST_CODE) {
                    SERVICE_FLOAT_REQUEST_CODE -> showServiceFloat()
                    ACTIVITY_SERVICE_FLOAT_REQUEST_CODE -> showActivityServiceFloat()
                }
            }
        }


    companion object {
        const val SERVICE_FLOAT_REQUEST_CODE = 1000
        const val ACTIVITY_SERVICE_FLOAT_REQUEST_CODE = 2000
        var CURRENT_REQUEST_CODE = 0
    }
}