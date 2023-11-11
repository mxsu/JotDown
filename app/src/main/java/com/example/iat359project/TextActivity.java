package com.example.iat359project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TextActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    Button buttonNext;
    EditText textEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        //View initialization
        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(this);
        textEntry = (EditText)findViewById(R.id.editTextJournalEntry);
        textEntry.addTextChangedListener(this);
    }


    public void onClick(View v) {
        if(v.getId() == R.id.buttonNext) { //Advances to next activity
            //Storage in Shared Preferences for access later
            SharedPreferences sp = getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            e.putString(Constants.TEXT, String.valueOf(textEntry.getText()));
            e.commit();
            //Next activity
            Intent i = new Intent(TextActivity.this, ImageActivity.class);
            startActivity(i);
        }
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {
        View view = this.getCurrentFocus();
        //We were having an interface issue where when we press enter it entered a new line,
        //when we wanted the keyboard to be hidden instead.

        //Makes the user keyboard exit when they press 'enter', rather than having a linebreak.
        if(editable.length()>1) {
            if (editable.charAt(editable.length() - 1) == '\n') {
                //When the last character of the EditText is a new line, it hides the keyboard and removes the last character.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                editable.delete(editable.length()-1,editable.length());
            }
        }
    }
}