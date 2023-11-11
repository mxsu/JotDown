package com.example.iat359project;

import static java.lang.String.valueOf;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity implements View.OnClickListener, RecViewInterface{

    RecyclerView rec;
    SqlDatabase db;
    SqlHelper sqlHelp;
    RecAdapter recAdapt;

    Button entry; //Button to create a new entry
    ArrayList<Journal> aList; //Holds SQL entries as custom Journal object, capable of storing multiple variable types.
    MediaPlayer mp; //sound effect for deleting entries
    Dialog popUp; //Used for the individual journal entry view.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        initialization(); //Initialization
        insertIntoArrayList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){ //Initializes the Themes menus
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Themes settings for switching between light and dark mode
        switch (item.getItemId()){
            case R.id.lightMode:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case R.id.darkMode:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initialization(){
        rec = (RecyclerView)findViewById(R.id.recGallery);
        entry = (Button)findViewById(R.id.button_entry);
        entry.setOnClickListener(this);
        db = new SqlDatabase(this);
        sqlHelp = new SqlHelper(this);
        mp = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
        popUp = new Dialog(this);
    }

    public void insertIntoArrayList(){ //Creates the aList by parsing through the SQL database. Sends values to RecyclerView for adaptive display.
        Cursor cursor = db.getData(); //Cursor is used to parse through SQL database

        //Cursors for each of the SQL db's columns
        int i1 = cursor.getColumnIndex(Constants.TEXT);
        int i2 = cursor.getColumnIndex(Constants.MOOD);
        int i3 = cursor.getColumnIndex(Constants.DATE);
        int i4 = cursor.getColumnIndex(Constants.TEMP);
        int i5 = cursor.getColumnIndex(Constants.IMAGE);

        //Holds series of Journals
        aList = new ArrayList<Journal>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()){ //Iterates through all SQL rows
            //Gets SQL data to build Journal object
            String text = cursor.getString(i1);
            int mood = cursor.getInt(i2);
            String date = cursor.getString(i3);
            int temp = cursor.getInt(i4);
            String img = cursor.getString(i5);

            //A custom Journal object is used as it allows for us to mix multiple variable types within an ArrayList
            Journal j = new Journal(text, date, mood, temp, img);
            aList.add(j);

            cursor.moveToNext();
        }
        recAdapt = new RecAdapter(aList, this);
        rec.setAdapter(recAdapt);
    }

    public void onClick(View v) {
        if(v.getId() == R.id.button_entry) { //Goes to TextActivity to build a new entry.
            Intent i = new Intent(GalleryActivity.this, TextActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onItemClick(int pos) { //Click on RecyclerView object
        Toast.makeText(this, "CLICKED POS: " + pos, Toast.LENGTH_SHORT).show();
        popUpWindow(pos); //Creates pop-up of expanded view with full-size image
    }
    @Override
    public void onBackPressed() { //Prevents the user for going back to the "Mood" entry, as that would let them re-submit the same journal entry a second time.
        //Creates an alert dialogue box to ask the user if they want to exit the app, rather than normally just going back to the mood entry activity.
        //alert dialog from https://stackoverflow.com/questions/11740311/android-confirmation-message-for-delete
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Exit app?");
        builder.setMessage("Press exit to close the app");
        builder.setPositiveButton("Exit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Exit app and goes back to android home screen
                        //Does not destroy the app
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        //cancel button, does nothing returns back to the gallery activity
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRestart(){
        //when the user navigates back to the app it prompts the pin screen for security
        super.onRestart();
        Intent intent = new Intent(GalleryActivity.this, PincodeActivity.class);
        startActivity(intent);
    }


    @Override
    public void onItemLongClick(int pos) { //Press and hold to delete an entry
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //We use a pop-up to confirm the user wants to delete the entry they selected
        builder.setCancelable(true);
        builder.setTitle("Delete entry?");
        builder.setMessage("This cannot be undone");
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Deletes the entry in the SQL and then plays an alert sound to give feedback to the user
                        databaseDelete(pos);
                        mp.start();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //using the onLongClick position, we delete the SQL entry with that position
    public void databaseDelete(int pos){
        Journal entryToDelete = aList.get(pos);
        aList.remove(pos);
        recAdapt.notifyItemRemoved(pos);
        String[] timeToDelete = {entryToDelete.date}; //We use the date as a key to access specific entries as it is always unique (The string is unique down to the second)
        db.deleteData(timeToDelete);
    }
    public void popUpWindow(int pos){ //Pop-up used to display individual journal entries when clicked-on by the user
        //Specific journal entry, gotten by position in ArrayList
        Journal j = aList.get(pos);
        Bitmap myBitmap = null;
        popUp.setContentView(R.layout.activity_popup);
        popUp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //Initialization
        ImageView popUpImage = (ImageView) popUp.findViewById(R.id.popUpImage);
        TextView popUpText = (TextView) popUp.findViewById(R.id.popUpText);
        TextView popUpMood = (TextView) popUp.findViewById(R.id.popUpMood);
        TextView popUpDate = (TextView) popUp.findViewById(R.id.popUpDate);
        TextView popUpSens = (TextView) popUp.findViewById(R.id.popUpSens);
        if(j.getImage() != null && j.getImage() != "NO IMAGE") {
            File file = new File(j.getImage()); //Image is accessed by saved String
            myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath()); //Decode file as a bitmap for display
            //For if an image is rotated (some phones will automatically display all photos as landscape)
            //Will rotate the image to the correct orientation
            try {
                myBitmap = ImageActivity.rotatePhoto(file.getAbsolutePath(), myBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Get values from all of the journal's different variables
            popUpImage.setImageBitmap(myBitmap);
            popUpText.setText(j.getEntry());
            popUpDate.setText(j.getDate());
            popUpSens.setText(j.getTemp() +" atm"); //Barometric pressure
            switch (j.getMood()) {
                case Constants.HAPPY:
                    popUpMood.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(),R.color.happyGreen));
                    popUpImage.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(),R.color.happyGreen));
                    popUpMood.setText("Happy");
                    break;
                case Constants.SAD:
                    popUpMood.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.sadBlue));
                    popUpImage.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.sadBlue));
                    popUpMood.setText("Sad");
                    break;
                case Constants.ANGRY:
                    popUpMood.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.angryRed));
                    popUpImage.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.angryRed));
                    popUpMood.setText("Angry");
                    break;
                case Constants.LOVE:
                    popUpMood.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.lovePink));
                    popUpImage.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.lovePink));
                    popUpMood.setText("Love");
                    break;
                case Constants.TIRED:
                    popUpMood.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.tiredBrown));
                    popUpImage.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.tiredBrown));
                    popUpMood.setText("Tired");
                    break;
                case Constants.WORRIED:
                    popUpMood.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.worriedPurple));
                    popUpImage.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.worriedPurple));
                    popUpMood.setText("Worried");
                    break;
                case Constants.EXCITED:
                    popUpMood.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.excitedYellow));
                    popUpImage.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.excitedYellow));
                    popUpMood.setText("Excited");
                    break;
                case Constants.NEUTRAL:
                    popUpMood.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.neutralGrey));
                    popUpImage.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.neutralGrey));
                    popUpMood.setText("Neutral");
                    break;
            }
        }
        popUp.show();
    }

}

