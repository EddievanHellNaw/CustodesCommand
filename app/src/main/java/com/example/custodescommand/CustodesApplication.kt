
package com.example.custodescommand

import android.app.Application
import com.example.custodescommand.core.AppContainer

class CustodesApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
