package com.twoid.proserv_mobile

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.lang.Exception


object ImageHelper {

    fun convertByteArrayToBase64String(bitmap: Bitmap) :String {

        try{
            val byteoutputdata = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteoutputdata)
            val bytedata = byteoutputdata.toByteArray()
            return Base64.encodeToString(bytedata, Base64.DEFAULT)
        }
        catch (e: Exception)
        {
            return ""
        }


    }

    fun convertByteArrayToBase64String(array: ByteArray) :String
    {
        try
        {

            return Base64.encodeToString(array, Base64.DEFAULT)
        }
        catch (e: Exception)
        {
            return ""
        }

    }

}