package com.twoid.proserv_mobile;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.stetho.Stetho;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.twoid.proserv_mobile.apiUtils.ApiUtils;
import com.twoid.proserv_mobile.database.DEMOGRAPHICS_DATABASE;
import com.twoid.proserv_mobile.model.DeviceId;
import com.twoid.proserv_mobile.model.FacilitatorStructure;
import com.twoid.proserv_mobile.model.LearnerStructure;
import com.twoid.proserv_mobile.services.ApiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
import retrofit2.Callback;

public class DemographicRegistrationActivity extends AppCompatActivity {


    private TextInputEditText personNameTextInputEditText;
    private TextInputEditText personSurameTextInputEditText;
    private TextInputEditText studentNumberTextInputEditText;
    private TextInputEditText employeeNumberTextInputEditText;
    private CheckBox checkBox_facilitator;
    private Spinner spinner_course;
    private static final String TAG = "RESPONSE_BODY";
    private static final String TAG_SYNC = "SYNC_STATUS";
    private static final String TAG_DBLIST = "TAG_DBLIST";
    private static final String TAG_DBLISTTOJSON = "TAG_DBLISTTOJSON";
    private String TAG_DIVCE_PERMISSION_DENIED = "permission_denied";
    private String TAG_UUID = "UUID";
    private String TAG_DEVICE_ID = "DEVICE_ID";
    private String LearnerGuid;
    String syncstatus;
    String deviceSN = "";
    private ApiService apiService;
    private Button btnDemographicRegistration;
    ArrayList<String> arrayList = new ArrayList();

    DEMOGRAPHICS_DATABASE demographics_database = new DEMOGRAPHICS_DATABASE(this);
    String url = "http://41.76.212.32:8090/ProServeService.svc/GetCourses";
    private boolean isconnected;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demographic_registration);
        Stetho.initializeWithDefaults(this);

        personNameTextInputEditText = findViewById(R.id.firstName);
        personSurameTextInputEditText = findViewById(R.id.lastName);
        studentNumberTextInputEditText = findViewById(R.id.student_number);
       // employeeNumberTextInputEditText = findViewById(R.id.employeeNumber);

        btnDemographicRegistration = findViewById(R.id.registerDemographics);
        checkBox_facilitator = findViewById(R.id.checkbox_facilitator);
        spinner_course = findViewById(R.id.course_id);

        apiService = ApiUtils.getApiService();
        if(isNetworkconnected()){
            loadspinner(url);
            spinner_course.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    String course = spinner_course.getItemAtPosition(spinner_course.getSelectedItemPosition()).toString();
                    Toast.makeText(getApplicationContext(),"course selected is " + course,Toast.LENGTH_LONG).show();
                }

                @Override()
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                deviceSN = Build.SERIAL;

                Log.i(TAG_DEVICE_ID, "DEVICE ID IS " + deviceSN);

            }else {
                deviceSN = Build.getSerial();
                Log.i(TAG_DEVICE_ID, "DEVICE ID OLDER VERSION  IS " + deviceSN);
            }

            final LearnerStructure learnerStructure = new LearnerStructure();

            LearnerGuid = generateGUID();
            final Intent inten_fingerpting_registration = new Intent(getApplicationContext(), FingerPrintRegistrationActivity.class);

            checkBox_facilitator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkBox_facilitator.isChecked()) {
                        studentNumberTextInputEditText.setEnabled(false);
                        spinner_course.setEnabled(false);
                        Toast.makeText(getApplicationContext(), "STUDENT ID FIELD NOT ACTIVE", Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "COURSE  FIELD NOT ACTIVE", Toast.LENGTH_LONG).show();
                    }


                }
            });

            btnDemographicRegistration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkBox_facilitator.isChecked()) {

                        FacilitatorStructure facilitatorStructure = new FacilitatorStructure();

                        final String Name = facilitatorStructure.setName(personNameTextInputEditText.getText().toString());
                        final String Surname = facilitatorStructure.setSurname(personSurameTextInputEditText.getText().toString());

                        if (Name.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "FACILITATOR NAME CANNOT BE EMPTY", Toast.LENGTH_LONG).show();
                        } else if (Surname.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "FACILITATOR SURNAME CANNOT BE EMPTY", Toast.LENGTH_LONG).show();

                        } else {
                            inten_fingerpting_registration.putExtra("Name", Name);
                            inten_fingerpting_registration.putExtra("Surname", Surname);
                            inten_fingerpting_registration.putExtra("deviceSN", deviceSN);
                            startActivity(inten_fingerpting_registration);
                        }

                    } else {

                        final String Name = learnerStructure.setName(personNameTextInputEditText.getText().toString());
                        final String Surname = learnerStructure.setSurname(personSurameTextInputEditText.getText().toString());
                        final String RSAID = learnerStructure.setRSAID(studentNumberTextInputEditText.getText().toString());
                        final String course = learnerStructure.setCourse(spinner_course.getSelectedItem().toString());
                        inten_fingerpting_registration.putExtra("deviceSN", deviceSN);

                        if (Name.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "NAME CANNOT BE EMPTY", Toast.LENGTH_LONG).show();

                        } else if (Surname.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "SURNAME CANNOT BE EMPTY", Toast.LENGTH_LONG).show();

                        } else if (RSAID.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "ID FIELD CANNOT BE EMPTY", Toast.LENGTH_LONG).show();

                        } else if (course.isEmpty()) {

                            Toast.makeText(getApplicationContext(), "COURSE  FIELD CANNOT BE EMPTY", Toast.LENGTH_LONG).show();

                        } else {

                            inten_fingerpting_registration.putExtra("Name", Name);
                            inten_fingerpting_registration.putExtra("Surname", Surname);
                            inten_fingerpting_registration.putExtra("RSAID", Name);
                            inten_fingerpting_registration.putExtra("LearnerGuid", LearnerGuid);

                            inten_fingerpting_registration.putExtra("course", course);
                            startActivity(inten_fingerpting_registration);
                        }

                    }
                }
            });

        }else {

            Toast.makeText(getApplicationContext(),"PLEASE CHECK INTERNET CONNECTION",Toast.LENGTH_LONG).show();
        }

    }


//Volley library to fetch data from server(GetCourses)
    public  void loadspinner(String URL){

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

            @Override

            public void onResponse(String response) {

               try{

                   JSONObject jsonObject = new JSONObject(response);
                   Log.i(TAG_DEVICE_ID,"JSON OBJECT COURSES IS " + jsonObject);
                   JSONObject jsonObject1 = jsonObject.getJSONObject("GetCoursesResult");
                   Log.i(TAG_DEVICE_ID,"JSON OBJECT1 COURSES IS " + jsonObject1);

                   JSONArray jsonArray = jsonObject1.getJSONArray("Course");
                   Log.i(TAG_DEVICE_ID,"JSON ARRAY OBJECT1 COURSES IS " + jsonArray);

                   for (int i = 0; i <= jsonArray.length(); i++) {

                       arrayList.add(jsonArray.get(i).toString());
                       Log.i(TAG_SYNC,"arraylist is " + arrayList);
                   }

               }catch (Exception e){

               }

                spinner_course.setAdapter(new ArrayAdapter<String>(DemographicRegistrationActivity.this, android.R.layout.simple_spinner_dropdown_item, arrayList));

            }

        }, new Response.ErrorListener() {

            @Override

            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

            }

        });
        int socketTimeout = 30000;

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        stringRequest.setRetryPolicy(policy);

        requestQueue.add(stringRequest);
    }
    // Fetch courses from server in spinner

    //check network status
    public boolean isNetworkconnected(){

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return  cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    //sync db
    public void syncLocalDBwithServ() throws Exception{

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        List <LearnerStructure> jsonArrayList = demographics_database.getAllUsers();
        Log.i(TAG_DBLIST, "db is " + jsonArrayList);

        StringBuilder stringBuilder = new StringBuilder();
        for (LearnerStructure user: jsonArrayList){
            stringBuilder.append(user);
        }
        String s = stringBuilder.toString();
        Log.i(TAG_DBLIST, "converted string is " + s);
        if (jsonArrayList.size() != 0) {


                Log.i(TAG_DBLISTTOJSON, demographics_database.composeJsonFromDB());
                String json = demographics_database.convertToJson();

                Log.i(TAG_DBLIST, "json string is " + json);
                String json2 = json.substring(1,json.length()-1);
                LearnerStructure learnerStructure = new LearnerStructure();
                Gson gson = new Gson();
                String  userRegistration1 = gson.toJson(learnerStructure);
                Log.i(TAG_DBLIST, "learnerStructure IS " + learnerStructure);
//                Gson gson = new GsonBuilder().create();
//                ObjectMapper objectMapper = new ObjectMapper();
//                JSONParser parser = new JSONParser();
//                JSONObject json1 = (JSONObject) parser.parse(json2);
//                Log.i(TAG_DBLIST, "JSON USER REG JSON1 IS " + json1);
//                UserRegistration userRegistration1 = new UserRegistration();
//                UserRegistration learnerStructure = objectMapper.readValue(json2,UserRegistration.class);
//                Log.i(TAG_DBLIST, "JSON USER REG IS " + learnerStructure);

//                apiService.saveDemographicsToServer(learnerStructure).enqueue(new Callback<LearnerStructure>() {
//                @Override
//                public void onResponse(Call<LearnerStructure> call, Response<LearnerStructure> response) {
//
//                }
//
//                @Override
//                public void onFailure(Call<LearnerStructure> call, Throwable t) {
//
//                }
//            });


                                                                          }

//          //    requestParams.put("json", userRegistration);
//                asyncHttpClient.addHeader("Content-Type", "application/json");
//                requestParams.put("json", json1);
//                asyncHttpClient.post("http://169.254.208.79:8091/api/demographics/registration", requestParams, new AsyncHttpResponseHandler() {
//
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                        Toast.makeText(getApplicationContext(), "DB Sync completed!", Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//
//                        if (statusCode == 404) {
//                            Toast.makeText(getApplicationContext(), "SERVER NOT UP", Toast.LENGTH_LONG).show();
//                        } else if (statusCode == 500) {
//                            Toast.makeText(getApplicationContext(), "SOMETHING WENT WRONG ON SERVER SIDE", Toast.LENGTH_LONG).show();
//                        } else if (statusCode == 400) {
//                            Toast.makeText(getApplicationContext(), "BAD REQUEST", Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]", Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
//            } else {
//                Toast.makeText(getApplicationContext(), "SQLite and Remote MySQL DBs are in Sync!", Toast.LENGTH_LONG).show();
           }


            private String generateGUID(){
                UUID uuid = UUID.randomUUID();
                Log.i(TAG_UUID , "guid is " + uuid.toString());
                return uuid.toString();
            }


    }


