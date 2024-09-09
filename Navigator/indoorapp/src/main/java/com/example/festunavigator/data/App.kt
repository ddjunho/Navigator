package com.example.festunavigator.data

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    companion object {
        const val ADMIN_MODE = "admin"
        const val USER_MODE = "user"

        // 항상 admin 모드로 설정
        const val mode = ADMIN_MODE
        const val isAdmin = mode == ADMIN_MODE
        const val isUser = mode == USER_MODE
    }
}
