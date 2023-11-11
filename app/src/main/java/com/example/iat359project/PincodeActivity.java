package com.example.iat359project;


//import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hanks.passcodeview.PasscodeView;

public class PincodeActivity extends AppCompatActivity {
    //PasscodeView from https://github.com/hanks-zyh/PasscodeView
    PasscodeView passcodeView;
    //password is hardcoded in
    String password = "77777";
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        passcodeView = findViewById(R.id.pview);
        mp = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);

        // to set length of password as here
        // we have set the length as 5 digits
        passcodeView.setPasscodeLength(5)
                // to set pincode or passcode
                .setLocalPasscode(password)

                // to set listener to it to check whether
                // passwords has matched or failed
                .setListener(new PasscodeView.PasscodeViewListener() {
                    @Override
                    public void onFail() {
                        // to show message when Password is incorrect
                        Toast.makeText(PincodeActivity.this, "Password is wrong!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(String number) {
                        //plays a sound for user feedback when successfully entered
                        mp.start();
                        //unlocks and sends user to the gallery activity
                        Intent intent_passcode = new Intent(PincodeActivity.this, GalleryActivity.class);
                        startActivity(intent_passcode);
                    }
                });
    }
}