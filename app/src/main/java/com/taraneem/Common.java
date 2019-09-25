package com.taraneem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TaskStackBuilder;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
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
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.taraneem.data.Booking;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

//contains all static methods used in other classes...
class Common {
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


    //Others dialog, contains 3 check boxes and a button.
    static void showOthersDialog(final Booking theBooking,
                                 final AppCompatTextView textView,
                                 Fragment fragment, View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

        @SuppressLint("InflateParams")//hides ide warning
        //layout used in this dialog
        final View layout = fragment.getLayoutInflater().inflate(R.layout.others_dialog, null);

        //set view to custom layout.
        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        dialog.show();

        //declare a few check boxes and a button.
        final AppCompatCheckBox surpriseCheck = layout.findViewById(R.id.surpriseCheck);
        final AppCompatCheckBox particularEventCheck = layout.findViewById(R.id.particularEventCheck);
        final AppCompatCheckBox customCheck = layout.findViewById(R.id.customCheck);
        ///confirm button.
        final AppCompatButton confirmOthers = layout.findViewById(R.id.confirmOthers);

        //String is immutable(creates a new object when changed)
        //StringBuilder is mutable
        final StringBuilder others = new StringBuilder();
        final String userOthers = theBooking.getOthers();

        //check if field already contains a specific string. Check box accordingly.
        surpriseCheck.setChecked(userOthers.contains(view.getContext().getString(R.string.surprise)));
        particularEventCheck.setChecked(userOthers.contains(view.getContext().getString(R.string.particular_event)));
        customCheck.setChecked(userOthers.contains(view.getContext().getString(R.string.customize)));

        //confirm button listener
        confirmOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (surpriseCheck.isChecked())
                    others.append(view.getContext().getString(R.string.surprise));

                if (particularEventCheck.isChecked())
                    if (others.length() == 0)
                        others.append(view.getContext().getString(R.string.particular_event));
                    else
                        others.append(", ").append(view.getContext().getString(R.string.particular_event));
                if (customCheck.isChecked())
                    if (others.length() == 0)
                        others.append(view.getContext().getString(R.string.customize));
                    else
                        others.append(", ").append(view.getContext().getString(R.string.customize));

                if (others.length() != 0)
                    textView.setText(others.toString());
                else
                    textView.setText(view.getContext().getString(R.string.tap_to_choose));

                //add selected to booking data
                theBooking.setOthers(others.toString());
                dialog.dismiss(); //hide dialog
            }
        });
    }


    //static so we can use it in other classes.
    //this method adds an array to a spinner and listens for changes.
    static void setSpinnerAdapter(final AppCompatSpinner spinner, final AppCompatTextView priceTextView,
                                  final int array, final Booking theBooking, View view) {

        final ArrayAdapter adapter = ArrayAdapter.createFromResource(
                view.getContext(), //context
                array, //integer id
                android.R.layout.simple_spinner_item
        );
        //data to add to the argument theBooking.
        final String[] arrayData = view.getContext().getResources().getStringArray(array);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //add adapter to spinner.
        spinner.setAdapter(adapter);

        //set selected item... this is useful when opening a dialog.
        if (spinner.getId() == R.id.editPhotoSpinner || spinner.getId() == R.id.photoSpinner)
            spinner.setSelection(Arrays.asList(view.getContext().getResources()
                    .getStringArray(array)).indexOf(theBooking.getPhotoOptions()));

        if (spinner.getId() == R.id.editInviteesSpinner || spinner.getId() == R.id.inviteesSpinner)
            spinner.setSelection(Arrays.asList(view.getContext().getResources()
                    .getStringArray(array)).indexOf(String.valueOf(theBooking.getInviteesCount())));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                //switch to decide where the change happened.
                switch (spinner.getId()) {
                    case R.id.editPhotoSpinner:
                    case R.id.photoSpinner:
                        //if id is R.id.editPhotoSpinner or R.id.photoSpinner
                        //add selected item to theBooking;
                        theBooking.setPhotoOptions(arrayData[pos]);
                        break;
                    case R.id.editInviteesSpinner:
                    case R.id.inviteesSpinner:
                        theBooking.setInviteesCount(Integer.parseInt(arrayData[pos]));
                        break;
                }
                //update price whenever there is a change
                updatePriceText(priceTextView, theBooking);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Required empty override
            }
        });
    }


    static void showHospitalityDialog(final Booking theBooking,
                                      final AppCompatTextView textView, final AppCompatTextView priceTextView,
                                      Fragment fragment, View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

        @SuppressLint("InflateParams")//hides ide warning
        //layout used in this dialog
        final View layout = fragment.getLayoutInflater().inflate(R.layout.hospitality_dialog, null);


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
        final String currentHospitality = theBooking.getHospitality();

        //check if field already contains a specific string. Check box accordingly.
        waterCheck.setChecked(currentHospitality.contains(view.getContext().getString(R.string.water)));
        kCheck.setChecked(currentHospitality.contains(view.getContext().getString(R.string.knafeh)));
        pepsiCheck.setChecked(currentHospitality.contains(view.getContext().getString(R.string.pepsi)));
        cakeCheck.setChecked(currentHospitality.contains(view.getContext().getString(R.string.cake)));

        //confirm button listener
        confirmHospitality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (waterCheck.isChecked())
                    hospitality.append(view.getContext().getString(R.string.water));

                if (kCheck.isChecked())
                    if (hospitality.length() == 0)
                        hospitality.append(view.getContext().getString(R.string.knafeh));
                    else
                        hospitality.append(", ").append(view.getContext().getString(R.string.knafeh));
                if (pepsiCheck.isChecked())
                    if (hospitality.length() == 0)
                        hospitality.append(view.getContext().getString(R.string.pepsi));
                    else
                        hospitality.append(", ").append(view.getContext().getString(R.string.pepsi));

                if (cakeCheck.isChecked())
                    if (hospitality.length() == 0)
                        hospitality.append(view.getContext().getString(R.string.cake));
                    else
                        hospitality.append(", ").append(view.getContext().getString(R.string.cake));
                if (hospitality.length() != 0)
                    textView.setText(hospitality.toString());
                else
                    textView.setText(view.getContext().getString(R.string.tap_to_choose));

                //add selected to booking data
                theBooking.setHospitality(hospitality.toString());
                dialog.dismiss(); //hide dialog

                //update price!
                updatePriceText(priceTextView, theBooking);
            }
        });
    }


    static void updatePriceText(AppCompatTextView textView, Booking theBooking) {
        //updates price
        textView.setText(textView.getContext().getString(R.string.total_price, theBooking.getPrice()));
    }


    //returns time after a specific duration. 24H format.
    static int timeAfter(int start, int afterWhat) {
        if ((start == 23 && afterWhat == 1) || (start == 22 && afterWhat == 2) || (start == 21 && afterWhat == 3))
            return 0;
        return (start + afterWhat);

    }

    //checks if time of a duration is ok. should be more than or equal 12 PM, and less than 24(0 AM)
    static boolean isTimeOK(int hours, int eventDuration) {
        return (hours + eventDuration <= 24) && (hours + eventDuration >= 12);
    }

    static void hideKeyboard(View view) {
        ((InputMethodManager) Objects.requireNonNull(view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE)))
                .hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    static void setLocale(String langCode, Activity activity) {
        Configuration config = activity.getResources().getConfiguration();
        config.locale = new Locale(langCode);
        activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
    }

    static DatabaseReference getEventPath(String date, String id, String hallName) {
        String year = date.substring(0, 4);
        String month = date.substring(date.indexOf('-') + 1, date.lastIndexOf('-'));
        String day = date.substring(date.lastIndexOf('-') + 1, date.lastIndexOf('-') + 3).replace(" ", "");
        return FirebaseDatabase.getInstance().getReference().child(hallName)
                .child(year).child(month).child(day).child(id);
    }

    //shows a datePick dialog when clicked on the editText. Takes a fragmentManager to display the dialog.
    static void showDatePickerDialog(AppCompatEditText editText, final FragmentManager manager) {
        final Common.DatePickerFragment datePickerFragment = new Common.DatePickerFragment(
                //01/01/1930 -1262312624000L
                editText, -1262312624000L,
                new Date().getTime() - 568080000000L);

        editText.setFocusableInTouchMode(false);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerFragment.show(manager, "datePicker");

            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")//disables an ide warning.
    static void passwordView(final AppCompatEditText editText) {
        //hides and shows password when user clicks at the 'eye' icon.
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[0].getBounds().width())) {
                        if (editText.getTransformationMethod() == null) {
                            //Hides password.
                            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visibility_off, 0);
                            editText.setTransformationMethod(new PasswordTransformationMethod());
                        } else {
                            //Shows password
                            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visibility, 0);
                            editText.setTransformationMethod(null);
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    static void logoutNow(final Activity activity) {
        //Dialog buttons listener
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {

                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes
                        FirebaseAuth.getInstance().signOut();
                        TaskStackBuilder.create(activity)
                                .addNextIntent(new Intent(activity, LoginActivity.class))
                                .startActivities();
                        activity.finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No
                        dialog.dismiss();
                        break;
                }
            }
        };
        //defining a dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.confirm_logout)).setMessage(
                activity.getString(R.string.are_sure)).setPositiveButton(activity.getString(R.string.yes), dialogClickListener)
                .setNegativeButton(activity.getString(R.string.no), dialogClickListener);
        AlertDialog dialog = builder.create();
        dialog.show();
        ((AppCompatTextView) dialog.findViewById(android.R.id.message)).setTypeface(ResourcesCompat.getFont(activity, R.font.titillium_regular));

    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        final AppCompatTextView starTimeText; //set picked time here
        final AppCompatTextView endTimeText; //set picked time+duration here
        final int eventDuration;


        TimePickerFragment(AppCompatTextView tv, AppCompatTextView tv2, int duration) {
            starTimeText = tv;
            endTimeText = tv2;
            eventDuration = duration;
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            if (!isTimeOK(i, eventDuration)) {
                Toast.makeText(getContext(), getString(R.string.invalid_time), Toast.LENGTH_SHORT).show();
                return;
            }
            starTimeText.setText(getString(R.string.timeFormat, i, i1));

            endTimeText.setText(getString(R.string.timeFormat, timeAfter(i, eventDuration), i1));
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new TimePickerDialog(getContext(), this, 12, 0, false);
        }
    }

    //Public and static: Android requires the class to be so.
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {


        final AppCompatEditText theEditText; //set picked date in this
        final private Long minDate; // minimum date that can be chosen by user
        final private Long maxDate; // maximum date that can be chosen by user


        //Constructor
        DatePickerFragment(AppCompatEditText editText, Long minimumDate, Long maximumDate) {

            theEditText = editText;
            minDate = minimumDate;
            maxDate = maximumDate;
        }


        @NonNull
        @Override
        //Creates a dialog, customizes a few things and returns it
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use date(maxDate) as the default date in the picker
            // by making a Calendar instance and setting time to maxDate.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(minDate);

            //Getting fields from calendar instance.
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DATE);

            // Create a new instance of DatePickerDialog and return it
            assert getContext() != null;
            //instantiating a Date picker dialog
            DatePickerDialog dialog = new DatePickerDialog(getContext(), this, year, month, day);

            //Setting max and min date.
            dialog.getDatePicker().setMinDate(minDate);
            dialog.getDatePicker().setMaxDate(maxDate);

            return dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            //set new date to the EditText we got from the Constructor.
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DATE, day);
            String dayString = Objects.requireNonNull(getContext()).getResources().getStringArray(R.array.daysOfWeek)[calendar.get(Calendar.DAY_OF_WEEK) - 1];
            //Since January is 0. December is 11. So we should increment month by 1.
            String dateString = year + "-" + (month + 1) + "-" + day + " " + dayString;
            theEditText.setText(dateString);

        }


    }

}
