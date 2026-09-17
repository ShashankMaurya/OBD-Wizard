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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diy.obdwizard.R;
import com.diy.obdwizard.trip.TripRecord;
import com.diy.obdwizard.trip.db.TripDatabase;
import com.diy.obdwizard.ui.activities.TripSummaryActivity;
import com.diy.obdwizard.ui.adapters.TripHistoryAdapter;

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
        adapter = new TripHistoryAdapter(tripList, trip -> {
            Intent intent = new Intent(getContext(), TripSummaryActivity.class);
            intent.putExtra("TRIP_RECORD", trip);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

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
}
