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

import com.suprema.BioMiniFactory;
import com.suprema.CaptureResponder;
import com.suprema.IBioMiniDevice;
import com.suprema.IUsbEventHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class FingerPrintRegistration extends AppCompatActivity {

    public static final boolean mbUsbExternalUSBManager = false;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager = null;
    private FingerPrintRegistration mainContext;
    private PendingIntent mPermissionIntent= null;
    private String loginFinger;
    private String TAG_DIVCE_PERMISSION_DENIED="permission_denied";
    private String TAG_EXTRACTED_TEMPLATE = "TEMPLATE_EXTRACTED";
    private String TAG_IMAGE_CONVERTED = "CONVERTED_IMAGE";
    private String TAG_ERROR="error";
    private String TAG_ERROR_CODE="capture_error_code";

    private String TAG_mCurrentDevice= "mCurrentDevice attached";
    private String TAG__onCaptureSuccess= "onCaptureSuccess";
    public IBioMiniDevice mCurrentDevice = null;
    private static BioMiniFactory mBioMiniFactory = null;

    private IBioMiniDevice.CaptureOption mCaptureOptionDefault = new IBioMiniDevice.CaptureOption();

    class  Users{
        byte [] template;
        public Users(byte[] data,int len) {
            this.template = Arrays.copyOf(data,len);
        }


    }
    ArrayList<Users> mUsers = new ArrayList<>();
    private CaptureResponder captureResponder = new CaptureResponder() {
        @Override
        public boolean onCaptureEx(Object context, final Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate, IBioMiniDevice.FingerState fingerState) {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (capturedImage != null){
                               ImageView imageView = findViewById(R.id.leftThumb);
                            if (imageView != null){
                                imageView.setImageBitmap(capturedImage);
                            }
                        };

                    }
                });

                if(capturedTemplate != null){
                    mUsers.add(new Users(capturedTemplate.data, capturedTemplate.data.length));
                }else{
                    Log.i(TAG_EXTRACTED_TEMPLATE,"TEMPLATE NOT EXTRACTED");
                }

                return true;
            };



        @Override
        public void onCaptureError(Object o, int i, String s) {
            super.onCaptureError(o, i, s);
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
        setContentView(R.layout.activity_finger_print_registration);

        Button button_left_thumb =  findViewById(R.id.fingerprint_capture);
        button_left_thumb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (mCurrentDevice != null){
                    mCurrentDevice.captureSingle(mCaptureOptionDefault,captureResponder, true);
                }
            }
        });
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
