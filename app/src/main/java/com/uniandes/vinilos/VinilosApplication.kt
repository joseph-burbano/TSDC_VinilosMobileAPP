package com.uniandes.vinilos

import android.app.Application
import com.uniandes.vinilos.network.NetworkServiceAdapter

class VinilosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkServiceAdapter.init(this)
    }
}
