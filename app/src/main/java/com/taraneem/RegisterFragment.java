package com.taraneem;


import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.taraneem.data.User;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.taraneem.Common.passwordView;
import static com.taraneem.Common.showDatePickerDialog;


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
                        password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String id = UUID.randomUUID().toString().substring(0, 10);
                            user.setId(id);
                            SharedPreferences.Editor editor = view.getContext().getSharedPreferences("userPrefs", 0).edit();
                            editor.putString("userID", id);
                            editor.apply();
                            sendData(ref, user, password);

                        } else if (Objects.requireNonNull(task.getException()).toString().contains("badly formatted")) {
                            //user entered an invalid email. tara@t.t is invalid, for example!
                            progressBar.setVisibility(View.GONE);
                            view.findViewById(R.id.registerView).setVisibility(View.VISIBLE);
                            Toast.makeText(view.getContext(), getString(R.string.invalidEmail), Toast.LENGTH_SHORT).show();
                            ((TextInputEditText) view.findViewById(R.id.registerEmailEd)).setError(getString(R.string.invalidEmail));
                            registerBtn.setEnabled(true);
                        }

                    }
                });


            }
        });

    }


    private void sendData(final DatabaseReference ref, final User user, final String password) {
        Common.hideKeyboard(view);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(user.getEmail(),
                password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    ref.child(user.getId())
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                TaskStackBuilder.create(getContext())
                                        .addNextIntent(new Intent(getActivity(), MainActivity.class))
                                        .startActivities();
                        }
                    });
                }
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
            nameED.setError(getString(R.string.invalidName));
            return null;
        }

        if (birthday.isEmpty()) {
            birthDateEd.setError(getString(R.string.invalidDate));
            return null;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEd.setError(getString(R.string.invalidEmail));
            return null;
        }

        if (phone.isEmpty() || !Patterns.PHONE.matcher(phone).matches()
                || phone.length() != 10) {
            phoneEd.setError(getString(R.string.invalidNumber));
            return null;
        }
        if (!male.isChecked() && !female.isChecked()) {
            Toast.makeText(view.getContext(), getString(R.string.choose_gender), Toast.LENGTH_SHORT).show();
            return null;
        }
        if (password.isEmpty() ||
                password.length() < 6) {
            passwordEd.setError(getString(R.string.shortPassword));
            return null;
        }

        if (passwordConfirm.isEmpty()) {
            passwordConfirmEd.setError(getString(R.string.confirm_password));
            return null;
        }

        if (!password.equals(passwordConfirm)) {
            passwordEd.setError(getString(R.string.dontMatchPassword));
            passwordConfirmEd.setError(getString(R.string.dontMatchPassword));
            return null;
        }


        user.setName(name);
        user.setDob(birthday);
        user.setEmail(email);
        user.setPhoneNo(phone);
        user.setGender(male.isChecked());

        return user;
    }


    private void buttonsListeners() {

        AppCompatEditText birthDateEd = view.findViewById(R.id.registerBirthDateEd);
        //keypad won't show.

        showDatePickerDialog(birthDateEd, getFragmentManager());
        //hides and shows password when user clicks the 'eye' icon.
        passwordView((AppCompatEditText) view.findViewById(R.id.registerPasswordEd));
        passwordView((AppCompatEditText) view.findViewById(R.id.registerPasswordConfirmEd));
    }




}
