package com.example.solnex.operator.data

object ApiConfig {

    /*
     * Physical Android phone testing through:
     * adb reverse tcp:5097 tcp:5097
     */
    val BASE_URL =
        if (android.os.Build.FINGERPRINT.contains("generic")) "http://10.0.2.2:5097/api" else "http://192.168.1.52:8080/api"
}
