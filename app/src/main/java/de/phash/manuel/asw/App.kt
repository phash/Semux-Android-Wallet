package de.phash.manuel.asw

import android.app.Application
import com.parse.Parse
import com.parse.ParseInstallation

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Parse.initialize(Parse.Configuration.Builder(this)
                .applicationId("")
                .clientKey("")
                .server("https://parseapi.back4app.com/")
                .build())
        ParseInstallation.getCurrentInstallation().saveInBackground()

    }
}