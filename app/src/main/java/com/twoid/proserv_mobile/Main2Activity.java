package com.twoid.proserv_mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.ftransisdk.FrigerprintControl;
import com.suprema.BioMiniFactory;
import com.suprema.CaptureResponder;
import com.suprema.IBioMiniDevice;
import com.suprema.IUsbEventHandler;
import com.twoid.proserv_mobile.model.DemographicRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class Main2Activity extends AppCompatActivity {

    public static final boolean mbUsbExternalUSBManager = false;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent= null;
    private String loginFinger;
    private static BioMiniFactory mBioMiniFactory = null;
    public static final int REQUEST_WRITE_PERMISSION = 786;
    public IBioMiniDevice mCurrentDevice = null;
    private Main2Activity mainContext;
    private String TAG_IMAGE_CONVERTED = "CONVERTED_IMAGE";
    private String TAG_ERROR="error";
    private String TAG_ERROR_CODE="capture_error_code";
    private String TAG_DIVCE_PERMISSION_DENIED="permission_denied";
    private String TAG_mCurrentDevice= "mCurrentDevice attached";
    private String TAG__onCaptureSuccess= "onCaptureSuccess";
    private String TAG__isMatch= "isMatch";

    class UserData{
        byte[] template;

        public UserData(byte[] template) {
            this.template = template;
        }
    }

    private ArrayList<UserData>mUsers = new ArrayList<>();
    private IBioMiniDevice.CaptureOption mCaptureOptionDefault = new IBioMiniDevice.CaptureOption();

    private CaptureResponder mCaptureResponseDefault = new CaptureResponder() {

        @Override
        public boolean onCaptureEx(final Object context, final Bitmap capturedImage,
                                   final IBioMiniDevice.TemplateData capturedTemplate,
                                   final IBioMiniDevice.FingerState fingerState) {
            Log.i(TAG__onCaptureSuccess, "onCapture : Capture successful!");


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (capturedImage != null) {
                        ImageView imageView = findViewById(R.id.fingerprint_icon);
                        if (imageView != null) {
                            imageView.setImageBitmap(capturedImage);
                        }
                    }
                }
            });

            try{
                   Log.i(TAG_mCurrentDevice, "INSIDE TRY BLOCK");
                    byte[] rawImage = mCurrentDevice.getCaptureImageAsWsq(512, 512, 0.8f, 0);
                    if(rawImage != null){
                        loginFinger = convertByteArrayString(rawImage);
                    }

            }catch (Exception ex) {

                ex.printStackTrace();
            }

            if(!isNetworkconnected()){
                if (capturedImage != null){
                    if (mUsers.size() > 0){
                        boolean isMatched = false;
                        for(UserData userData : mUsers){
                            if (mCurrentDevice.verify(capturedTemplate.data, capturedTemplate.data.length,
                                    userData.template,userData.template.length)){

                                isMatched = true;
                            }
                        }

                        if(isMatched){
                            Log.i(TAG__isMatch,"match is found");
                        }
                    }
                }
            }
            return true;
        }



        @Override
        public void onCaptureError(Object contest, int errorCode, String error) {
            Log.i(TAG_ERROR,"CAPTURE ERROR IS " + error);
            if (errorCode != IBioMiniDevice.ErrorCode.OK.value());
            Log.i(TAG_ERROR_CODE, "ERROR CODE IS" + errorCode );

        }
    };




    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)){
                synchronized (this){
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)){
                        if (device != null){
                            if (mBioMiniFactory ==null) return;

                            mBioMiniFactory.addDevice(device);
                            System.out.println(String.format(Locale.ENGLISH ,"Initialized device count- BioMiniFactory (%d)" , mBioMiniFactory.getDeviceCount() ));
                        }
                    }else{
                        Log.i(TAG_DIVCE_PERMISSION_DENIED,"permission denied for device " + device);
                    }
                }

            }
        }
    };


    public void checkDevice(){

        if(mUsbManager == null) return;
        HashMap<String,UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIter = deviceList.values().iterator();
        while(deviceIter.hasNext()){
            UsbDevice _device = deviceIter.next();
            if (_device.getVendorId() ==0x16d1){
                mUsbManager.requestPermission(_device,mPermissionIntent);

            }else{

            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        FrigerprintControl.frigerprint_power_on();
        mainContext = this;

        mCaptureOptionDefault.frameRate = IBioMiniDevice.FrameRate.SHIGH;

       Button button = findViewById(R.id.submit);
       if(button != null) {
           button.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                 ((ImageView) findViewById(R.id.fingerprint_icon)).setImageBitmap(null);
                        mCaptureOptionDefault.captureTemplate =true;
                       if(mCurrentDevice != null) {
                           //mCaptureOptionDefault.captureTimeout = (int)mCurrentDevice.getParameter(IBioMiniDevice.ParameterType.TIMEOUT).value;
                           mCurrentDevice.captureSingle(
                                   mCaptureOptionDefault,
                                   mCaptureResponseDefault,
                                   true);
                       }
                   }

           });

       }
        if(mBioMiniFactory != null) {
            mBioMiniFactory.close();
        }
        restartBioMini();
        Intent intent = new Intent(this, DemographicRegistration.class);
        startActivity(intent);
    }


    void handleDevChange(IUsbEventHandler.DeviceChangeEvent event, Object dev) {
        if (event == IUsbEventHandler.DeviceChangeEvent.DEVICE_ATTACHED && mCurrentDevice == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int cnt = 0;
                    while (mBioMiniFactory == null && cnt < 20) {
                        SystemClock.sleep(1000);
                        cnt++;
                    }
                    if (mBioMiniFactory != null) {
                        mCurrentDevice = mBioMiniFactory.getDevice(0);
                        Log.d(TAG_mCurrentDevice, "mCurrentDevice attached : " + mCurrentDevice);
                        if (mCurrentDevice != null) {
                            System.out.println(" DeviceName : " + mCurrentDevice.getDeviceInfo().deviceName);
                            System.out.println("         SN : " + mCurrentDevice.getDeviceInfo().deviceSN);
                            System.out.println("SDK version : " + mCurrentDevice.getDeviceInfo().versionSDK);
                        }
                    }
                }
            }).start();
        } else if (mCurrentDevice != null && event == IUsbEventHandler.DeviceChangeEvent.DEVICE_DETACHED && mCurrentDevice.isEqual(dev)) {
            Log.d(TAG_mCurrentDevice, "mCurrentDevice removed : " + mCurrentDevice);
            mCurrentDevice = null;
        }
    }

    void restartBioMini() {
        if(mBioMiniFactory != null) {
            mBioMiniFactory.close();
        }

        if( mbUsbExternalUSBManager ){
            mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
            mBioMiniFactory = new BioMiniFactory(mainContext, mUsbManager){
                @Override
                public void onDeviceChange(DeviceChangeEvent event, Object dev) {
                    System.out.println("--------------------------------------");
                    System.out.println("onDeviceChange : " + event + " using external usb-manager");
                    System.out.println("--------------------------------------");
                    handleDevChange(event, dev);
                }
            };
            //
            mPermissionIntent = PendingIntent.getBroadcast(this,0,new Intent(ACTION_USB_PERMISSION),0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            registerReceiver(mUsbReceiver, filter);
            checkDevice();
        }else {
            mBioMiniFactory = new BioMiniFactory(mainContext) {
                @Override
                public void onDeviceChange(DeviceChangeEvent event, Object dev) {
                    System.out.println("--------------------------------------");
                    System.out.println("onDeviceChange : " + event);
                    System.out.println("--------------------------------------");
                    handleDevChange(event, dev);
                }
            };
        }
    }

    public String convertByteArrayString(byte[] array){
       try{

           return Base64.encodeToString(array,Base64.DEFAULT);

       }catch (Exception ex){

       }
        return "";
    }

    //check network status
    public boolean isNetworkconnected(){

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return  cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
