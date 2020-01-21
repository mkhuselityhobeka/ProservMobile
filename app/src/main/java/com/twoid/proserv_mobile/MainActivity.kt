package com.twoid.proserv_mobile

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.location.LocationManager
import android.media.Image
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager

import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.ftransisdk.FrigerprintControl

import com.suprema.BioMiniFactory
import com.suprema.CaptureResponder
import com.suprema.IBioMiniDevice
import com.suprema.IUsbEventHandler

import java.util.*


class MainActivity : AppCompatActivity() {



    private var bioMiniFactory: BioMiniFactory? = null
    private var ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private var usbManager: UsbManager? = null
    private var  mbExternalUsbManager:Boolean = false
    private var pendingIntentPession: PendingIntent? = null
    private var currentDevice:IBioMiniDevice? = null
    private  var mainContext:MainActivity = this
    private lateinit var loginFinger:String

    private val defaultCaptureOptions: IBioMiniDevice.CaptureOption = IBioMiniDevice.CaptureOption()

    private val mCaptureResponseDefault = object: CaptureResponder(){

        override fun onCaptureEx(context: Any?, capturedImage: Bitmap?, capturedTemplate: IBioMiniDevice.TemplateData?, iBioMiniDeviceState: IBioMiniDevice.FingerState?
        ): Boolean {
            runOnUiThread {

                if(capturedImage != null){
                    var imageCpatured = findViewById<ImageView>(R.id.fingerprint_icon)

                    if(imageCpatured != null){
                       imageCpatured.setImageBitmap(capturedImage)
                   }

                }

            }
          return true
        }


        override fun onCaptureError(context: Any?, errorCode: Int, errorDescription: String?) {

            Log.d("captureError", "error description is " + errorDescription)
            if (errorCode != IBioMiniDevice.ErrorCode.OK.value()){

            }

        }


    }


    private  var  broadcastReciever = object : BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?) {

            val action = intent!!.action
            if (ACTION_USB_PERMISSION.equals(action)){
                    synchronized(this){
                        val device = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)){

                            if(device != null){
                                if(bioMiniFactory == null){return}

                                  bioMiniFactory?.addDevice(device)
                                  println("===========================================================================================================")
                                  println(String.format(Locale.ENGLISH, "Initialized device count- BioMiniFactory (%d)", bioMiniFactory?.getDeviceCount()))

                            }
                        }else{
                            println("permission denied for device$device")
                        }

                    }
            }

        }


    }


       private fun checkDevice(){

            if (usbManager == null){ return }

                 val deviceList = usbManager!!.deviceList
                 val deviceIterator = deviceList!!.values.iterator()

                while (deviceIterator.hasNext()){
                    val devices = deviceIterator.next()
                    if (devices.vendorId == 0x1374){
                        usbManager!!.requestPermission(devices, pendingIntentPession)
                    }else{

                    }
                }
        }

    fun deviceChangeHandler(event : IUsbEventHandler.DeviceChangeEvent, dev:Any) {

        if (event == IUsbEventHandler.DeviceChangeEvent.DEVICE_ATTACHED && currentDevice == null) {

            Thread(Runnable {

                var cnt = 0
                while (bioMiniFactory == null && cnt < 20) {
                    Thread.sleep(1000)
                    cnt++
                }


                if (bioMiniFactory != null) {
                    currentDevice = bioMiniFactory!!.getDevice(0)
                    println("bioMiniFactory attached : " + currentDevice)

                    if (currentDevice != null) {

                        println("Device name : " + currentDevice?.getDeviceInfo()!!.deviceName)
                        println("Device SN : " + currentDevice?.getDeviceInfo()!!.deviceSN)
                        println("SDK version : " + currentDevice?.getDeviceInfo()!!.versionSDK)
                    }
                }


            }).start()

        } else if (currentDevice != null && event == IUsbEventHandler.DeviceChangeEvent.DEVICE_DETACHED && currentDevice!!.equals(dev)) {

            println("currentDevice : " + currentDevice)
           // Log.e("currentDevice : " + currentDevice)
            currentDevice = null
        }
    }

    fun startBiominiDevice(){

        if(bioMiniFactory != null){
            bioMiniFactory!!.close()
        }

        if(mbExternalUsbManager){
            usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
            bioMiniFactory = object : BioMiniFactory(mainContext,usbManager){

                override fun onDeviceChange(event: IUsbEventHandler.DeviceChangeEvent, dev: Any) {
                    println("----------------------------------------")
                    println("onDeviceChange : $event using external usb-manager")
                    println("----------------------------------------")
                    deviceChangeHandler(event, dev)
                }

            }

            pendingIntentPession = PendingIntent.getBroadcast(this,0, Intent(ACTION_USB_PERMISSION),0)
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(broadcastReciever, filter)
            checkDevice()
        }else{

            bioMiniFactory = object : BioMiniFactory(mainContext){

                override fun onDeviceChange(event: IUsbEventHandler.DeviceChangeEvent, dev: Any) {

                    deviceChangeHandler(event, dev)
                }



            }
        }
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        FrigerprintControl.frigerprint_power_on()
    //    defaultCaptureOptions.frameRate = IBioMiniDevice.FrameRate.SHIGH
//        defaultfingerPringerPrintCapture.frameRate = IBioMiniDevice.FrameRate.SHIGH
//        defaultfingerPringerPrintCapture.captureTemplate = true
//        defaultfingerPringerPrintCapture.captureImage = true



        var submitButton = findViewById<Button>(R.id.submit)

        submitButton.setOnClickListener {
//
//
//
//         try{
//
//             val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//             inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
//
//             findViewById<ImageView>(R.id.fingerprint_icon).setImageBitmap(null)
//             Log.d("currentDevice","currentDevice " + currentDevice)
//             if(currentDevice != null)
//
//             //  Log.d("currentDevice","currentDevice " + currentDevice)
//                 currentDevice!!.captureSingle(defaultCaptureOptions, mCaptureResponseDefault, true)
//            }catch (ex : Exception){
//                ex.printStackTrace()
//            }
              var registerIntent = Intent(this,Main2Activity::class.java)
              startActivity(registerIntent)


        }

        if(bioMiniFactory != null){
            bioMiniFactory?.close()
        }

        if(mbExternalUsbManager){

            checkDevice()
        }

        startBiominiDevice()




  }
}
