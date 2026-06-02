package com.example

import android.app.Application
import com.example.data.SadaDatabase

class SadaApplication : Application() {
    val database: SadaDatabase by lazy { SadaDatabase.getDatabase(this) }
}
