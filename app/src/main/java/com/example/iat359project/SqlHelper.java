package com.example.iat359project;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class SqlHelper extends SQLiteOpenHelper {

    private Context context;
    //Constant used to create the SQL table
    private static final String CREATE_TABLE =
            "CREATE TABLE " +
                    Constants.TABLE_NAME + " (" +
                    Constants.UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Constants.TEXT + " TEXT, " +
                    Constants.MOOD + " INTEGER, " +
                    Constants.DATE + " TEXT, " +
                    Constants.TEMP + " INTEGER, " +
                    Constants.IMAGE + " TEXT);" ;

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + Constants.TABLE_NAME;

    public SqlHelper(Context context){
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            //try to build table
            db.execSQL(CREATE_TABLE);
            Toast.makeText(context, "SQL Helper onCreate() - Success", Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
            Toast.makeText(context, "SQL Helper onCreate() - Failure", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int verOld, int verNew) {
        //upgrade the SQL table
        try {
            db.execSQL(DROP_TABLE);
            onCreate(db);
            Toast.makeText(context, "SQL Helper onUpgrade() - Success", Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
            Toast.makeText(context, "SQL Helper onUpgrade() - Failure", Toast.LENGTH_LONG).show();
        }
    }
}
