package com.taraneem;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taraneem.data.Booking;
import com.taraneem.data.TempData;
import com.taraneem.data.User;

import java.util.HashMap;
import java.util.Objects;

import static com.taraneem.Common.getEventPath;


public class InfoFragment extends Fragment {

    private View view; //instead of using getView(). This makes things better.

    private Booking booking;
    final private Fragment fragment = this;

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
                booking.getPhotoOptions(), booking.getHospitality(),
                booking.getOthers(), String.valueOf(booking.getInviteesCount())));

        ((AppCompatTextView) (view.findViewById(R.id.bookingDone))).setText(getString(R.string.bookingDone, booking.getHallName()));
        ((AppCompatTextView) (view.findViewById(R.id.infoPriceText))).setText(
                getString(R.string.total_price, booking.getPrice())
        );
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
                        Common.viewInfoDialog(infoMessage, booking.getHallName(), view);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //required empty override.
            }
        });
        view.findViewById(R.id.doneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                updateBookingData(v);
            }
        });
        view.findViewById(R.id.editButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog();
            }
        });

    }


    private String removeWeekDays(String string) {
        return string.substring(0, string.lastIndexOf('-') + 3);
    }


    private void updateBookingData(final View v) {
        String userID = Objects.requireNonNull(getContext()).getSharedPreferences("userPrefs", 0).getString("userID", "");
        User user = TempData.getUserData();
        HashMap<String, String> bookings = new HashMap<>();
        if (user.getBookings() != null)
            bookings = user.getBookings();

        bookings.put(booking.getId(), removeWeekDays(booking.getEventDate()) + "'" + booking.getHallName());
        user.setBookings(bookings);
        FirebaseDatabase.getInstance().getReference().child("Users").child(userID).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    DatabaseReference eventRef = getEventPath(booking.getEventDate(), booking.getId(), booking.getHallName());
                    eventRef.setValue(booking).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Navigation.findNavController(v).navigate(R.id.infoToMain);
                            Toast.makeText(view.getContext(), getString(R.string.bookingDone, booking.getHallName()), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }


    private void showEditDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        @SuppressLint("InflateParams")//hides ide warning
        final View layout = getLayoutInflater().inflate(R.layout.edit_booking_dialog, null);

        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        dialog.show();

        AppCompatSpinner
                photoSpinner = layout.findViewById(R.id.editPhotoSpinner),
                inviteesSpinner = layout.findViewById(R.id.editInviteesSpinner);

        final AppCompatTextView
                editHospitalityText = layout.findViewById(R.id.editHospitalityText),
                editOthersText = layout.findViewById(R.id.editOthersText),
                //view because this is from the fragment!
                infoPriceText = view.findViewById(R.id.infoPriceText);

        editHospitalityText.setText(booking.getHospitality());
        editOthersText.setText(booking.getOthers());
        booking.setRes(view.getContext().getResources()); //res is needed to calculate the final price!
        Common.setSpinnerAdapter(photoSpinner, infoPriceText, R.array.photographyOptions, booking, view);
        Common.setSpinnerAdapter(inviteesSpinner, infoPriceText, R.array.inviteesNumbers, booking, view);
        editHospitalityText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHospitalityDialog(booking, editHospitalityText, infoPriceText, fragment, view);
            }
        });
        editOthersText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showOthersDialog(booking, editOthersText, fragment, view);
            }
        });
        layout.findViewById(R.id.confirmBookingEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

}
