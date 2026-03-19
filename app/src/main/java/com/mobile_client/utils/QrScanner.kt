package com.mobile_client.utils

import android.content.Context
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

fun launchQrScanner(context: Context, onResult: (String) -> Unit, onError: (String) -> Unit) {
    val scanner = GmsBarcodeScanning.getClient(context)
    scanner.startScan()
        .addOnSuccessListener { barcode ->
            barcode.rawValue?.let { onResult(it) }
        }
        .addOnFailureListener {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        barcode.rawValue?.let { onResult(it) }
                    }
                    .addOnFailureListener { retryError ->
                        onError(retryError.message ?: "Erreur de scan")
                    }
            }, 1000)
        }
}
