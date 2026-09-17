package com.diy.obdwizard.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.diy.obdwizard.MainActivity;
import com.diy.obdwizard.R;

/**
 * Fragment for triggering DTC diagnostics scan & clear operations and viewing fault code descriptions.
 */
public class DtcFragment extends Fragment {

    private Button btnDtcScan;
    private LinearLayout layoutLoading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dtc, container, false);

        btnDtcScan = view.findViewById(R.id.btnDtcScan);
        Button btnDtcDictionary = view.findViewById(R.id.btnDtcDictionary);
        layoutLoading = view.findViewById(R.id.layoutLoading);

        btnDtcScan.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                // Trigger DTC scan with onStart and onFinish loading UI callbacks
                activity.openDtcDialog(
                        this::showLoadingState,
                        this::hideLoadingState
                );
            }
        });

        btnDtcDictionary.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.showDtcDictionaryDialog();
            }
        });

        return view;
    }

    /**
     * Shows the loading spinner and disables the button during DTC query over OBD.
     */
    private void showLoadingState() {
        if (btnDtcScan != null && layoutLoading != null) {
            btnDtcScan.setEnabled(false);
            btnDtcScan.setText("");
            layoutLoading.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Restores button state after DTC query completes or fails.
     */
    private void hideLoadingState() {
        if (btnDtcScan != null && layoutLoading != null) {
            layoutLoading.setVisibility(View.GONE);
            btnDtcScan.setText("Scan & Clear DTCs");
            btnDtcScan.setEnabled(true);
        }
    }
}
