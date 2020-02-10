package com.twoid.proserv_mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ftransisdk.FrigerprintControl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.suprema.BioMiniFactory;
import com.suprema.CaptureResponder;
import com.suprema.IBioMiniDevice;
import com.suprema.IUsbEventHandler;
import com.twoid.proserv_mobile.apiUtils.ApiUtils;
import com.twoid.proserv_mobile.model.Constants;

import com.twoid.proserv_mobile.model.DeviceId;
import com.twoid.proserv_mobile.model.LearnerStructure;

import com.twoid.proserv_mobile.model.UsersData;
import com.twoid.proserv_mobile.model.WSQFingerprints;

import com.twoid.proserv_mobile.services.ApiService;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class FingerPrintRegistrationActivity extends AppCompatActivity {

    public static final boolean mbUsbExternalUSBManager = false;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager = null;
    private FingerPrintRegistrationActivity mainContext;
    private PendingIntent mPermissionIntent= null;
    private String WSQImagesBase64_6_leftThumb;private String WSQImagesBase64_1_right_thumb;

    private String WSQImagesBase64_3_index_right;
    private String WSQImagesBase64_4_index_left;

    private String syncStatus;private String GUID ;
    private String TAG_DIVCE_PERMISSION_DENIED="permission_denied";
    private String TAG_EXTRACTED_TEMPLATE = "TEMPLATE_EXTRACTED";
    private String TAG_IMAGE_CONVERTED = "CONVERTED_IMAGE";
    private String TAG_JSON_STRUCTURE = "JSON";
    private String TAG_REQUEST_PARAMS = "REQUEST PARAMS";
    private String TAG_ERROR="error";
    String deviceSn= "18016022300013";
    private String TAG_ERROR_CODE="capture_error_code";
    private String TAG_mCurrentDevice= "mCurrentDevice attached";
    private ApiService apiService;
    private String TAG__onCaptureSuccess= "onCaptureSuccess";
    private String TAG_UUID = "UIID";
    public IBioMiniDevice mCurrentDevice = null;
    private static BioMiniFactory mBioMiniFactory = null;

    private IBioMiniDevice.CaptureOption mCaptureOptionDefault = new IBioMiniDevice.CaptureOption();


    ArrayList<UsersData> mUsers = new ArrayList<>();


    CaptureResponder captureResponder = new CaptureResponder() { //right thumb capture
        @Override
        public boolean onCaptureEx(Object context, final Bitmap capturedImage, final IBioMiniDevice.TemplateData capturedTemplate, IBioMiniDevice.FingerState fingerState) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (capturedImage != null) {
                        ImageView rightTumbImage = findViewById(R.id.WSQImagesBase64_1);
                        if (rightTumbImage != null) {
                            rightTumbImage.setImageBitmap(capturedImage);
                        }
                    }

                    IBioMiniDevice.TemplateData cpTemplate = mCurrentDevice.extractTemplate();

                    if (cpTemplate != null) {
                        mUsers.add(new UsersData(cpTemplate.data, cpTemplate.data.length));
                        Toast.makeText(getApplicationContext(), "ADDED TO USERS", Toast.LENGTH_LONG).show();
//                        boolean isMatched = false;
//                        for (UsersData data : mUsers) {
//                            if (mCurrentDevice.verify(cpTemplate.data, cpTemplate.data.length,
//                                    cpTemplate.data,cpTemplate.data.length));
//                                    isMatched = true;
//
//
//                        }
//                        if (isMatched){
//                            Toast.makeText(getApplicationContext(),"MATCH FOUND",Toast.LENGTH_LONG).show();
//                        }else {
//                            Toast.makeText(getApplicationContext(),"MATCH NOT FOUND",Toast.LENGTH_LONG).show();
//
                    }

                }
            });





            try {
                byte[] rightThumbArray = mCurrentDevice.getCaptureImageAsWsq(512, 512, 0.8f, 0);
                if (rightThumbArray != null) {
                    WSQImagesBase64_1_right_thumb = Base64.encodeToString(rightThumbArray, Base64.DEFAULT);
                    ArrayList<String> list = new ArrayList<>();
                    list.add(WSQImagesBase64_1_right_thumb);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }


            return true;
        }

        @Override
        public void onCaptureError(Object o, int i, String s) {
            super.onCaptureError(o, i, s);
        }
    };


    //Added By Vusi
    private LearnerStructure Learner = null;
    private String learnerWSQ = "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";



    private CaptureResponder leftThumbCaptureResponder = new CaptureResponder() { //left thumb capture
        @Override
        public boolean onCaptureEx(Object context, final Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate, IBioMiniDevice.FingerState fingerState) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (capturedImage!= null){
                        ImageView leftThumbImg = findViewById(R.id.WSQImagesBase64_2);
                        if (leftThumbImg != null){
                            leftThumbImg.setImageBitmap(capturedImage);
                        }
                    }
                }
            });

            try {
                byte [] leftThumbArray = mCurrentDevice.getCaptureImageAsWsq(512, 512, 0.8f, 0);
                if (leftThumbArray != null){
                    WSQImagesBase64_6_leftThumb = convertByteArrayString(leftThumbArray);

                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
//            if (capturedTemplate != null){
//                mUsers.add(new Users(capturedTemplate.data,capturedTemplate.data.length));
//            }

            return  true;
        }

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

        apiService = ApiUtils.getApiService();
        ImageView rightThumbImage = findViewById(R.id.WSQImagesBase64_1);
        ImageView leftThumbImage = findViewById(R.id.WSQImagesBase64_2);



        Button button_fingerprint_capture = findViewById(R.id.fingerprint_capture);
       // FrigerprintControl.frigerprint_power_on();
        mainContext = this;
        getAllUsers();
      //  findByFingerPrint();


        mCaptureOptionDefault.frameRate = IBioMiniDevice.FrameRate.SHIGH;
        if (rightThumbImage != null) {
            rightThumbImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((ImageView) findViewById(R.id.WSQImagesBase64_1)).setImageBitmap(null);
                    if (mCurrentDevice != null) {
                        mCurrentDevice.captureSingle(mCaptureOptionDefault, captureResponder, true);

                    }
                }
            });
        }

        if (leftThumbImage != null) {
            leftThumbImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((ImageView) findViewById(R.id.WSQImagesBase64_2)).setImageBitmap(null);
                    if (mCurrentDevice != null) {
                        mCurrentDevice.captureSingle(mCaptureOptionDefault, leftThumbCaptureResponder, true);
                    }
                }
            });
        }


        button_fingerprint_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                if (mCurrentDevice != null){
//                    if (mUsers.size() == 0){
//                        Toast.makeText(getApplicationContext(),"NO ENROLLED DATA",Toast.LENGTH_LONG).show();
//                        return;
//                    }
//                }



               //  getAllLearnersFromDB();
                SharedPreferences sharedPreferences = getSharedPreferences("templates",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gsonPref = new Gson();
                String jsontemplates =gsonPref.toJson(mUsers);
                editor.putString("jsontemplates",jsontemplates);
                editor.apply();
                Toast.makeText(getApplicationContext(),"ADDED TO SHARED PREFS",Toast.LENGTH_LONG).show();

                if (isNetworkconnected()) {
                       syncStatus = Constants.getSyncStatusOk();
                       savaBiometricsToDB();
                       Bundle extras = getIntent().getExtras();

                        String Name = extras.getString("Name");
                        String Surname = extras.getString("Surname");
                        String RSAID = extras.getString("RSAID");
                        String course = extras.getString("course");
                        String LearnerGuid = new LearnerStructure().setLearnerGuid(extras.getString("LearnerGuid"));
                        String DeviceSN = extras.getString("deviceSN");


                         String WSQImagesBase64_1 = learnerWSQ;
                         String WSQImagesBase64_2= learnerWSQ;
                         String WSQImagesBase64_3 = learnerWSQ;
                         String WSQImagesBase64_4 = learnerWSQ;
                         String WSQImagesBase64_5 = learnerWSQ;
                         String WSQImagesBase64_6 = learnerWSQ;
                         String WSQImagesBase64_7 = learnerWSQ;
                         String WSQImagesBase64_8 = learnerWSQ;
                         String WSQImagesBase64_9 = learnerWSQ;
                         String WSQImagesBase64_10 = learnerWSQ;

                       WSQFingerprints WSQFingerprintImages  = new WSQFingerprints(WSQImagesBase64_1,WSQImagesBase64_2,
                                                                              WSQImagesBase64_3,WSQImagesBase64_4,WSQImagesBase64_5,WSQImagesBase64_6,WSQImagesBase64_7,WSQImagesBase64_8,
                                                                              WSQImagesBase64_9,WSQImagesBase64_10);
                    Learner = new LearnerStructure(Name,Surname,LearnerGuid,RSAID,course,WSQFingerprintImages);

                    JSONObject postData = new JSONObject();
                    String json = "";
                    try
                    {
                        DeviceId deviceId = new DeviceId(DeviceSN);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("DeviceSN",DeviceSN);



                        postData =  jsonObject.put("Learner", Learner);
                        Log.i(TAG__onCaptureSuccess,"postData IS " + postData);
                        //    String json1 = postData.toString();


                       if (postData != null) {
                           new SaveLearnerAsync().execute("http://41.76.212.32:8090/ProServeService.svc/saveLearner");
                           //new DemographicDetails().execute("http://41.76.212.32:8090/ProServeService.svc/saveLearner", postData.toString());
                       }else {

                       }

                        Log.i(TAG__onCaptureSuccess,"jsonObject1 is " + postData);

                    }catch (Exception exception){
                        exception.printStackTrace();
                    }

                        final String TAG_LearnerStructure = "Learner";
                        Log.i(TAG_LearnerStructure, "Learner is " + Learner);
                        Gson gson = new Gson();
//                        String json = gson.toJson(Learner);
                    //    String deviceSN = extras.getString("deviceSN");
                        // String DeviceSN = "18016022300013";
                        // String DeviceSN = "18016022300013";
                         DeviceId deviceId = new DeviceId(DeviceSN);
                         String deviceSNtoJson = gson.toJson(deviceId);
                         String learner = gson.toJson(Learner);
                       // String deviceSn = deviceId.setDeviceSn(deviceId);


                                 apiService.saveDemographicsToServer(deviceId, Learner).enqueue(new Callback<LearnerStructure>() {
                                      @Override
                                        public void onResponse(Call<LearnerStructure> call, Response<LearnerStructure> response) {

                                              if (response.isSuccessful()){
                                                  Log.i(TAG__onCaptureSuccess,"STATUS CODE IS " + response.code());
                                              }else {
                                                  Log.i(TAG_ERROR,"STATUS CODE IS " + response.code());
                                              }

                                           }

                                          @Override
                                          public void onFailure(Call<LearnerStructure> call, Throwable t) {
                                                   t.printStackTrace();
                                                 }
                                            });
                                     RequestParams requestParams = new RequestParams();
                           requestParams.put("DeviceSN",deviceId);
                           requestParams.put("jsonObject1",postData);
                           Log.i(TAG__onCaptureSuccess,"PostData is " + postData);


                       AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

                       asyncHttpClient.post(mainContext, "http://41.76.212.32:8090/ProServeService.svc/saveLearner",  requestParams,  new BaseJsonHttpResponseHandler() {
                           @Override
                           public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                               Log.i(TAG__onCaptureSuccess,"json success is " + rawJsonResponse + "status code is " + statusCode);
                               Toast.makeText(getApplicationContext(),"on success " + rawJsonResponse,Toast.LENGTH_LONG).show();
                           }

                           @Override
                           public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                               Log.i(TAG__onCaptureSuccess,"json failure is " + rawJsonData + "status code is " + statusCode);
                               Toast.makeText(getApplicationContext(),"on failure " + rawJsonData,Toast.LENGTH_LONG).show();
                           }

                           @Override
                           protected Object parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                               return null;
                           }
                       });



                    }else {
                    syncStatus = Constants.getSyncStatusFail();

                    savaBiometricsToDB();

                }

            }

        });

        if(mBioMiniFactory != null) {
            mBioMiniFactory.close();
        }
       restartBioMini();


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
//Base64 encode
    public String convertByteArrayString(byte[] array){
        try{

            return Base64.encodeToString(array,Base64.DEFAULT);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    //check network status
    public boolean isNetworkconnected(){

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return  cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    //save baiometrics to db
    public void savaBiometricsToDB(){
        // GUID = generateGUID();
        Bundle extras = getIntent().getExtras();

          if (extras != null){

                String Name = extras.getString("Name");
                String Surname = extras.getString("Surname");
                String RSAID = extras.getString("RSAID");
                String course = extras.getString("course");
                String learnerGuid = extras.getString("LearnerGuid");

                WSQFingerprints wSQFingerprints = new WSQFingerprints(WSQImagesBase64_1_right_thumb, WSQImagesBase64_6_leftThumb,WSQImagesBase64_3_index_right,WSQImagesBase64_4_index_left);
                wSQFingerprints.save();//Save to WSQFingerprints db
                Toast.makeText(getApplicationContext(),"SAVING FINGERPRINT TO DB",Toast.LENGTH_LONG).show();
                LearnerStructure learnerStructure = new LearnerStructure(Name,Surname,learnerGuid,RSAID,course,wSQFingerprints);
                learnerStructure.save();// Save to LearnerStructure db
                Toast.makeText(getApplicationContext(),"SAVING DEMOGRAPHICS TO DB",Toast.LENGTH_LONG).show();

            }
              else
                     {

                        Toast.makeText(getApplicationContext(),"Not saved to db",Toast.LENGTH_SHORT).show();
                    }




    }

    private class DemographicDetails extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {

            String data = "";
            HttpURLConnection httpURLConnection = null;
            try{

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);

                DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                dataOutputStream.writeBytes("PostData" + params[1]);
                dataOutputStream.flush();
                dataOutputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int inputStreamData = inputStreamReader.read();
                while(inputStreamData != -1){
                    char current = (char)inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data +=current;
                }

            }catch (Exception exception){
                exception.printStackTrace();
            }finally {
                if (httpURLConnection != null){
                    httpURLConnection.disconnect();
                }
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.i(TAG__onCaptureSuccess,"result" + result);
            Log.e("TAG", result);
            Toast.makeText(getApplicationContext(), "result is " + result,Toast.LENGTH_LONG).show();
        }

    }

    public List getAllUsers(){

        List<LearnerStructure> list = LearnerStructure.listAll(LearnerStructure.class);
        Log.i(TAG_UUID,"all in db is " + list);
        try {
            String fileName = "all";
            FileOutputStream fileOutputStream = openFileOutput(fileName,MODE_PRIVATE);
            fileOutputStream.write(list.toString().getBytes());
            fileOutputStream.close();
        }catch (IOException allUsers){
            allUsers.printStackTrace();
        }
        return  list;
    }
//
//    private String getAllLearnersFromDB() {
//
//          List <LearnerStructure> learnerList = LearnerStructure.listAll(LearnerStructure.class);
//          Gson gson = new Gson();
//          String learJsonList  = gson.toJson(learnerList);
//          ObjectMapper objectMapper = new ObjectMapper();
//          Log.i(TAG_JSON_STRUCTURE,"learnerList is " + learnerList);
//          String conevrtJson = learJsonList.substring(1,learJsonList.length()-1);
//          Log.i(TAG_JSON_STRUCTURE,"conevrtJson is " + conevrtJson);
//
//        try {
//
//            String fileName = "base64";
//
////            FileOutputStream fileOutputStream = openFileOutput(fileName,MODE_PRIVATE);
////            fileOutputStream.write(WSQImagesBase64_1_right_thumb.getBytes());
////            fileOutputStream.close();
////            File file = new File(getFilesDir(),fileName);
//            System.out.println("Successfully wrote to the file.");
//        } catch (Exception e) {
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }
//
//         try{
//
//
//             String jsonfile = "json";
//             FileOutputStream fileOutputStream = openFileOutput(jsonfile,MODE_PRIVATE);
//             fileOutputStream.write(conevrtJson.getBytes());
//             fileOutputStream.close();
//             JSONObject jsonObject = new JSONObject(learJsonList);
//             JSONArray jsonArray = new JSONArray();
//             jsonArray.add(jsonObject);
//             Log.i(TAG_JSON_STRUCTURE, "jsonArray is " + jsonArray);
//
//             JSONObject jsonObject1 = jsonObject.getJSONObject("WSQFingerprints");
//             String s = jsonObject1.getString("WSQImagesBase64_1");
//             Toast.makeText(getApplicationContext(),"s is " + s,Toast.LENGTH_LONG).show();
//             JSONArray jsonArray1 = new JSONArray();
//             jsonArray1.add(jsonObject1);
//             Log.i(TAG_JSON_STRUCTURE,"jsonArray1 is " + jsonArray1);
//             for (int k =0 ; k<= jsonArray1.size();k++)
//             if (s.equals(WSQImagesBase64_1_right_thumb.getBytes())){
//                 Toast.makeText(getApplicationContext(),"MATCH FOUND AT GET BYTES",Toast.LENGTH_LONG).show();
//             }else {
//                 Toast.makeText(getApplicationContext(),"MATCH NOT FOUND AT GET BYTES",Toast.LENGTH_LONG).show();
//             }
//
//                  String name =   jsonObject.getString("Name");
//                  String surname =  jsonObject.getString("Surname");
//                  String leranerguid = jsonObject.getString("LearnerGuid");
//                  String rsaid=   jsonObject.getString("RSAID");
//                  String course = jsonObject.getString("course");
//
//                  LearnerStructure learnerStructure = new LearnerStructure();
//                  WSQFingerprints wSQFingerprints = new WSQFingerprints();
//                  learnerStructure.setName(name);
//                  learnerStructure.setSurname(surname);
//                  learnerStructure.setLearnerGuid(leranerguid);
//                  learnerStructure.setRSAID(rsaid);
//
//                  learnerStructure.setCourse(course);
//
//                  Log.i(TAG_JSON_STRUCTURE," Name is " + name);
//                  Log.i(TAG_JSON_STRUCTURE," surname is " + surname);
//                  Log.i(TAG_JSON_STRUCTURE," course is " + course);
//                  Log.i(TAG_JSON_STRUCTURE," rsaid is " + rsaid);
//
////                  JSONObject jsonObject1 =  jsonObject.getJSONObject("WSQFingerprints");
////                  String wSQFingerprints_1_rightThumb = jsonObject1.getString("WSQImagesBase64_1");
//               //   Log.i(TAG_JSON_STRUCTURE, "wSQFingerprints_1_rightThumb is " + wSQFingerprints_1_rightThumb);
//
////                   if(wSQFingerprints_1_rightThumb.equals(WSQImagesBase64_1_right_thumb)){
////
////                            Toast.makeText(getApplicationContext(),"MATCH FOUND ", Toast.LENGTH_LONG).show();
////
////                             }
////                               else {
////
////                             Toast.makeText(getApplicationContext(),"MATCH NOT FOUND ",Toast.LENGTH_LONG).show();
////
////                      }
////
////
////                   Log.i(TAG_JSON_STRUCTURE," wSQFingerprints_1_rightThumb is " + wSQFingerprints_1_rightThumb);
//              }catch (Exception exception){
//                  exception.printStackTrace();
//              }
//
//            return  learJsonList;
//    }







    private class SaveLearnerAsync extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {

            String response = "";
            HttpURLConnection httpURLConnection = null;
            try{

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setConnectTimeout(1000);
                Map hashMap = new HashMap<String, String>();
                hashMap.put("DeviceSN", "18016022300013");
                hashMap.put("Learner",Learner);

                //Added By Vusi To Test
                Gson gson = new Gson();

                String strContent = gson.toJson(hashMap);
                OutputStream writer = httpURLConnection.getOutputStream();
                writer.write(strContent.getBytes());
                writer.flush();
                writer.close();

                Integer resCode = httpURLConnection.getResponseCode();
                if (resCode == 200)
                {
                    BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                    String inputLine;
                    StringBuffer response2 = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response2.append(inputLine);
                    }
                    in.close();
                    response = response2.toString();
                }

            }catch (Exception exception)
            {
                exception.printStackTrace();
            }
            finally
            {
                if (httpURLConnection != null)
                {
                    httpURLConnection.disconnect();
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.i(TAG__onCaptureSuccess,"result" + result);
            Log.e("TAG", result);
            Toast.makeText(getApplicationContext(), "result is " + result,Toast.LENGTH_LONG).show();
        }

    }

}
