package com.example.cipher_events.pages;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cipher_events.R;

public class ScanConfirmationDialogFragment extends DialogFragment {

    public static ScanConfirmationDialogFragment newInstance(String eventId) {
        ScanConfirmationDialogFragment fragment = new ScanConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View view = inflater.inflate(R.layout.dialog_scan_confirmation, container, false);

        Button acceptButton = view.findViewById(R.id.btn_accept);
        Button declineButton = view.findViewById(R.id.btn_decline);

        String eventId = getArguments() != null ? getArguments().getString("eventId") : null;

        acceptButton.setOnClickListener(v -> {
            dismiss();
            if (eventId != null) {
                ScannedEventDetailsDialogFragment scannedDialog = ScannedEventDetailsDialogFragment.newInstance(eventId);
                scannedDialog.show(getParentFragmentManager(), "ScannedEventDetailsDialog");
            }
        });

        declineButton.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
