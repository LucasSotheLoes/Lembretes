package br.com.lucas.lembretes

import android.app.Application

class LembretesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.createChannel(this)
    }
}
