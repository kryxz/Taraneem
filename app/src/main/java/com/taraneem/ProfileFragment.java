package com.taraneem;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;
import com.taraneem.data.TempData;
import com.taraneem.data.User;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class ProfileFragment extends Fragment {

    private User user;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        user = new User();
        setFields(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void setFields(final View view) {
        if (TempData.getUserData() != null)
            user = TempData.getUserData();
        else {
            Toast.makeText(view.getContext(), getString(R.string.cannotLoadData), Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }
        ((AppCompatTextView) (view.findViewById(R.id.userInfo))).setText(getString(R.string.profileData,
                user.getName(), user.getDob(), user.getPhoneNo(), user.getEmail()));

        view.findViewById(R.id.editProfileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });
        view.findViewById(R.id.logoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsFragment.SettingsAdapter.logoutNow(getActivity());
            }
        });
    }


    private void showEditDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        @SuppressLint("InflateParams")//hides ide warning
        final View layout = getLayoutInflater().inflate(R.layout.edit_profile_dialog, null);

        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        dialog.show();

        final TextInputEditText nameEd = layout.findViewById(R.id.editName);
        final TextInputEditText phoneEd = layout.findViewById(R.id.editPhone);
        final AppCompatTextView emailEd = layout.findViewById(R.id.editEmail);
        final AppCompatEditText bodEd = layout.findViewById(R.id.editBod);

        RegisterFragment.showDatePickerDialog(bodEd, getFragmentManager());

        nameEd.setText(user.getName());
        bodEd.setText(user.getDob());
        emailEd.setText(user.getEmail());
        phoneEd.setText(user.getPhoneNo());

        Set<View> viewSet = new HashSet<>();
        viewSet.add(layout.findViewById(R.id.cancelEdit));
        viewSet.add(layout.findViewById(R.id.confirmEdit));

        for (View button : viewSet)
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {

                        case R.id.cancelEdit:
                            dialog.dismiss();
                            break;

                        case R.id.confirmEdit:
                            String name = Objects.requireNonNull(nameEd.getText()).toString();
                            String birthday = Objects.requireNonNull(phoneEd.getText()).toString();

                            String phone = Objects.requireNonNull(phoneEd.getText()).toString();
                            if (name.isEmpty()) {
                                nameEd.setError("Invalid name");
                                return;
                            }

                            if (birthday.isEmpty()) {
                                bodEd.setError("Invalid date");
                                return;
                            }

                            if (phone.isEmpty() || !Patterns.PHONE.matcher(phone).matches()) {
                                phoneEd.setError("Invalid Phone Number");
                                return;
                            }
                            user.setName(name);
                            user.setPhoneNo(phone);
                            user.setDob(birthday);
                            updateData();
                            dialog.dismiss();
                            break;
                    }
                }
            });
    }

    private void updateData() {
        String userID = Objects.requireNonNull(getActivity()).getSharedPreferences("userPrefs", 0).getString("userID", "");
        FirebaseDatabase.getInstance().getReference().child("Users").child(userID).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), getString(R.string.editDone), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
