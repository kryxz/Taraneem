package com.taraneem;


import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


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

    private void buttonListeners() {
        final AppCompatEditText passwordEd = view.findViewById(R.id.loginPasswordTV);
        final AppCompatEditText emailEd = view.findViewById(R.id.loginEmailTV);

        //hides and shows password on 'eye' icon click.
        RegisterFragment.passwordView(passwordEd);

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
                final ContentLoadingProgressBar progressBar = view.findViewById(R.id.loginProgressBar);
                progressBar.setVisibility(View.VISIBLE);
                view.findViewById(R.id.loginView).setVisibility(View.GONE);
                loginNow(emailEd.getText().toString(), passwordEd.getText().toString());
            }
        });
    }

    private void loginNow(String email, String password) {
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

}
