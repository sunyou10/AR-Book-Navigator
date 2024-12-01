package com.example.librarytest.arcore

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.ar.core.ArCoreApk

object ArCoreUtils {
    fun checkAndInstallArCore(activity: Activity) {
        val availability = ArCoreApk.getInstance().checkAvailability(activity)
        if (availability.isTransient) {
            Handler(Looper.getMainLooper()).postDelayed({
                checkAndInstallArCore(activity)
            }, 200)
        } else if (availability.isSupported) {
            try {
                if (ArCoreApk.getInstance().requestInstall(activity, true) == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                    // 설치 요청됨, 시스템에서 처리
                }
            } catch (e: Exception) {
                Toast.makeText(activity, "ARCore 설치 중 문제가 발생했습니다.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(activity, "이 기기는 ARCore를 지원하지 않습니다.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Google Play 스토어로 이동하여 ARCore 설치 요청
     */
    fun openPlayStoreForArCore(activity: Activity) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.ar.core"))
            activity.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(activity, "Play 스토어를 열 수 없습니다. ARCore APK를 수동으로 설치하세요.", Toast.LENGTH_LONG).show()
        }
    }
}