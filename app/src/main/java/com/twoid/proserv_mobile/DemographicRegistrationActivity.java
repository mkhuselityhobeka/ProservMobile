package com.twoid.proserv_mobile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.twoid.proserv_mobile.apiUtils.ApiUtils;
import com.twoid.proserv_mobile.database.DEMOGRAPHICS_DATABASE;
import com.twoid.proserv_mobile.model.UserRegistration;
import com.twoid.proserv_mobile.services.ApiService;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DemographicRegistrationActivity extends AppCompatActivity {


    private TextInputEditText personNameTextInputEditText;
    private TextInputEditText personSurameTextInputEditText;
    private TextInputEditText studentNumberTextInputEditText;
    private TextInputEditText employeeNumberTextInputEditText;
    private Spinner spinner;
    private static final String TAG = "RESPONSE_BODY";
    private static final String TAG_SYNC = "SYNC_STATUS";
    private static final String TAG_DBLIST = "TAG_DBLIST";
    private static final String TAG_DBLISTTOJSON = "TAG_DBLISTTOJSON";
    private ApiService apiService;
    private Button btnDemographicRegistration;
    private String TAG_DIVCE_PERMISSION_DENIED="permission_denied";
    DEMOGRAPHICS_DATABASE demographics_database = new DEMOGRAPHICS_DATABASE(this);

    private boolean isconnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demographic_registration);

        Stetho.initializeWithDefaults(this);// db library

        personNameTextInputEditText = findViewById(R.id.firstName);
        personSurameTextInputEditText = findViewById(R.id.lastName);
        studentNumberTextInputEditText = findViewById(R.id.student_number);
        employeeNumberTextInputEditText = findViewById(R.id.employeeNumber);
        btnDemographicRegistration = findViewById(R.id.registerDemographics);
        //spinner = findViewById(R.id.studentNumber_Spinner);
        apiService = ApiUtils.getApiService();
        btnDemographicRegistration.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                UserRegistration userRegistration = new UserRegistration();
                final String personName =  userRegistration.setPersonName(personNameTextInputEditText.getText().toString());
                final String personsurname = userRegistration.setPersonSurname(personSurameTextInputEditText.getText().toString());
                final String studentNumber =  userRegistration.setStudentNumber(studentNumberTextInputEditText.getText().toString());
                final String employeeNumber = userRegistration.setEmployeeNumber(employeeNumberTextInputEditText.getText().toString());

              // save data to db


                    if(isNetworkconnected()){

                        String syncstatus = userRegistration.setSyncStatus("1");
                        Log.i(TAG_SYNC, syncstatus);
                        UserRegistration demographicRegistration = new UserRegistration(personName,personsurname,studentNumber,employeeNumber,syncstatus);
                        demographicRegistration.save();

                            Log.i(TAG, "DEVICE IS CONNECTED");

                            if(personName.isEmpty()) {

                                Toast.makeText(getApplicationContext(),"PLEASE ENTER FIRSTNAME",Toast.LENGTH_SHORT).show();

                            }else if(personsurname.isEmpty()){

                                Toast.makeText(getApplicationContext(),"PLEASE ENTER LASTNAME",Toast.LENGTH_SHORT).show();

                            }else if(studentNumber.isEmpty()) {

                                Toast.makeText(getApplicationContext(),"PLEASE ENTER STUDENT NUMBER",Toast.LENGTH_SHORT).show();

                            }else if(employeeNumber.isEmpty()){
                                Toast.makeText(getApplicationContext(),"PLEASE ENTER EMPLOYEE NUMBER",Toast.LENGTH_SHORT).show();

                            }else {

                                // call apiservice method
                                apiService.saveDemographicsToServer(userRegistration).enqueue(new Callback<UserRegistration>() {
                                    @Override
                                    public void onResponse(Call<UserRegistration> call, Response<UserRegistration> response) {

                                        if (response.isSuccessful()) {
                                            String responseBody = response.body().toString();

                                            Log.i(TAG, responseBody);
                                            Toast.makeText(getApplicationContext(), "REGISTERED DEMOGRAPHICS", Toast.LENGTH_SHORT).show();

                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<UserRegistration> call, Throwable t) {
                                        Log.i(TAG, " FAILED ");
                                        Toast.makeText(getApplicationContext(), "FAILED TO REGISTER DEMOGRAPHICS", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                    }else{

                        Log.i(TAG, "DEVICE IS NOT CONNECTED");
                        String syncstatus = userRegistration.setSyncStatus("0");
                        Log.i(TAG_SYNC, syncstatus);
                        UserRegistration demographicRegistration = new UserRegistration(personName,personsurname,studentNumber,employeeNumber,syncstatus);
                        demographicRegistration.save();


                        AlertDialog.Builder builder = new AlertDialog.Builder(DemographicRegistrationActivity.this);
                        builder.setMessage("DATABASES ARE NOT IN SYNC");
                        builder.setPositiveButton("SYNC NOW", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    syncLocalDBwithServ();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                }

            });
        }


    //check network status
    public boolean isNetworkconnected(){

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return  cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    //sync db
    public void syncLocalDBwithServ() throws Exception {

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        List <UserRegistration> jsonArrayList = demographics_database.getAllUsers();
        Log.i(TAG_DBLIST, "db is " + jsonArrayList);

        StringBuilder stringBuilder = new StringBuilder();
        for (UserRegistration user: jsonArrayList){
            stringBuilder.append(user);
        }
        String s = stringBuilder.toString();
        Log.i(TAG_DBLIST, "converted string is " + s);
        if (jsonArrayList.size() != 0) {


                Log.i(TAG_DBLISTTOJSON, demographics_database.composeJsonFromDB());
                String json = demographics_database.convertToJson();

                Log.i(TAG_DBLIST, "json string is " + json);
                String json2 = json.substring(1,json.length()-1);
                UserRegistration userRegistration = new UserRegistration();
                ObjectMapper objectMapper = new ObjectMapper();
                userRegistration = objectMapper.readValue(json,UserRegistration.class);
                Log.i(TAG_DBLIST, "userRegistration IS " + userRegistration);
//                Gson gson = new GsonBuilder().create();
//                ObjectMapper objectMapper = new ObjectMapper();
//                JSONParser parser = new JSONParser();
//                JSONObject json1 = (JSONObject) parser.parse(json2);
//                Log.i(TAG_DBLIST, "JSON USER REG JSON1 IS " + json1);
//                UserRegistration userRegistration1 = new UserRegistration();
//                UserRegistration userRegistration = objectMapper.readValue(json2,UserRegistration.class);
//                Log.i(TAG_DBLIST, "JSON USER REG IS " + userRegistration);

                apiService.saveDemographicsToServer(userRegistration).enqueue(new Callback<UserRegistration>() {
                @Override
                public void onResponse(Call<UserRegistration> call, Response<UserRegistration> response) {

                }

                @Override
                public void onFailure(Call<UserRegistration> call, Throwable t) {

                }
            });


                                                                          }

//          //  requestParams.put("json", userRegistration);
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
//            }
        }
    }


