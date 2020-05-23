package ch.pete.appconfigapp.app

import android.app.Application
import ch.pete.appconfigapp.BuildConfig
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(
            if (BuildConfig.DEBUG) {
                Timber.DebugTree()
            } else {
                CrashReportingTree()
            }
        )
    }
}
