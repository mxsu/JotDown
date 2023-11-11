package com.example.iat359project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MoodActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private Button finishButton;
    private ImageButton buttonHappy, buttonSad, buttonAngry, buttonLove, buttonTired, buttonWorried, buttonExcited, buttonNeutral;
    private SensorManager sManager;
    private Sensor tempSens;
    SqlDatabase db;
    int moodVal;
    private float pressure;
    MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        initialization(); //Initialize the inputs/listeners

    }

    public void onClick(View v) {

        switch(v.getId()){ //Users sets their mood via button. Moods are stored as integer in the Constants class
            case R.id.happyButton: moodVal = Constants.HAPPY; break;
            case R.id.sadButton: moodVal = Constants.SAD; break;
            case R.id.angryButton: moodVal = Constants.ANGRY; break;
            case R.id.loveButton: moodVal = Constants.LOVE; break;
            case R.id.tiredButton: moodVal = Constants.TIRED; break;
            case R.id.worriedButton: moodVal = Constants.WORRIED; break;
            case R.id.excitedButton: moodVal = Constants.EXCITED; break;
            case R.id.neutralButton: moodVal = Constants.NEUTRAL; break;
        }
        //Moves to the gallery and commits all data to the SQL database. This is done here as it is the final activity in the chain of inputs.
        if(v.getId() == R.id.buttonFinish) {
            mp.start(); //Plays sound
            //Saves data to SharedPreferences
            SharedPreferences sp = getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            e.putInt(Constants.MOOD, moodVal);
            e.commit();
            //Commits all data from SharedPreferences to SQL
            addJournal(v);
            //Advances to gallery activity
            Intent i = new Intent(MoodActivity.this, GalleryActivity.class);
            startActivity(i);
        }
    }

    public void addJournal (View v){
        //Gets shared preferences access
        SharedPreferences sp = getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        //Gets text entry from SP
        String entry = sp.getString(Constants.TEXT, "No text entry."); //Will show "no text entry." if there was no entry written.
        //Gets date and formats it
        DateFormat format = new SimpleDateFormat("EEE d/MM/yyyy, HH:mm:ss"); //Date formatting
        String date = format.format(Calendar.getInstance().getTime()); //Sets date string according to formatting.
        //Gets mood entry from SP
        int mood = sp.getInt(Constants.MOOD, 8); //Will default to Happy if there was no mood input.
        //Gets atmospheric pressure from sensors (originally intended to be ambient temperature, thus naming)
        int temp = (int) pressure; //Sets temperature off sensor readings
        //Gets image file path from SP
        String image = sp.getString(Constants.IMAGE, "NO IMAGE");
        //Inserts the data into SQL
        long id = db.insertData(entry, mood, date, temp, image);
        if (id < 0) {
            Toast.makeText(this, "SQL ENTRY FAILURE", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "SQL ENTRY SUCCESS", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        //Gets sensor readings for the barometric sensor
        if (e.sensor.getType() == Sensor.TYPE_PRESSURE) {
            float[] vals = e.values;
            pressure = vals[0];
            //Log.e("Test!", pressure + "");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    protected void onResume(){
        super.onResume();
        sManager.registerListener((SensorEventListener) this, tempSens, SensorManager.SENSOR_DELAY_NORMAL); //Sensor register
    }

    @Override
    protected void onPause(){
        sManager.unregisterListener((SensorEventListener) this); //Sensor unregister
        super.onPause();
    }

    private void initialization(){
        finishButton = (Button)findViewById(R.id.buttonFinish);
        finishButton.setOnClickListener(this);
        buttonHappy = (ImageButton) findViewById(R.id.happyButton);
        buttonHappy.setOnClickListener(this);
        buttonSad = (ImageButton) findViewById(R.id.sadButton);
        buttonSad.setOnClickListener(this);
        buttonAngry = (ImageButton) findViewById(R.id.angryButton);
        buttonAngry.setOnClickListener(this);
        buttonLove = (ImageButton) findViewById(R.id.loveButton);
        buttonLove.setOnClickListener(this);
        buttonTired = (ImageButton) findViewById(R.id.tiredButton);
        buttonTired.setOnClickListener(this);
        buttonWorried = (ImageButton) findViewById(R.id.worriedButton);
        buttonWorried.setOnClickListener(this);
        buttonExcited = (ImageButton) findViewById(R.id.excitedButton);
        buttonExcited.setOnClickListener(this);
        buttonNeutral = (ImageButton) findViewById(R.id.neutralButton);
        buttonNeutral.setOnClickListener(this);
        db = new SqlDatabase(this);
        moodVal = Constants.HAPPY; //Defaults mood to happy, in case the user enters nothing.
        //Sensor initialization
        sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //tempSens = sManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        tempSens = sManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        //Creates a sound for when u create a new entry
        mp = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
    }

}