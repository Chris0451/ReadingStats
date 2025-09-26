package com.project.readingstats

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application(){
    override fun onCreate(){
        super.onCreate()
        com.google.firebase.FirebaseApp.initializeApp(this)
        com.google.firebase.FirebaseApp.initializeApp(this)
        com.google.firebase.auth.FirebaseAuth.getInstance().setLanguageCode("it") // facoltativo
        com.google.firebase.FirebaseApp.getInstance().setAutomaticResourceManagementEnabled(true)
        com.google.firebase.platforminfo.GlobalLibraryVersionRegistrar.getInstance() // forza init
        com.google.firebase.firestore.FirebaseFirestore.setLoggingEnabled(true)
    }
}