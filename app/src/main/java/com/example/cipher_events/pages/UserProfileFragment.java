package com.example.cipher_events.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.cipher_events.R;

public class UserProfileFragment extends Fragment {

    private EditText editName, editEmail, editPassword, editPhone;
    private Button saveButton, deleteButton;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        editPassword = view.findViewById(R.id.editPassword);
        editPhone = view.findViewById(R.id.editPhone);
        saveButton = view.findViewById(R.id.saveButton);
        deleteButton = view.findViewById(R.id.button2);

        saveButton.setOnClickListener(v -> {
            // Logic to save profile changes
            getParentFragmentManager().popBackStack();
        });

        deleteButton.setOnClickListener(v -> {
            // Logic to delete profile
        });
    }
}
