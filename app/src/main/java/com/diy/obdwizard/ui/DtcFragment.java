package com.diy.obdwizard.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.diy.obdwizard.MainActivity;
import com.diy.obdwizard.R;

public class DtcFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dtc, container, false);

        Button btnDtcScan = view.findViewById(R.id.btnDtcScan);
        Button btnDtcDictionary = view.findViewById(R.id.btnDtcDictionary);

        btnDtcScan.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.openDtcDialog();
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
}
