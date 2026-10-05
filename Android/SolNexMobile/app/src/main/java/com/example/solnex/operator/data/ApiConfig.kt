package com.example.solnex.operator.data

object ApiConfig {

    /*
     * Physical Android phone testing through:
     * adb reverse tcp:5097 tcp:5097
     */
    // Comprehensive check to ensure it detects ANY emulator.
    private val isEmulator = android.os.Build.FINGERPRINT.contains("generic") ||
            android.os.Build.FINGERPRINT.startsWith("unknown") ||
            android.os.Build.MODEL.contains("google_sdk") ||
            android.os.Build.MODEL.contains("Emulator") ||
            android.os.Build.MODEL.contains("Android SDK built for x86") ||
            android.os.Build.MANUFACTURER.contains("Genymotion") ||
            (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic")) ||
            "google_sdk" == android.os.Build.PRODUCT ||
            android.os.Build.HARDWARE.contains("ranchu") ||
            android.os.Build.HARDWARE.contains("goldfish")

    // If running in an emulator, it automatically hits localhost (10.0.2.2).
    // If testing on a physical phone, change the IP below.
    val BASE_URL = if (isEmulator) "http://10.0.2.2:5097" else "http://192.168.1.52:5097"
}
