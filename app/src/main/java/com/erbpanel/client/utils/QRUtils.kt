package com.erbpanel.client.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import com.budiyev.android.codescanner.CodeScanner
import com.erbpanel.client.R
import com.erbpanel.client.adapters.VehicleInfo
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun generateQRCode(vehicleInfo: VehicleInfo): Bitmap? {
    val qrCodeWriter = QRCodeWriter()

    return try {
        val jsonData = Gson().toJson(vehicleInfo)
        val bitMatrix = qrCodeWriter.encode(jsonData, BarcodeFormat.QR_CODE, 500, 500)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: WriterException) {
        e.printStackTrace()
        null
    } catch (e: Exception){
        e.printStackTrace()
        null
    }

}

fun saveQRCodeToFile(bitmap: Bitmap): File? {
    val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val qrFile = File(storageDir, "vehicle_info_qr.png")

    return try {
        val outputStream = FileOutputStream(qrFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        qrFile
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun showSuccessDialog(context: Context, message: String, codeScanner: CodeScanner, controller: NavController, cancelable:Boolean = false) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Success")
    builder.setMessage(message)
    builder.setPositiveButton("OK") { dialog, _ ->
        controller.navigate(R.id.action_FirstFragment_to_SecondFragment)
        dialog.dismiss()
    }
    builder.setNegativeButton("Cancel") { dialog, _ ->
        dialog.dismiss()
        codeScanner.startPreview()
    }
    builder.setCancelable(cancelable)
    val dialog = builder.create()
    dialog.show()
}

fun showErrorDialog(context: Context, message: String, retry: Int = 0, cancelable:Boolean = false){
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Success")
    builder.setMessage(message)
    builder.setPositiveButton("OK") { dialog, _ ->
        dialog.dismiss()
    }
    if (retry> 0) {
        builder.setNegativeButton("Retry") { dialog, _ ->
            dialog.dismiss()
        }
    }
    builder.setCancelable(cancelable)
    val dialog = builder.create()
    dialog.show()
}