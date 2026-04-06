package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cipher_events.R;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class EventMapFragment extends Fragment implements OnMapReadyCallback {

    private String eventId;
    private GoogleMap mMap;
    private final DBProxy db = DBProxy.getInstance();

    public static EventMapFragment newInstance(String eventId) {
        EventMapFragment fragment = new EventMapFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_map, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        ImageButton btnBack = view.findViewById(R.id.btn_back_map);
        TextView tvTitle = view.findViewById(R.id.tv_map_title);

        Event event = db.getEvent(eventId);
        if (event != null) {
            tvTitle.setText(event.getName() + " Map");
        }

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        Event event = db.getEvent(eventId);
        if (event == null) return;

        // 1. Add Event Location (Orange Marker)
        LatLng eventLoc = new LatLng(event.getLatitude(), event.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(eventLoc)
                .title("Event: " + event.getName())
                .snippet(event.getLocation())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        // 2. Add Entrant Locations (Blue Markers)
        if (event.getEntrants() != null) {
            for (User entrant : event.getEntrants()) {
                if (entrant.getLatitude() != 0.0 || entrant.getLongitude() != 0.0) {
                    LatLng entrantLoc = new LatLng(entrant.getLatitude(), entrant.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(entrantLoc)
                            .title("Entrant: " + entrant.getName())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
            }
        }

        // Center camera on event
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLoc, 10f));
    }
}