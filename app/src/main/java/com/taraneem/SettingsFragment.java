package com.taraneem;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;


public class SettingsFragment extends Fragment {

    private View view;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        setUpSettingsList();
        super.onViewCreated(view, savedInstanceState);
    }

    private void setUpSettingsList() {
        RecyclerView recyclerView = view.findViewById(R.id.settingsRV);
        List<SettingsAdapter.SettingsItem> settingsItems = new ArrayList<>();
        settingsItems.add(new SettingsAdapter.SettingsItem(SettingsAdapter.Option.Profile, R.drawable.ic_person, getString(R.string.profile)));
        settingsItems.add(new SettingsAdapter.SettingsItem(SettingsAdapter.Option.Bookings, R.drawable.ic_book, getString(R.string.bookings)));
        settingsItems.add(new SettingsAdapter.SettingsItem(SettingsAdapter.Option.Language, R.drawable.ic_language, getString(R.string.changeLanguage)));
        settingsItems.add(new SettingsAdapter.SettingsItem(SettingsAdapter.Option.Logout, R.drawable.ic_exit_color, getString(R.string.logOut)));
        settingsItems.add(new SettingsAdapter.SettingsItem(SettingsAdapter.Option.About, R.drawable.ic_info_color, getString(R.string.about)));

        RecyclerView.Adapter adapter = new SettingsAdapter(settingsItems, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }


    static class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsRV> {

        final private List<SettingsItem> list;
        final private Activity activity;

        static class SettingsRV extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            final LinearLayoutCompat layoutView;
            final AppCompatTextView textView;

            SettingsRV(LinearLayoutCompat v) {
                super(v);
                layoutView = v;
                textView = layoutView.findViewById(R.id.itemTextView);
            }
        }

        static class SettingsItem {
            final private int icon;
            final private String title;
            final private Option option;

            SettingsItem(Option whichOption, int theIcon, String text) {
                option = whichOption;
                icon = theIcon;
                title = text;
            }


        }

        SettingsAdapter(List<SettingsItem> stringList, Activity theActivity) {
            list = stringList;
            activity = theActivity;
        }

        @NonNull
        @Override
        public SettingsRV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayoutCompat v = (LinearLayoutCompat) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.settings_item, parent, false);

            return new SettingsRV(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SettingsRV holder, final int position) {
            holder.textView.setText(list.get(position).title);
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, list.get(position).icon, 0);
            holder.layoutView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (list.get(position).option) {
                        case Logout:
                            logoutNow(activity);
                            break;

                        case Bookings:
                            viewBookings(view);
                            break;

                        case Profile:
                            goToProfile(view);
                            break;

                        case PrivacyPolicy:
                            break;
                        case About:
                            aboutApp(view);
                            break;
                        case Language:
                            changeLanguage(view);
                            break;

                    }
                }
            });
        }

        private void changeLanguage(final View view) {

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
                    activity.getString(R.string.confirm_logout_message)).setPositiveButton(activity.getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(activity.getString(R.string.no), dialogClickListener);
            AlertDialog dialog = builder.create();
            dialog.show();
            ((AppCompatTextView) dialog.findViewById(android.R.id.message)).setTypeface(ResourcesCompat.getFont(activity, R.font.titillium_regular));

        }

        private String getString(int id) {
            return activity.getString(id);
        }

        //TODO implement settings.

        //goto Bookings fragment.
        void viewBookings(View view) {
            Navigation.findNavController(view).navigate(R.id.profileFragment);

        }

        //Goto Profile fragment.
        void goToProfile(View view) {
            Navigation.findNavController(view).navigate(R.id.profileFragment);
        }

        //show some info
        void aboutApp(View view) {
            BookingFragment.viewInfoDialog(getString(R.string.aboutApp), getString(R.string.about), view);
        }


        @Override
        public int getItemCount() {
            return list.size();
        }

        enum Option {
            Logout, Bookings, Profile, PrivacyPolicy, About, Language
        }

    }

}

