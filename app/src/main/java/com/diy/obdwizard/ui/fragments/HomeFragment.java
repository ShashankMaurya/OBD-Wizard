package com.diy.obdwizard.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.diy.obdwizard.MainActivity;
import com.diy.obdwizard.R;

public class HomeFragment extends Fragment {

    private TextView txtStatus;
    private Button btnConnect;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        txtStatus = view.findViewById(R.id.txtStatus);
        btnConnect = view.findViewById(R.id.btnConnect);

        btnConnect.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                if (activity.getObdManager().isConnected()) {
                    activity.getObdManager().disconnect();
                } else {
                    activity.startConnection();
                }
            }
        });

        updateStatus();

        return view;
    }

    public void showConnecting() {
        if (txtStatus != null && btnConnect != null && getContext() != null) {
            txtStatus.setText(R.string.status_connecting);
            txtStatus.setTextColor(requireContext().getColor(R.color.text_gray));
            btnConnect.setEnabled(false);
        }
    }

    public void updateStatus() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null && txtStatus != null && btnConnect != null && getContext() != null) {
            if (activity.getObdManager().isConnecting()) {
                showConnecting();
                return;
            }
            btnConnect.setEnabled(true);
            boolean isConnected = activity.getObdManager().isConnected();
            if (isConnected) {
                txtStatus.setText(R.string.status_connected);
                txtStatus.setTextColor(activity.getColor(R.color.accent_green));
                btnConnect.setText(R.string.btn_disconnect);
            } else {
                txtStatus.setText(R.string.status_disconnected);
                txtStatus.setTextColor(activity.getColor(R.color.accent_red));
                btnConnect.setText(R.string.btn_connect);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatus();
    }
}
