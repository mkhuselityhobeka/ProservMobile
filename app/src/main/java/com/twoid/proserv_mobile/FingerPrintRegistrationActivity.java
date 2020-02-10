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

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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

                         String WSQImagesBase64_1 = "";
                         String WSQImagesBase64_2= "";
                         String WSQImagesBase64_3 = "";
                         String WSQImagesBase64_4 = "";
                         String WSQImagesBase64_5 = "";
                         String WSQImagesBase64_6 = "";
                         String WSQImagesBase64_7 = "";
                         String WSQImagesBase64_8 = "";
                         String WSQImagesBase64_9 = "";
                         String WSQImagesBase64_10 = "";

                       WSQFingerprints WSQFingerprintImages  = new WSQFingerprints(WSQImagesBase64_1,WSQImagesBase64_2,
                                                                              WSQImagesBase64_3,WSQImagesBase64_4,WSQImagesBase64_5,WSQImagesBase64_6,WSQImagesBase64_7,WSQImagesBase64_8,
                                                                              WSQImagesBase64_9,WSQImagesBase64_10);
                    LearnerStructure Learner = new LearnerStructure(Name,Surname,LearnerGuid,RSAID,course,WSQFingerprintImages);

                    JSONObject postData = new JSONObject();
                    String json = "";
                    try{
                             DeviceId deviceId = new DeviceId(DeviceSN);
                             JSONObject jsonObject = new JSONObject();
                             jsonObject.put("DeviceSN",DeviceSN);

                              postData =  jsonObject.put("Learner", Learner);
                              Log.i(TAG__onCaptureSuccess,"postData IS " + postData);
                         //    String json1 = postData.toString();


                               if (postData != null) {
                                   new DemographicDetails().execute("http://41.76.212.32:8090/ProServeService.svc/saveLearner", postData.toString());
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
}
