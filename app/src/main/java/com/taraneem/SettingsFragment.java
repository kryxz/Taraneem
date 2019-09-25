package com.taraneem;


import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.taraneem.data.TempData;

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
            String langCode = activity.getSharedPreferences("userPrefs", 0).getString("langPref", "");
            //icon position depends on language
            if (langCode.equals("ar"))
                holder.textView.setCompoundDrawablesWithIntrinsicBounds(list.get(position).icon, 0, 0, 0);
            else
                holder.textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, list.get(position).icon, 0);

            holder.layoutView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (list.get(position).option) {
                        case Logout:
                            Common.logoutNow(activity);
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
                            changeLanguage();
                            break;

                    }
                }
            });
        }

        private void changeLanguage() {
            String langCode = activity.getSharedPreferences("userPrefs", 0).getString("langPref", "");
            if (langCode.equals("ar"))
                langCode = "en";
            else
                langCode = "ar";
            final SharedPreferences.Editor editor = activity.getSharedPreferences("userPrefs", 0).edit();
            editor.putString("langPref", langCode);
            editor.apply();
            TaskStackBuilder.create(activity)
                    .addNextIntent(new Intent(activity, MainActivity.class))
                    .startActivities();
            activity.finish();
        }

        private String getString(int id) {
            return activity.getString(id);
        }

        //goto Bookings fragment if user has made any bookings.
        void viewBookings(View view) {
            if (TempData.getUserData() == null)
                Toast.makeText(view.getContext(), getString(R.string.cannotLoadData), Toast.LENGTH_SHORT).show();
            else if (TempData.getUserData().getBookings().isEmpty())
                Toast.makeText(view.getContext(), getString(R.string.noBookingsYet), Toast.LENGTH_SHORT).show();
            else
                Navigation.findNavController(view).navigate(R.id.userBookingsFragment);
        }

        //Goto Profile fragment.
        void goToProfile(View view) {
            Navigation.findNavController(view).navigate(R.id.profileFragment);
        }

        //show some info
        void aboutApp(View view) {
            Common.viewInfoDialog(getString(R.string.aboutApp), getString(R.string.about), view);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


        enum Option {
            Logout, Bookings, Profile, PrivacyPolicy, About, Language
        }

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

    }

}

