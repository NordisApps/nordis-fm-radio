package com.nordisapps.fmradio.system

import android.annotation.SuppressLint
import android.os.IBinder

const val FM_SERVICE_NAME = "FMPlayer"

object SystemServiceLocator {
    @SuppressLint("PrivateApi")
    fun getServiceOrNull(name: String): IBinder? = runCatching {
        val cls = Class.forName("android.os.ServiceManager")
        cls.getMethod("getService", String::class.java).invoke(null, name) as? IBinder
    }.getOrNull()

    fun getService(name: String): IBinder =
        getServiceOrNull(name) ?: throw IllegalStateException("Service '$name' not found")
}