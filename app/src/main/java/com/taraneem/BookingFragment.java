package com.taraneem;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taraneem.data.Booking;
import com.taraneem.data.TempData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class BookingFragment extends Fragment {
    private View view;

    private Booking thisBooking;

    public BookingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_booking, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //view fully created. we can use it now.
        this.view = view;
        thisBooking = new Booking();
        hallsSpinner(); //gets Halls data from Fire base and puts them in the spinner.
        getData(); //gets data from previous fragment. Determines Wedding/Birthday etc..
        setUpFields(); //sets some texts. Initializes spinners and their listeners. Adds a listener to other views(Duration text, submit button, etc).

        super.onViewCreated(view, savedInstanceState);
    }

    private void sendBookingData(DatabaseReference databaseReference) {
        //all booking data are correct. Date and time are available. We can now store them in the database.

        TempData.setCurrentBooking(thisBooking); //stores booking object to temp data.

        //random uuid.
        String uuid = thisBooking.getId().substring(0, 5) + UUID.randomUUID().toString().substring(0, 10);
        databaseReference.child(uuid)
                .setValue(thisBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //go to booking info fragment.
                Navigation.findNavController(view).navigate(R.id.bookingToInfo);
            }
        });
    }

    private void submitNow() {
        if (thisBooking.allFieldsOK()) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(thisBooking.getHallName()).child(thisBooking.yearOfDate())
                    .child(thisBooking.monthOfDate()).child(thisBooking.dayOfDate());

            thisBooking.setId(FirebaseAuth.getInstance().getUid());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean isTaken = true;
                    if (!dataSnapshot.exists() || !dataSnapshot.hasChildren())
                        sendBookingData(databaseReference);
                    else
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            isTaken = true; //Assume this time is taken, for now.

                            //requireNonNull ==> Cannot be null. if null, throws NullPointerException.
                            String time = Objects.requireNonNull(ds.child("eventTime").getValue()).toString();
                            int dur = Integer.parseInt(Objects.requireNonNull(ds.child("eventDuration").getValue()).toString());

                            int hours = Integer.parseInt(time.substring(0, time.indexOf(":")));
                            int minutes = Integer.parseInt(time.substring(time.indexOf(":") + 1)) + 30;

                            int endHours = dur + hours + minutes / 60;
                            int endMinutes = minutes % 60;
                            int newHours = thisBooking.hourOfEvent();
                            int newMinutes = thisBooking.minutesOfEvent();

                            float theTime = hours + (float) minutes / 60;
                            float endTime = endHours + (float) endMinutes / 60;
                            float newTime = newHours + (float) newMinutes / 60;
                            float newEndTime = thisBooking.getEventDuration() + newHours + (float) newMinutes / 60;

                            Log.i("BookingNow", "theTime: " + theTime);
                            Log.i("BookingNow", "endTime: " + endTime);
                            Log.i("BookingNow", "newTime: " + newTime);
                            Log.i("BookingNow", "newEndTime: " + newEndTime);
                            if ((newTime < endTime && newTime >= theTime)
                                    || (newTime < endTime && newTime >= theTime - 1)
                                    || (newEndTime <= endTime && newEndTime > theTime)) {
                                Log.i("BookingNow", "Floating... We're in range! Don't Book!");
                                Log.i("BookingNow", Objects.requireNonNull(ds.getKey()));
                                Log.i("BookingNow", "Existing event starts at " + hours + ":" + minutes
                                        + " and ends at " + endHours + ":" + endMinutes);
                                break;
                            }
                            Log.i("BookingNow", "Not in Range");
                            Log.i("BookingNow", "-------------------------");
                            isTaken = false; //time is free. User can Book!

                        }
                    if (!isTaken) {
                        sendBookingData(databaseReference);
                        Log.i("BookingNow", "We gotcha. Booking Now!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //Empty required override.
                }
            });

        } else
            Toast.makeText(view.getContext(), getString(R.string.checkFields), Toast.LENGTH_SHORT).show();
    }


    private HashMap<String, String> getMap(DataSnapshot hall) {
        //Take a Data snapshot and iterate through its children to make a HashMap.

        //declare map
        HashMap<String, String> map = new HashMap<>();
        //a for loop to iterate through children.
        for (DataSnapshot dataSnapshot : hall.getChildren())
            //requireNonNull ==> Cannot be null. if null, throws NullPointerException.
            map.put(dataSnapshot.getKey(), Objects.requireNonNull(dataSnapshot.getValue()).toString());
        return map;
    }

    private void showHospitalityDialog(final AppCompatTextView textView) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        @SuppressLint("InflateParams")//hides ide warning
        //layout used in this dialog
        final View layout = getLayoutInflater().inflate(R.layout.hospitality_dialog, null);


        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        dialog.show();

        //declare a few check boxes and a button.
        final AppCompatCheckBox waterCheck = layout.findViewById(R.id.waterCheck);
        final AppCompatCheckBox kCheck = layout.findViewById(R.id.knafehCheck);
        final AppCompatCheckBox pepsiCheck = layout.findViewById(R.id.pepsiCheck);
        final AppCompatCheckBox cakeCheck = layout.findViewById(R.id.cakeCheck);
        final AppCompatButton confirmHospitality = layout.findViewById(R.id.confirmHospitality);

        final StringBuilder hospitality = new StringBuilder();
        final String currentHospitality = thisBooking.getHospitality();

        //check if field already contains a specific string. Check box accordingly.
        waterCheck.setChecked(currentHospitality.contains(getString(R.string.water)));
        kCheck.setChecked(currentHospitality.contains(getString(R.string.knafeh)));
        pepsiCheck.setChecked(currentHospitality.contains(getString(R.string.pepsi)));
        cakeCheck.setChecked(currentHospitality.contains(getString(R.string.cake)));

        //confirm button listener
        confirmHospitality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (waterCheck.isChecked())
                    hospitality.append(getString(R.string.water));

                if (kCheck.isChecked())
                    if (hospitality.length() == 0)
                        hospitality.append(getString(R.string.knafeh));
                    else
                        hospitality.append(", ").append(getString(R.string.knafeh));
                if (pepsiCheck.isChecked())
                    if (hospitality.length() == 0)
                        hospitality.append(getString(R.string.pepsi));
                    else
                        hospitality.append(", ").append(getString(R.string.pepsi));

                if (cakeCheck.isChecked())
                    if (hospitality.length() == 0)
                        hospitality.append(getString(R.string.cake));
                    else
                        hospitality.append(", ").append(getString(R.string.cake));
                if (hospitality.length() != 0)
                    textView.setText(hospitality.toString());
                else
                    textView.setText(getString(R.string.tap_to_choose));

                //add selected to booking data
                thisBooking.setHospitality(hospitality.toString());
                dialog.dismiss(); //hide dialog
            }
        });
    }

    private void hallsSpinner() {
        //get data from FireBase and set up the halls spinner

        //declaring variables
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(); //to reach database.
        final AppCompatSpinner hallSpinner = view.findViewById(R.id.hallSpinner); //set the adapter to this spinner.
        final ArrayList<String> hallsList = new ArrayList<>(); //put names in this list.
        final ArrayList<HashMap<String, String>> halls = new ArrayList<>(); //put all data in this list, to access it later on.

        //                                  SingleValueEvent so we won't keep listening for changes in the database.
        databaseReference.child("Halls").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChildren())//check if "Halls" has children.
                    for (DataSnapshot hall : dataSnapshot.getChildren()) {// iterate through them
                        hallsList.add(hall.getKey()); //add the name to hallsList
                        halls.add(getMap(hall)); //put a HashMap of data in halls.
                    }

                //Adapter for spinner.
                final ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(),
                        android.R.layout.simple_spinner_dropdown_item, hallsList);

                //Set view type for adapter.
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Assign the new adapter to the spinner.

                hallSpinner.setAdapter(adapter);
                //Adds a listener to the 'info', 'i' icon to display a dialog when clicked!
                view.findViewById(R.id.hallInfoBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Message to show; formatted in strings.xml...
                        String infoMessage = getString(R.string.infoMessage,
                                halls.get(hallSpinner.getSelectedItemPosition()).get("address"),
                                halls.get(hallSpinner.getSelectedItemPosition()).get("phone"),
                                halls.get(hallSpinner.getSelectedItemPosition()).get("info"));

                        //Hall name as a title of the dialog.
                        String title = halls.get(hallSpinner.getSelectedItemPosition()).get("name");
                        viewInfoDialog(infoMessage, title, view);
                    }
                });
                //halls spinner listener.
                hallSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        //adds selected item to booking data.
                        thisBooking.setHallName(hallsList.get(i));

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        //Empty required override.
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Empty required override.
            }

        });


    }


    private void setUpFields() {

        //declares spinners
        AppCompatSpinner photoSpinner = view.findViewById(R.id.photoSpinner);
        AppCompatSpinner otherSpinner = view.findViewById(R.id.otherSpinner);
        AppCompatSpinner inviteesSpinner = view.findViewById(R.id.inviteesSpinner);

        //sets spinners items and their listeners.
        setSpinnerAdapter(photoSpinner, R.array.photographyOptions);
        setSpinnerAdapter(otherSpinner, R.array.others);
        setSpinnerAdapter(inviteesSpinner, R.array.inviteesNumbers);

        //declare hospitality text view. add a listener to it.
        final AppCompatTextView hospitalityText = view.findViewById(R.id.hospitalityText);


        //add data to booking Object...
        thisBooking.setHospitality(getResources().getStringArray(R.array.photographyOptions)[0]);
        thisBooking.setPhotoOptions(getResources().getStringArray(R.array.photographyOptions)[0]);
        thisBooking.setOthers(getResources().getStringArray(R.array.others)[0]);
        thisBooking.setInviteesCount(Integer.parseInt(getResources().getStringArray(R.array.inviteesNumbers)[0]));

        //declare event date editText. adds a onClick listener that shows a DatePick dialog onClick.
        final AppCompatEditText dateEd = view.findViewById(R.id.eventDate);
        //shows dialog to pick date onclick
        showDatePickerDialog(dateEd);

        //declares event duration, start and end time text views.
        final AppCompatTextView durationText = view.findViewById(R.id.durationText);
        final AppCompatTextView startTime = view.findViewById(R.id.startTime);
        final AppCompatTextView endTime = view.findViewById(R.id.endTime);
        //final String timeFormat = "%02d:%02d";

        durationText.setText(getString(R.string.number_hour, 1));
        startTime.setText(getString(R.string.timeFormat, 12, 0));
        endTime.setText(getString(R.string.timeFormat, timeAfter(12, 1), 0));

        //change duration when clicked. shows an error if duration is invalid.
        durationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //gets end time and start time to check if duration is valid.
                String endTimeStr = endTime.getText().toString();
                String startTimeStr = startTime.getText().toString();

                int minutes = Integer.parseInt(endTimeStr.substring(endTimeStr.indexOf(":") + 1));
                int startTimeHours = Integer.parseInt(startTimeStr.substring(0, startTimeStr.indexOf(":")));
                int currentDuration = Integer.parseInt(durationText.getText().toString().subSequence(0, 1).toString());
                int currentEndTime;

                //change duration by one. if it's a 3, then make a 1.
                switch (currentDuration) {
                    case 1:
                        //check if new duration is ok
                        if (isTimeOK(startTimeHours, 2))
                            currentDuration = 2;
                        else //shows an error message, and makes text red for one second.
                            showErrorMessage(getString(R.string.invalid_duration), durationText);
                        break;
                    case 2:
                        if (isTimeOK(startTimeHours, 3))
                            currentDuration = 3;
                        else
                            showErrorMessage(getString(R.string.invalid_duration), durationText);
                        break;
                    case 3:
                        currentDuration = 1;
                        break;
                }
                currentEndTime = timeAfter(startTimeHours, currentDuration);
                endTime.setText(getString(R.string.timeFormat, currentEndTime, minutes));
                durationText.setText(getString(R.string.number_hour, currentDuration));

            }
        });
        //sets onClick listener to show a dialog.
        hospitalityText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHospitalityDialog(hospitalityText);
            }
        });
        //show a dialog when startTime/endTime views are clicked.
        setListenerTimeDialog(startTime, endTime);
        //checks some fields when clicked and adds data to booking Object.
        submitButton();
    }

    private void submitButton() {
        final AppCompatTextView durationText = view.findViewById(R.id.durationText);
        final AppCompatTextView startTime = view.findViewById(R.id.startTime);
        final TextInputEditText eventDate = view.findViewById(R.id.eventDate);

        view.findViewById(R.id.eventConfirmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisBooking.setEventDuration(Integer.parseInt(durationText.getText().toString().subSequence(0, 1).toString()));
                thisBooking.setEventTime(startTime.getText().toString());
                //requireNonNull ==> Cannot be null. if null, throws NullPointerException.
                thisBooking.setEventDate(Objects.requireNonNull(eventDate.getText()).toString());

                //calculate price according to whatever data the user provided
                thisBooking.calculatePrice();
                //checks if alll fields are ok. if not, shows an error message. Also, it checks if time is valid.
                submitNow();
            }
        });
    }

    private void showErrorMessage(String message, final AppCompatTextView textView) {
        //shows an error message and makes text red for one second.
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

        textView.setTextColor(Color.RED);

        //schedule reverting text color back to white after one second.
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (view != null)
                    textView.setTextColor(Color.WHITE);
            }
        }, 1000);

    }


    //returns time after a specific duration. 24H format.
    static private int timeAfter(int start, int afterWhat) {
        if ((start == 23 && afterWhat == 1) || (start == 22 && afterWhat == 2) || (start == 21 && afterWhat == 3))
            return 0;
        return (start + afterWhat);

    }

    //checks if time of a duration is ok. should be more than or equal 12 PM, and less than 24(0 AM)
    static private boolean isTimeOK(int hours, int eventDuration) {
        return (hours + eventDuration <= 24) && (hours + eventDuration >= 12);
    }

    private void setListenerTimeDialog(final AppCompatTextView textStart, final AppCompatTextView textEnd) {
        //listens for clicks to event time texts. show timePick dialog when clicked.
        textStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickDialog(textStart, textEnd);
            }
        });
        textEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickDialog(textStart, textEnd);
            }
        });
    }

    private void setSpinnerAdapter(final AppCompatSpinner spinner, final int array) {
        //takes a spinner and an id to array to set up.

        final ArrayAdapter adapter = ArrayAdapter.createFromResource(
                view.getContext(), //context
                array, //integer id
                android.R.layout.simple_spinner_item
        );
        final String[] arrayData = getResources().getStringArray(array);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                switch (spinner.getId()) {
                    case R.id.photoSpinner:
                        thisBooking.setPhotoOptions(arrayData[pos]);
                        break;
                    case R.id.otherSpinner:
                        thisBooking.setOthers(arrayData[pos]);
                        break;
                    case R.id.inviteesSpinner:
                        thisBooking.setInviteesCount(Integer.parseInt(arrayData[pos]));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Required empty override
            }
        });
    }

    private void getData() {
        //gets data from the previous fragment and changes a few things in text so that it's appropriate.
        Bundle bundle = getArguments();
        if (bundle == null) return;
        Booking.BookingType bookingType = BookingFragmentArgs.fromBundle(getArguments()).getBookingType();

        //add bookingType to booking data.
        thisBooking.setBookingType(bookingType.toString());
        //a switch to determine where we are.
        switch (bookingType) {
            case Store:
                setTexts(R.string.storeCeremony_duration, R.string.ceremony_time, R.string.ceremony_date);
                break;
            case Wedding:
                setTexts(R.string.wedding_duration, R.string.wedding_time, R.string.wedding_date);
                break;
            case Birthday:
                setTexts(R.string.birthdayParty_duration, R.string.party_time, R.string.party_time);
                break;
            case Graduation:
                setTexts(R.string.graduationParty_duration, R.string.party_time, R.string.party_time);
                break;
            case Default:
                break;
        }
    }

    private void setTexts(int eventDuration, int eventTime, int eventDate) {
        //takes three strings ids and assigns the three text views to those.
        //event, party, wedding duration and so on.

        ((AppCompatTextView) view.findViewById(R.id.eventDurationTv)).setText(getString(eventDuration));
        ((AppCompatTextView) view.findViewById(R.id.eventTimeTv)).setText(getString(eventTime));
        ((AppCompatTextView) view.findViewById(R.id.eventDateTv)).setText(getString(eventDate));

    }


    private void showDatePickerDialog(final AppCompatEditText editText) {
        //keypad won't show when click.
        editText.setFocusableInTouchMode(false);
        Long timeNow = new Date().getTime();
        Long timeAfter5Years = timeNow + 157852800000L;
        //Min: Present time. Max: present + 5 years.
        //E.g. timeNow is 09/17/2019. timeNow + fiveYears is 09/17/2024.

        final RegisterFragment.DatePickerFragment datePickerFragment = new RegisterFragment.DatePickerFragment(
                editText, timeNow, timeAfter5Years);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert getFragmentManager() != null;
                datePickerFragment.show(getFragmentManager(), "DatePicker");

            }
        });
    }


    private void showTimePickDialog(final AppCompatTextView textViewStart, final AppCompatTextView textViewEnd) {
        assert getFragmentManager() != null;
        //keypad won't show when click.
        textViewStart.setFocusableInTouchMode(false);
        textViewEnd.setFocusableInTouchMode(false);

        //get duration from Duration Text
        AppCompatTextView durationText = view.findViewById(R.id.durationText);
        int duration = Integer.parseInt(durationText.getText().subSequence(0, 1).toString());

        //Create a Dialog Fragment to display.
        TimePickerFragment timePickerFragment = new TimePickerFragment(
                textViewStart, textViewEnd, duration);
        timePickerFragment.show(getFragmentManager(), "TimePicker");

    }


    static void viewInfoDialog(final String message, final String title, final View theView) {
        //creates a new dialog with specified message and title.
        AlertDialog.Builder builder = new AlertDialog.Builder(theView.getContext());

        builder.setTitle(title).setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();

        //Changes font for message to match the app font.
        ((AppCompatTextView) dialog.findViewById(android.R.id.message))
                .setTypeface(ResourcesCompat.getFont(theView.getContext(), R.font.titillium_regular));
    }


    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        final AppCompatTextView starTimeText; //set picked time here
        final AppCompatTextView endTimeText; //set picked time+duration here
        final int eventDuration;


        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            if (!isTimeOK(i, eventDuration)) {
                Toast.makeText(getContext(), getString(R.string.invalid_time), Toast.LENGTH_SHORT).show();
                return;
            }
            starTimeText.setText(getString(R.string.timeFormat, i, i1));

            endTimeText.setText(getString(R.string.timeFormat, timeAfter(i, eventDuration), i1));
        }

        TimePickerFragment(AppCompatTextView tv, AppCompatTextView tv2, int duration) {
            starTimeText = tv;
            endTimeText = tv2;
            eventDuration = duration;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new TimePickerDialog(getContext(), this, 12, 0, false);
        }
    }

}
