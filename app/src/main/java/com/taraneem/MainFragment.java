package com.taraneem;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.taraneem.data.Booking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


public class MainFragment extends Fragment {
    private View view;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        cardViewListener();
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.notificationBtn)
            Log.i("notification", dateToString(new Date().getTime()));
        return super.onOptionsItemSelected(item);
    }

    private void cardViewListener() {
        Set<MaterialCardView> cardViews = new HashSet<>();
        cardViews.add((MaterialCardView) (view.findViewById(R.id.weddingCv)));
        cardViews.add((MaterialCardView) (view.findViewById(R.id.birthdayCv)));
        cardViews.add((MaterialCardView) (view.findViewById(R.id.graduationCv)));
        cardViews.add((MaterialCardView) (view.findViewById(R.id.storeCv)));

        for (MaterialCardView cardView : cardViews) {
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View thisView) {
                    Booking.BookingType bookingType = Booking.BookingType.Default;

                    switch (thisView.getId()) {
                        case R.id.weddingCv:
                            bookingType = Booking.BookingType.Wedding;
                            break;
                        case R.id.birthdayCv:
                            bookingType = Booking.BookingType.Birthday;
                            break;
                        case R.id.graduationCv:
                            bookingType = Booking.BookingType.Graduation;
                            break;
                        case R.id.storeCv:
                            bookingType = Booking.BookingType.Store;
                            break;
                    }
                    Navigation.findNavController(thisView).navigate(MainFragmentDirections.mainToBooking().setBookingType(bookingType));
                }
            });
        }
    }

    private String dateToString(Long milliseconds) {
        // returns date as a string from a time, formatted as: 2019/09/17-hour24;
        return new SimpleDateFormat("yyyy/MM/dd-HH", Locale.US).format(milliseconds);
    }

}
