package com.taraneem;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taraneem.data.Booking;
import com.taraneem.data.TempData;

import java.util.HashMap;
import java.util.Objects;


public class InfoFragment extends Fragment {
    private View view; //instead of using getView(). This makes things better.

    private Booking booking;

    public InfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        //view is not null here.
        view = v;
        booking = TempData.getCurrentBooking(); //get booking data from TempData.
        setFields();
        super.onViewCreated(view, savedInstanceState);
    }

    private void setFields() {
        //wedding, birthday...
        String bookingChoice = "";
        //2 hours, 3 hours.
        String bookingDuration = getString(R.string.number_hour, booking.getEventDuration());

        // int hours ==> 1 hours.
        if (booking.getEventDuration() == 1)
            bookingDuration = getString(R.string.one_hour);
        //This is required so that it's translatable.
        switch (booking.getBookingType()) {
            case "Wedding":
                bookingChoice = getString(R.string.wedding);
                break;
            case "Birthday":
                bookingChoice = getString(R.string.birthday);
                break;
            case "Graduation":
                bookingChoice = getString(R.string.graduation);
                break;
            case "Store":
                bookingChoice = getString(R.string.ceremony);
                break;
        }

        //sets texts
        ((AppCompatTextView) (view.findViewById(R.id.bookingInfo))).setText(getString(R.string.bookingInfoFull,
                bookingChoice, booking.getEventDate(), booking.getEventTime(), bookingDuration,
                booking.getPhotoOptions(), booking.getHospitality(), booking.getOthers()));

        ((AppCompatTextView) (view.findViewById(R.id.bookingDone))).setText(getString(R.string.bookingDone, booking.getHallName()));

        //get hall data from fire base.
        final HashMap<String, String> hall = new HashMap<>();
        FirebaseDatabase.getInstance().getReference().child("Halls")
                .child(booking.getHallName()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //add hall data to hall HashMap.
                for (DataSnapshot ds : dataSnapshot.getChildren())
                    hall.put(ds.getKey(), Objects.requireNonNull(ds.getValue()).toString());

                view.findViewById(R.id.hallInfo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //store hall info in a formatted way. Check out infoMessage in strings.xml.
                        String infoMessage = getString(R.string.infoMessage,
                                hall.get("address"),
                                hall.get("phone"),
                                hall.get("info"));


                        //show hall info dialog, which is the same as the one in Booking Fragment.
                        BookingFragment.viewInfoDialog(infoMessage, booking.getHallName(), view);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //required empty override.
            }
        });

    }
}
