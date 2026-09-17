package com.diy.obdwizard.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diy.obdwizard.R;
import com.diy.obdwizard.trip.TripRecord;
import com.diy.obdwizard.trip.db.TripDatabase;
import com.diy.obdwizard.ui.activities.TripSummaryActivity;
import com.diy.obdwizard.ui.adapters.TripHistoryAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TripHistoryFragment extends Fragment {

    private final List<TripRecord> tripList = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private RecyclerView recyclerView;
    private TripHistoryAdapter adapter;
    private TextView txtEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerTripHistory);
        txtEmpty = view.findViewById(R.id.txtEmptyHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TripHistoryAdapter(tripList, new TripHistoryAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(TripRecord trip) {
                Intent intent = new Intent(getContext(), TripSummaryActivity.class);
                intent.putExtra("TRIP_RECORD", trip);
                startActivity(intent);
            }

            @Override
            public void onTripDeleteClick(TripRecord trip, int position) {
                deleteTrip(trip, position);
            }
        });
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    deleteTrip(tripList.get(position), position);
                }
            }
        }).attachToRecyclerView(recyclerView);

        loadTrips();

        return view;
    }

    private void loadTrips() {
        executor.execute(() -> {
            List<TripRecord> trips = TripDatabase.getDatabase(getContext()).tripDao().getAllTrips();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tripList.clear();
                    tripList.addAll(trips);
                    adapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(tripList.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }
        });
    }

    private void deleteTrip(TripRecord trip, int position) {
        // Remove from list immediately for better UX
        tripList.remove(position);
        adapter.notifyItemRemoved(position);
        txtEmpty.setVisibility(tripList.isEmpty() ? View.VISIBLE : View.GONE);

        Snackbar snackbar = Snackbar.make(recyclerView, "Trip deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", v -> {
            // Restore trip
            tripList.add(position, trip);
            adapter.notifyItemInserted(position);
            txtEmpty.setVisibility(View.GONE);
            recyclerView.scrollToPosition(position);
        });

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                    // Permanently delete from DB
                    executor.execute(() -> TripDatabase.getDatabase(getContext()).tripDao().delete(trip));
                }
            }
        });

        snackbar.show();
    }
}
