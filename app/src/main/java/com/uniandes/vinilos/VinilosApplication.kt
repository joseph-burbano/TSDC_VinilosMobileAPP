package com.uniandes.vinilos

import android.app.Application
import com.uniandes.vinilos.network.NetworkServiceAdapter
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp 
class VinilosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkServiceAdapter.init(this)
    }
}
