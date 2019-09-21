package com.taraneem;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;


public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLanguage();
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);


        //setLanguage
    }

    void setLanguage() {
        String langCode = getSharedPreferences("userPrefs", 0).getString("langPref", "");
        if (langCode.isEmpty()) return;
        MainActivity.setLocale(langCode, this);

    }
}


