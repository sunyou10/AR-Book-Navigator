package com.example.librarytest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.librarytest.arcore.ARCoreSessionLifecycleHelper
import com.example.librarytest.arcore.ArCoreUtils
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.sceneform.ux.ArFragment

class MainActivity : AppCompatActivity() {

    private val arCoreSessionHelper = ARCoreSessionLifecycleHelper(this)
    private lateinit var arFragment: ArFragment
    private val book: String = "스타벅스 웨이"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupARCoreSession()

        ArCoreUtils.checkAndInstallArCore(this)

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment
        arFragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                val arSceneView = arFragment.arSceneView
                if (arSceneView != null) {
                    setupArFragment()
                } else {
                    Log.e("MainActivity", "ArSceneView is null in onResume")
                }
            }
        })

        findViewById<ExtendedFloatingActionButton>(R.id.startButton).setOnClickListener {
            val intent = Intent(this, NavigationActivity::class.java).apply {
                putExtra("bookNumber", book)
            }
            startActivity(intent)
        }
    }

    private fun setupARCoreSession() {
        arCoreSessionHelper.beforeSessionResume = { session ->
            session.configure(
                session.config.apply {
                    focusMode = Config.FocusMode.AUTO
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL // 수평 및 수직 평면 감지
                    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                        depthMode = Config.DepthMode.AUTOMATIC
                    }
                }
            )

            val filter = CameraConfigFilter(session)
                .setFacingDirection(CameraConfig.FacingDirection.BACK)

            val configs = session.getSupportedCameraConfigs(filter)
            val sort = compareByDescending<CameraConfig> { it.imageSize.width }
                .thenByDescending { it.imageSize.height }
            session.cameraConfig = configs.sortedWith(sort)[0]
        }
    }

    private fun setupArFragment() {
        arFragment.arSceneView?.let { arSceneView ->
            arSceneView.scene.addOnUpdateListener { frameTime ->
                arFragment.onUpdate(frameTime)
            }
        } ?: run {
            Toast.makeText(this, "ARSceneView 초기화 실패.", Toast.LENGTH_SHORT).show()
        }
    }
}