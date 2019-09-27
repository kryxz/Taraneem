package com.taraneem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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
import java.util.Objects;

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

        //Fragments without back/up button
        int[] viewsSet = new int[]{R.id.mainFragment, R.id.settingsFragment, R.id.infoFragment};
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(viewsSet).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        getUserData(this);
    }

    private void setLanguage() {
        String langCode = getSharedPreferences("userPrefs", 0).getString("langPref", "");
        if (langCode.isEmpty()) return;
        Common.setLocale(langCode, this);

    }

    private static void getUserData(Activity activity) {
        final User user = new User();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userID = activity.getSharedPreferences("userPrefs", 0).getString("userID", "");

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

                TempData.setUserData(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //empty override.
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }
}
