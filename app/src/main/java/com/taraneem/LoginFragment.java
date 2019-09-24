package com.taraneem;


import android.app.Activity;
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
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


public class LoginFragment extends Fragment {
    //a view variable we can use instead of using getView()
    private View view;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //view is fully created here. So, initialize everything.
        this.view = view;
        chooseLanguage(); //asks user to choose a language if this is the first time they use the app.
        goToRegister();
        buttonListeners();
        super.onViewCreated(view, savedInstanceState);
    }


    private void goToRegister() {
        //Goes to register page onclick.
        view.findViewById(R.id.registerTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.registerFragment);
            }
        });
    }


    private void chooseLanguage() {
        String langPref = view.getContext().getSharedPreferences("userPrefs", 0).getString("langPref", "");
        if (!langPref.isEmpty()) return;
        view.findViewById(R.id.languageView).setVisibility(View.VISIBLE);
        view.findViewById(R.id.loginView).setVisibility(View.GONE);

        View[] views = new View[]{view.findViewById(R.id.englishLang), view.findViewById(R.id.arabicLang)};

        final SharedPreferences.Editor editor = view.getContext().getSharedPreferences("userPrefs", 0).edit();
        for (final View button : views) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (button.getId()) {
                        case R.id.englishLang:
                            editor.putString("langPref", "en");
                            break;
                        case R.id.arabicLang:
                            editor.putString("langPref", "ar");
                            break;
                    }
                    editor.apply();
                    Activity activity = getActivity();
                    TaskStackBuilder.create(activity)
                            .addNextIntent(new Intent(activity, LoginActivity.class))
                            .startActivities();
                    assert activity != null;
                    activity.finish();
                }
            });
        }

    }



    private void buttonListeners() {
        final AppCompatEditText passwordEd = view.findViewById(R.id.loginPasswordTV);
        final AppCompatEditText emailEd = view.findViewById(R.id.loginEmailTV);

        //hides and shows password on 'eye' icon click.
        Common.passwordView(passwordEd);

        //login button click listener.
        view.findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View thisView) {
                //makes sure fields are correct!
                if (passwordEd.getText() == null || emailEd.getText() == null)
                    return;

                if (passwordEd.getText().length() < 6) {
                    passwordEd.setError("Password too short");
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(emailEd.getText().toString()).matches()) {
                    emailEd.setError("Invalid Email");
                    return;
                }
                showHideProgressBar(true);
                loginNow(emailEd.getText().toString(), passwordEd.getText().toString());
            }
        });
    }


    private void showHideProgressBar(boolean shouldShow) {
        if (shouldShow) {
            view.findViewById(R.id.loginProgressBar).setVisibility(View.VISIBLE);
            view.findViewById(R.id.loginView).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.loginProgressBar).setVisibility(View.GONE);
            view.findViewById(R.id.loginView).setVisibility(View.VISIBLE);
        }

    }


    private void loginNow(String email, String password) {
        Common.hideKeyboard(view);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                    TaskStackBuilder.create(getContext())
                            .addNextIntent(new Intent(getActivity(), MainActivity.class))
                            .startActivities();
                else if (Objects.requireNonNull(task.getException()).toString().contains("no user")) {
                    Toast.makeText(getContext(), getString(R.string.noAccount), Toast.LENGTH_SHORT).show();
                    showHideProgressBar(false);
                } else if (Objects.requireNonNull(task.getException()).toString().contains("password is invalid")) {
                    Toast.makeText(getContext(), getString(R.string.wrongPassword), Toast.LENGTH_SHORT).show();
                    showHideProgressBar(false);
                }
            }
        });
    }

}
