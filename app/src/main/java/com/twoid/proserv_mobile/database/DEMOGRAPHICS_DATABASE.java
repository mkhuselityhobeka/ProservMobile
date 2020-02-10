package com.twoid.proserv_mobile.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.twoid.proserv_mobile.model.LearnerStructure;


import java.util.List;

public class DEMOGRAPHICS_DATABASE extends SQLiteOpenHelper {

    public DEMOGRAPHICS_DATABASE(Context applicationcontext){
        super(applicationcontext, "androidsqlite.db", null, 1);
    }

    private static final String TAG_LISTTOJSON = "LISTTOJSON";


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
//        String query;
//        query = "CREATE TABLE USER_REGISTRATION ( ID INTEGER PRIMARY KEY, PersonName TEXT, PersonSurname TEXT, StudentNumber TEXT,EmployeeNumber TEXT, syncStatus TEXT)";
//        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String query;
        query = "DROP TABLE IF EXISTS USER_REGISTRATION";
        sqLiteDatabase.execSQL(query);
        onCreate(sqLiteDatabase);
    }




    public List getAllUsers(){

        List<LearnerStructure> list = LearnerStructure.listAll(LearnerStructure.class);

         return  list;
    }

    //compose json from the db
    public String  composeJsonFromDB(){

        List<LearnerStructure> list = LearnerStructure.find(LearnerStructure.class,"SYNC_STATUS=?","0");
        Gson gson = new GsonBuilder().create();

        return gson.toJson(list);
    }

    public String convertToJson(){

        String json = composeJsonFromDB();
        String conevrtJson = json.substring(1,json.length()-1);
        Gson gson = new GsonBuilder().create();

        return gson.toJson(conevrtJson);
    }

    public  String getAllLearnersToJson(){
        List<LearnerStructure> list = LearnerStructure.listAll(LearnerStructure.class);
        Gson gson = new GsonBuilder().create();
        return  gson.toJson(list);
    }
//get sync status
    public String getSyncStatus(){

        String msg = null;

        if (this.syncCount() == 0){

            msg = "SQLite and Remote MySQL DBs are in Sync!";
        }else {

            msg = "DB Sync needed\n";
        }

        return msg;
    }

    //sync count
    public int syncCount() {

        int count = 0;

        String selectQuery = "SELECT  * FROM USER_REGISTRATION where syncStatus = '"+"0"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery,null);
        count = cursor.getCount();
        database.close();


        return count;
    }



}
