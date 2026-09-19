package com.diy.obdwizard.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

        long durationMs = trip.durationMs;
        long sec = (durationMs / 1000) % 60;
        long min = (durationMs / 60000) % 60;
        long hr = (durationMs / 3600000);

        String timeStr = (hr > 0)
                ? String.format(Locale.US, "%02d:%02d:%02d", hr, min, sec)
                : String.format(Locale.US, "%02d:%02d", min, sec);

        String durationSummary = (hr > 0) ? hr + "h " + min + "m" : min + " mins";
        holder.txtDuration.setText(durationSummary);
        
        holder.txtDistance.setText(String.format(Locale.US, "%.1f", trip.totalDistanceKm));

        // Use the second slot for formatted duration (previously max speed)
        holder.txtTime.setText(timeStr);

        holder.itemView.setOnClickListener(v -> listener.onTripClick(trip));
        holder.btnDelete.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                listener.onTripDeleteClick(trips.get(currentPos), currentPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public interface OnTripClickListener {
        void onTripClick(TripRecord trip);

        void onTripDeleteClick(TripRecord trip, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtDuration, txtDistance, txtTime;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtTripDate);
            txtDuration = itemView.findViewById(R.id.txtTripDuration);
            txtDistance = itemView.findViewById(R.id.txtTripDistance);
            txtTime = itemView.findViewById(R.id.txtTripTime);
            btnDelete = itemView.findViewById(R.id.btnDeleteTrip);
        }
    }
}
