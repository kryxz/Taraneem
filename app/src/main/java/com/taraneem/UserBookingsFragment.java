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
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taraneem.data.Booking;
import com.taraneem.data.TempData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


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
        setUpAdapter(); //initialize recyclerView adapter!
        super.onViewCreated(view, savedInstanceState);
    }


    private void setUpAdapter() {
        if (TempData.getUserData() == null) { //no booking history => go back.
            Navigation.findNavController(view).navigateUp();
            return;
        }
        //define recyclerView
        RecyclerView recyclerView = view.findViewById(R.id.bookingsRV);

        //add adapter to recyclerView
        recyclerView.setAdapter(createAdapter());
        //add layoutManager to recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }


    private BookingsAdapter createAdapter() {
        //collects required data to and returns a BookingAdapter.

        //user bookings
        HashMap<String, String> map = TempData.getUserData().getBookings();

        List<String> hallNamesList = new ArrayList<>();
        List<String> datesList = new ArrayList<>();

        //the keys in the user bookings maps represent the ids of the bookings.
        List<String> idsList = new ArrayList<>(map.keySet());
        //iterate through the map to get names and dates.
        for (String string : map.values())
            hallNamesList.add(string.substring(string.indexOf("'") + 1));

        for (String string : map.values())
            datesList.add(string.substring(0, string.indexOf("'")));

        //progress bar is required in the BookingsAdapter constructor!
        ContentLoadingProgressBar progressBar = view.findViewById(R.id.infoProgressBar);
        return new BookingsAdapter(datesList, hallNamesList, idsList, getActivity(), progressBar);

    }


    static class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingsRV> {

        //lists that hold the user booking data. Date, hall name, and id.
        final private List<String> datesList;
        final private List<String> hallNamesList;
        final private List<String> idsList;


        final private Activity activity; //required to show a dialog, and to get sharedPreferences
        final private ContentLoadingProgressBar progressBar;

        //constructor
        BookingsAdapter(List<String> datesList, List<String> hallNamesList, List<String>
                idsList, Activity activity, ContentLoadingProgressBar progressBar) {
            this.datesList = datesList;
            this.hallNamesList = hallNamesList;
            this.idsList = idsList;
            this.activity = activity;
            this.progressBar = progressBar;
        }

        //display the data at the specified position.
        @Override
        public void onBindViewHolder(@NonNull final BookingsRV holder, final int position) {
            holder.textView.setText(activity.getString(R.string.bookingItemInfo, datesList.get(position), hallNamesList.get(position)));
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteEvent(Common.getEventPath(datesList.get(position),
                            idsList.get(position), hallNamesList.get(position)), position, view);
                }
            });
            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editEvent(Common.getEventPath(datesList.get(position),
                            idsList.get(position), hallNamesList.get(position)), position, view);
                }
            });

        }

        //create viewHolder to represent the items.
        @NonNull
        @Override
        public BookingsRV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BookingsRV((LinearLayoutCompat) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.booking_item, parent, false));
        }

        //edit booking data. This gets the data from the database and sends the user to another fragment!
        void editEvent(final DatabaseReference reference, final int position, final View view) {
            progressBar.setVisibility(View.VISIBLE);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //getting booking data here
                        final String eventDate = datesList.get(position);
                        final String hallName = hallNamesList.get(position);
                        final String id = idsList.get(position);
                        final String bookingType = Objects.requireNonNull(dataSnapshot.child("bookingType").getValue()).toString();
                        final String duration = Objects.requireNonNull(dataSnapshot.child("eventDuration").getValue()).toString();
                        final String time = Objects.requireNonNull(dataSnapshot.child("eventTime").getValue()).toString();
                        final String hospitality = Objects.requireNonNull(dataSnapshot.child("hospitality").getValue()).toString();
                        final String photoOptions = Objects.requireNonNull(dataSnapshot.child("photoOptions").getValue()).toString();
                        final String others = Objects.requireNonNull(dataSnapshot.child("others").getValue()).toString();
                        final String inviteesCount = Objects.requireNonNull(dataSnapshot.child("inviteesCount").getValue()).toString();
                        FirebaseDatabase.getInstance().getReference().child("Halls").child(hallName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //get hall cost
                                int cost = Integer.parseInt(Objects.requireNonNull(dataSnapshot.child("cost").getValue()).toString());

                                //create a new booking object, add the data to it.
                                Booking thisBooking = new Booking();
                                thisBooking.setId(id);
                                thisBooking.setRes(view.getContext().getResources()); //res is needed to calculate the final price!
                                thisBooking.setHospitality(hospitality);
                                thisBooking.setBookingType(bookingType);
                                thisBooking.setEventDate(eventDate);
                                thisBooking.setHallName(hallName, cost);
                                thisBooking.setEventDuration(Integer.parseInt(duration));
                                thisBooking.setEventTime(time);
                                thisBooking.setPhotoOptions(photoOptions);
                                thisBooking.setOthers(others);
                                thisBooking.setInviteesCount(Integer.parseInt(inviteesCount));
                                //set it to be the current booking object.
                                TempData.setCurrentBooking(thisBooking);
                                //go to editing page (Info Fragment, that is).
                                Navigation.findNavController(view).navigate(R.id.changeBookingInfo);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        private void updateItems(final int position, View view) {
            //notifies the adapter about the deleted item...
            hallNamesList.remove(position);
            idsList.remove(position);
            datesList.remove(position);
            TempData.removeBookingItem(idsList.get(position));
            if (datesList.isEmpty()) {
                Navigation.findNavController(view).navigateUp();
                TempData.removeBookings();
            }
            notifyDataSetChanged();
            notifyItemRangeChanged(position, datesList.size());
        }

        private void deleteEvent(final DatabaseReference reference, final int position, final View view) {
            //Dialog buttons listener
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        //yes
                        case DialogInterface.BUTTON_POSITIVE:
                            reference.removeValue();
                            String userID = activity.getSharedPreferences("userPrefs", 0).getString("userID", "");
                            FirebaseDatabase.getInstance().getReference().child("Users")
                                    .child(userID).child("bookings").child(idsList.get(position)).removeValue();
                            updateItems(position, view);
                            break;
                        //no
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

        //how many items in the recyclerView
        @Override
        public int getItemCount() {
            return idsList.size();
        }

        //View holder class to represent the items.
        static class BookingsRV extends RecyclerView.ViewHolder {

            final LinearLayoutCompat layoutView;
            final AppCompatTextView textView;
            final AppCompatButton delete;
            final AppCompatButton edit;

            BookingsRV(LinearLayoutCompat v) {
                super(v);
                //init values
                layoutView = v;
                textView = layoutView.findViewById(R.id.itemInfo);
                delete = layoutView.findViewById(R.id.deleteItemButton);
                edit = layoutView.findViewById(R.id.editItemButton);
            }
        }
    }


}
