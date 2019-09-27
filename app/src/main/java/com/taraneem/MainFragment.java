package com.taraneem;


import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;


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
        if (item.getItemId() == R.id.infoBtn)
            showInfoDialog();
        return super.onOptionsItemSelected(item);
    }


    private void showInfoDialog() {
        String message = getString(R.string.mainInfoMessage),
                title = getString(R.string.mainInfoTitle);
        Common.viewInfoDialog(message, title, view);
    }


    private void cardViewListener() {
        List<MaterialCardView> cardViews = new ArrayList<>();
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

}
