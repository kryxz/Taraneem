package com.taraneem;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taraneem.data.TempData;
import com.taraneem.data.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.MainTheme);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        setLanguage();

        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        navController = Navigation.findNavController(this, R.id.fragment_host);
        NavigationUI.setupActionBarWithNavController(this, navController);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        Set<Integer> set = new HashSet<>();
        //Fragments without back/up button
        set.add(R.id.mainFragment);
        set.add(R.id.settingsFragment);
        set.add(R.id.infoFragment);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(set).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        getUserData();
    }

    static void getUserData() {
        final User user = new User();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid().substring(0, 10);
        FirebaseDatabase.getInstance().getReference().child("Users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String dob = Objects.requireNonNull(dataSnapshot.child("dob").getValue()).toString();
                String email = Objects.requireNonNull(dataSnapshot.child("email").getValue()).toString();
                String name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                String phoneNo = Objects.requireNonNull(dataSnapshot.child("phoneNo").getValue()).toString();

                HashMap<String, String> bookings = new HashMap<>();
                for (DataSnapshot ds : dataSnapshot.child("bookings").getChildren())
                    bookings.put(ds.getKey(), Objects.requireNonNull(ds.getValue()).toString());

                user.setDob(dob);
                user.setEmail(email);
                user.setName(name);
                user.setPhoneNo(phoneNo);
                user.setId(dataSnapshot.getKey());
                user.setBookings(bookings);
                Log.i("Dataaaaa", bookings.values().toString());
                Log.i("Dataaaaa", bookings.keySet().toString());

                for (String value : bookings.values())
                    Log.i("Dataaaaa", value);

                for (String key : bookings.keySet())
                    Log.i("Dataaaaa", key);

                TempData.setUserData(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //empty override.
            }
        });
    }

    void setLanguage() {
        String langCode = getSharedPreferences("userPrefs", 0).getString("langPref", "");
        if (langCode.isEmpty()) return;
        setLocale(langCode, this);

    }

    static void setLocale(String langCode, Activity activity) {
        Configuration config = activity.getResources().getConfiguration();
        config.locale = new Locale(langCode);
        activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }
}
