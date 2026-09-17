package com.diy.obdwizard.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diy.obdwizard.R;
import com.diy.obdwizard.trip.TripRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripHistoryAdapter extends RecyclerView.Adapter<TripHistoryAdapter.ViewHolder> {

    private final List<TripRecord> trips;
    private final OnTripClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public TripHistoryAdapter(List<TripRecord> trips, OnTripClickListener listener) {
        this.trips = trips;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TripRecord trip = trips.get(position);
        holder.txtDate.setText(dateFormat.format(new Date(trip.startTimeMs)));

        long durationMs = trip.endTimeMs - trip.startTimeMs;
        long mins = durationMs / 60000;
        holder.txtDuration.setText(mins + " mins");

        holder.txtDistance.setText(String.format(Locale.US, "%.1f", trip.totalDistanceKm));
        holder.txtMaxSpeed.setText(String.valueOf(trip.maxSpeedKmh));

        holder.itemView.setOnClickListener(v -> listener.onTripClick(trip));
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public interface OnTripClickListener {
        void onTripClick(TripRecord trip);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtDuration, txtDistance, txtMaxSpeed;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtTripDate);
            txtDuration = itemView.findViewById(R.id.txtTripDuration);
            txtDistance = itemView.findViewById(R.id.txtTripDistance);
            txtMaxSpeed = itemView.findViewById(R.id.txtTripMaxSpeed);
        }
    }
}
