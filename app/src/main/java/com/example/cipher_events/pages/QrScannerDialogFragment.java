package com.example.cipher_events.pages;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cipher_events.R;
import com.example.cipher_events.organizer.EventQrCodeGenerator;
import com.google.zxing.WriterException;

public class QrScannerDialogFragment extends DialogFragment {

    public static QrScannerDialogFragment newInstance(String eventId) {
        QrScannerDialogFragment fragment = new QrScannerDialogFragment();
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

        View view = inflater.inflate(R.layout.dialog_qr_scanner, container, false);

        ImageView qrImageView = view.findViewById(R.id.qr_image);
        Button scanButton = view.findViewById(R.id.btn_scan);

        String eventId = null;
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            if (eventId != null) {
                try {
                    String payload = EventQrCodeGenerator.buildPayload(eventId);
                    Bitmap bitmap = EventQrCodeGenerator.generateQrBitmap(payload, 500, 500);
                    qrImageView.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error generating QR code", Toast.LENGTH_SHORT).show();
                }
            }
        }

        final String finalEventId = eventId;
        scanButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Scanning QR Code...", Toast.LENGTH_SHORT).show();
            dismiss();
            if (finalEventId != null) {
                ScannedEventDetailsDialogFragment scannedDialog = ScannedEventDetailsDialogFragment.newInstance(finalEventId);
                scannedDialog.show(getParentFragmentManager(), "ScannedEventDetailsDialog");
            }
        });

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
