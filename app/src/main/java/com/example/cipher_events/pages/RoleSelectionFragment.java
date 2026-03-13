package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cipher_events.MainActivity;
import com.example.cipher_events.R;

public class RoleSelectionFragment extends Fragment {

    public RoleSelectionFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_role_selection, container, false);

        Button btnEntrant = view.findViewById(R.id.btn_entrant);
        Button btnOrganizer = view.findViewById(R.id.btn_organizer);
        Button btnAdmin = view.findViewById(R.id.btn_admin);

        btnEntrant.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onRoleSelected("ENTRANT");
            }
        });

        btnOrganizer.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onRoleSelected("ORGANIZER");
            }
        });

        btnAdmin.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onRoleSelected("ADMIN");
            }
        });

        return view;
    }
}