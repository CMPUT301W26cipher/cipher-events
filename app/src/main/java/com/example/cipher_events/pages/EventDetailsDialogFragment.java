package com.example.cipher_events.pages;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cipher_events.R;

import java.util.ArrayList;

/**
 * Displays a popup upon clicking an event
 * pop up displays full event details:
 * - Event Title
 * - Number of attendees
 * - Event Description
 * - Date and Location
 * - Tags
 * - Lottery disclaimer
 */

public class EventDetailsDialogFragment extends DialogFragment {

    public static EventDetailsDialogFragment newInstance(
            String name,
            String description,
            String time,
            String location,
            int attendeeCount,
            ArrayList<String> tags
    ) {
        EventDetailsDialogFragment fragment = new EventDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("description", description);
        args.putString("time", time);
        args.putString("location", location);
        args.putInt("attendeeCount", attendeeCount);
        args.putStringArrayList("tags", tags);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Make dialog background transparent so rounded corners show
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        View view = inflater.inflate(R.layout.dialog_event_details, container, false);

        TextView title = view.findViewById(R.id.detail_title);
        TextView attendees = view.findViewById(R.id.detail_attendees);
        TextView description = view.findViewById(R.id.detail_description);
        TextView dateLocation = view.findViewById(R.id.detail_date_location);
        LinearLayout tagContainer = view.findViewById(R.id.detail_tags_container);
        TextView lotteryHeader = view.findViewById(R.id.detail_lottery_header);
        TextView lotteryText = view.findViewById(R.id.detail_lottery_text);

        Bundle args = getArguments();
        if (args != null) {
            title.setText(args.getString("name"));
            attendees.setText(args.getInt("attendeeCount") + " people attending");
            description.setText(args.getString("description"));
            dateLocation.setText(args.getString("location") + " • " + args.getString("time"));

            ArrayList<String> tags = args.getStringArrayList("tags");
            if (tags != null) {
                for (String tag : tags) {
                    TextView chip = new TextView(requireContext());
                    chip.setText(tag);
                    chip.setPadding(20, 10, 20, 10);
                    chip.setBackgroundResource(R.drawable.tag_chip_bg);
                    chip.setTextSize(14);

                    LinearLayout.LayoutParams params =
                            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(10, 10, 10, 10);
                    chip.setLayoutParams(params);

                    tagContainer.addView(chip);
                }
            }
        }

        // Lottery guidelines text
        lotteryText.setText(
                "⚠️ Disclaimer\n" +
                        "Some events use a lottery system when more people join than there are available spots.\n\n" +
                        "What this means for you:\n" +
                        "• When you join, you're entered into the lottery.\n" +
                        "• Everyone who joins before the deadline has the same chance.\n" +
                        "• Joining earlier does not increase your odds.\n" +
                        "• If you're selected, you'll receive a confirmation.\n" +
                        "• If you're not selected, you may be placed on a waitlist.\n\n" +
                        "This system helps keep things fair and avoids first‑come‑first‑served pressure."
        );


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Make dialog 90% of screen width
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.90);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}