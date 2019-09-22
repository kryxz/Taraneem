package com.taraneem;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.taraneem.data.TempData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class UserBookingsFragment extends Fragment {

    private View view;

    public UserBookingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        this.view = v;
        setUpAdapter();
        super.onViewCreated(view, savedInstanceState);
    }

    private void setUpAdapter() {
        if (TempData.getUserData() == null) {
            Navigation.findNavController(view).navigateUp();
            return;
        }
        RecyclerView recyclerView = view.findViewById(R.id.bookingsRV);

        RecyclerView.Adapter adapter = createAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    private BookingsAdapter createAdapter() {
        HashMap<String, String> map = TempData.getUserData().getBookings();
        List<String> hallNamesList = new ArrayList<>();
        List<String> datesList = new ArrayList<>();

        List<String> idsList = new ArrayList<>(map.keySet());

        for (String string : map.values())
            hallNamesList.add(string.substring(string.indexOf("'") + 1));
        for (String string : map.values())
            datesList.add(string.substring(0, string.indexOf("'")));

        return new BookingsAdapter(datesList, hallNamesList, idsList, getActivity());

    }

    static class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingsRV> {

        private List<String> datesList;
        private List<String> hallNamesList;
        private List<String> idsList;
        private Activity activity;


        BookingsAdapter(List<String> datesList, List<String> hallNamesList, List<String> idsList, Activity activity) {
            this.datesList = datesList;
            this.hallNamesList = hallNamesList;
            this.idsList = idsList;
            this.activity = activity;
        }

        static class BookingsRV extends RecyclerView.ViewHolder {
            final LinearLayoutCompat layoutView;
            final AppCompatTextView textView;
            final AppCompatButton delete;

            BookingsRV(LinearLayoutCompat v) {
                super(v);
                layoutView = v;
                textView = layoutView.findViewById(R.id.itemInfo);
                delete = layoutView.findViewById(R.id.deleteItemButton);
            }
        }

        @NonNull
        @Override
        public BookingsRV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BookingsRV((LinearLayoutCompat) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.booking_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull BookingsRV holder, final int position) {
            holder.textView.setText(activity.getString(R.string.bookingItemInfo, datesList.get(position), hallNamesList.get(position)));
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteEvent(InfoFragment.getEventPath(datesList.get(position),
                            idsList.get(position), hallNamesList.get(position)), position);
                }
            });
        }


        private void deleteEvent(final DatabaseReference reference, final int position) {
            //Dialog buttons listener
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {

                        case DialogInterface.BUTTON_POSITIVE:
                            reference.removeValue();
                            String userID = activity.getSharedPreferences("userPrefs", 0).getString("userID", "");
                            FirebaseDatabase.getInstance().getReference().child("Users")
                                    .child(userID).child("bookings").child(idsList.get(position)).removeValue();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getString(R.string.cancelBooking)).setMessage(
                    activity.getString(R.string.are_sure)).setPositiveButton(activity.getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(activity.getString(R.string.no), dialogClickListener);
            AlertDialog dialog = builder.create();
            dialog.show();
            ((AppCompatTextView) dialog.findViewById(android.R.id.message)).setTypeface(ResourcesCompat.getFont(activity, R.font.titillium_regular));

        }

        @Override
        public int getItemCount() {
            return idsList.size();
        }
    }
}
