package com.alpdroid.huGen10
import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy

enum class ServiceState {
    STARTED,
    STOPPED,
}

enum class Actions {
    START,
    STOP
}

private const val name = "SPYSERVICE_KEY"
private const val key = "SPYSERVICE_STATE"

fun setServiceState(context: Context, state: ServiceState) {
    val sharedPrefs = getPreferences(context)
    sharedPrefs.edit().let {
        it.putString(key, state.name)
        it.apply()
    }
}

fun getServiceState(context: Context): ServiceState {
    val sharedPrefs = getPreferences(context)
    var state:ServiceState

    val oldPolicy: ThreadPolicy

    oldPolicy = StrictMode.getThreadPolicy()
    StrictMode.allowThreadDiskReads()
    try {
        val value = sharedPrefs.getString(key, ServiceState.STOPPED.name)
        state = value?.let { ServiceState.valueOf(it) }!!
    }
    finally {
        StrictMode.setThreadPolicy(oldPolicy)
    }
    return state
}

private fun getPreferences(context: Context): SharedPreferences {
    var sharpreferences: SharedPreferences

    val oldPolicy: ThreadPolicy = StrictMode.getThreadPolicy()

    StrictMode.allowThreadDiskReads()
    try {
        // Do reads here
        context.getSharedPreferences(name, 0).also { sharpreferences = it }
    } finally {
        StrictMode.setThreadPolicy(oldPolicy)
    }
    return sharpreferences
}