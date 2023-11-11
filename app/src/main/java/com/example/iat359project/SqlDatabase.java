package com.example.iat359project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SqlDatabase {
    private SQLiteDatabase db;
    private Context context;
    private final SqlHelper sqlHelp;

    public SqlDatabase (Context c){
        context = c;
        sqlHelp = new SqlHelper(context);
    }

    public long insertData (String text, int mood, String date, int temp, String img){
        //Mood is made of 8 constants (1 = Happy, 2 = Sad, etc.) See the constants class for exact values.

        //Create objects
        db = sqlHelp.getWritableDatabase();
        ContentValues vals = new ContentValues();

        //Assign new values
        vals.put(Constants.TEXT, text);
        vals.put(Constants.MOOD, mood);
        vals.put(Constants.DATE, date);
        vals.put(Constants.TEMP, temp);
        vals.put(Constants.IMAGE, img);

        long id = db.insert(Constants.TABLE_NAME, null, vals);
        return id;
    }

    public Cursor getData(){ //Returns a cursor object used to navigate the SQL table's data
        SQLiteDatabase db = sqlHelp.getWritableDatabase();

        String[] cols = {Constants.UID, Constants.TEXT, Constants.MOOD, Constants.DATE, Constants.TEMP, Constants.IMAGE};
        Cursor c = db.query(Constants.TABLE_NAME, cols, null, null, null, null, null);
        return c;
    }
    //Deletes a row from the table by searching for it based on date (unique identifier, the date variable is saved down to the second)
    public void deleteData(String[] date){
        SQLiteDatabase db = sqlHelp.getWritableDatabase();
        db.delete(Constants.TABLE_NAME, Constants.DATE + "=?", date);
    }
}
