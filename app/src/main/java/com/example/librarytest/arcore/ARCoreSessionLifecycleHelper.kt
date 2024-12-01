package com.example.librarytest.arcore

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException

class ARCoreSessionLifecycleHelper(
    private val activity: Activity,
    private val features: Set<Session.Feature> = setOf()
) : DefaultLifecycleObserver {

    private var installRequested = false
    private var sessionCache: Session? = null

    // 세션 생성 실패 시 예외 처리
    var exceptionCallback: ((Exception) -> Unit)? = null

    var beforeSessionResume: ((Session) -> Unit)? = null

    var trackingStateCallback: ((TrackingState) -> Unit)? = null

    fun tryCreateSession(): Session? {
        when (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
            ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                installRequested = true
                return null
            }

            ArCoreApk.InstallStatus.INSTALLED -> {
                // ARCore 설치 완료
            }
        }

        return try {
            Session(activity, features)
        } catch (e: Exception) {
            exceptionCallback?.invoke(e)
            null
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        val session = tryCreateSession() ?: return
        try {
            beforeSessionResume?.invoke(session)
            session.resume()
            sessionCache = session
        } catch (e: CameraNotAvailableException) {
            exceptionCallback?.invoke(e)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        sessionCache?.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        sessionCache?.close()
        sessionCache = null
    }

    fun getCurrentFrame(): Frame? {
        return sessionCache?.update()
    }
}