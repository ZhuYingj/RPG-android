package com.mobile_client.utils

import android.content.Context
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

fun launchQrScanner(context: Context, onResult: (String) -> Unit, onError: (String) -> Unit) {
    val scanner = GmsBarcodeScanning.getClient(context)
    scanner.startScan()
        .addOnSuccessListener { barcode ->
            barcode.rawValue?.let { onResult(it) }
        }
        .addOnFailureListener { e ->
            onError(e.message ?: "Erreur de scan")
        }
}
