package com.taraneem;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.taraneem.data.User;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public class RegisterFragment extends Fragment {
    private View view;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //view is fully created here. So, initialize everything.
        this.view = view;
        backToLogin();
        buttonsListeners();
        registerButton();
        super.onViewCreated(view, savedInstanceState);
    }

    private void backToLogin() {
        //two buttons listen to onclick. Goes back to login page when clicked.
        Set<AppCompatTextView> viewsSet = new HashSet<>();
        viewsSet.add((AppCompatTextView) (view.findViewById(R.id.loginTextView)));
        viewsSet.add((AppCompatTextView) (view.findViewById(R.id.haveAccount)));

        for (View textView : viewsSet)
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Navigation.findNavController(view).navigateUp();
                }
            });
    }


    private void registerButton() {
        final AppCompatButton registerBtn = view.findViewById(R.id.registerNow);
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        final FirebaseAuth auth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View thisView) {
                final User user = userData();
                if (user == null)
                    return;
                registerBtn.setEnabled(false);
                final ContentLoadingProgressBar progressBar = view.findViewById(R.id.regProgressBar);
                AppCompatEditText passwordEd = view.findViewById(R.id.registerPasswordEd);
                progressBar.setVisibility(View.VISIBLE);
                view.findViewById(R.id.registerView).setVisibility(View.GONE);

                final String password = Objects.requireNonNull(passwordEd.getText()).toString();

                auth.createUserWithEmailAndPassword(user.getEmail(),
                        password);
                String id = UUID.randomUUID().toString().substring(0, 10);
                SharedPreferences.Editor editor = view.getContext().getSharedPreferences("userPrefs", 0).edit();
                editor.putString("userID", id);
                editor.apply();
                ref.child(id)
                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            loginNow(user.getEmail(), password);
                    }
                });

            }
        });

    }

    @Nullable
    private User userData() {
        //User Object that we will return
        User user = new User();


        //Defining Edit Texts...
        AppCompatEditText nameED = view.findViewById(R.id.registerNameEd);
        AppCompatEditText birthDateEd = view.findViewById(R.id.registerBirthDateEd);

        AppCompatEditText emailEd = view.findViewById(R.id.registerEmailEd);
        AppCompatEditText phoneEd = view.findViewById(R.id.registerPhoneEd);

        AppCompatEditText passwordEd = view.findViewById(R.id.registerPasswordEd);
        AppCompatEditText passwordConfirmEd = view.findViewById(R.id.registerPasswordConfirmEd);
        AppCompatRadioButton male = view.findViewById(R.id.maleRadioBtn);
        AppCompatRadioButton female = view.findViewById(R.id.femaleRadioBtn);


        //String variables from EditTexts.
        String name = Objects.requireNonNull(nameED.getText()).toString();
        String birthday = Objects.requireNonNull(birthDateEd.getText()).toString();

        String email = Objects.requireNonNull(emailEd.getText()).toString();
        String phone = Objects.requireNonNull(phoneEd.getText()).toString();

        String password = Objects.requireNonNull(passwordEd.getText()).toString();
        String passwordConfirm = Objects.requireNonNull(passwordConfirmEd.getText()).toString();


        //Checking if fields are ok
        if (name.isEmpty()) {
            nameED.setError("Invalid name");
            return null;
        }

        if (birthday.isEmpty()) {
            birthDateEd.setError("Invalid date");
            return null;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEd.setError("Invalid Email");
            return null;
        }

        if (phone.isEmpty() || !Patterns.PHONE.matcher(phone).matches()) {
            phoneEd.setError("Invalid Phone Number");
            return null;
        }
        if (!male.isChecked() && !female.isChecked()) {
            Toast.makeText(view.getContext(), getString(R.string.choose_gender), Toast.LENGTH_SHORT).show();
            return null;
        }
        if (password.isEmpty() ||
                password.length() < 6) {
            passwordEd.setError("Password too short");
            return null;
        }

        if (passwordConfirm.isEmpty()) {
            passwordConfirmEd.setError("Confirm password");
            return null;
        }

        if (!password.equals(passwordConfirm)) {
            passwordEd.setError("Passwords don't match");
            passwordConfirmEd.setError("Passwords don't match");
            return null;
        }


        user.setName(name);
        user.setDob(birthday);
        user.setEmail(email);
        user.setPhoneNo(phone);
        user.setGender(male.isChecked());

        return user;
    }


    private void loginNow(String email, String password) {
        MainFragment.hideKeyboard(view);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                    TaskStackBuilder.create(getContext())
                            .addNextIntent(new Intent(getActivity(), MainActivity.class))
                            .startActivities();

            }
        });
    }


    private void buttonsListeners() {

        AppCompatEditText birthDateEd = view.findViewById(R.id.registerBirthDateEd);
        //keypad won't show.

        showDatePickerDialog(birthDateEd, getFragmentManager());
        //hides and shows password when user clicks the 'eye' icon.
        passwordView((AppCompatEditText) view.findViewById(R.id.registerPasswordEd));
        passwordView((AppCompatEditText) view.findViewById(R.id.registerPasswordConfirmEd));
    }


    //shows a datePick dialog when clicked on the editText. Takes a fragmentManager to display the dialog.
    static void showDatePickerDialog(AppCompatEditText editText, final FragmentManager manager) {
        final RegisterFragment.DatePickerFragment datePickerFragment = new RegisterFragment.DatePickerFragment(
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


}
