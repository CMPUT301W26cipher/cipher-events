package com.example.cipher_events.pages;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.style.LineBackgroundSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.google.android.material.button.MaterialButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private TextView tvSelectedDate;
    private TextView tvEmptyState;
    private final List<Event> filteredEvents = new ArrayList<>();
    private final DBProxy db = DBProxy.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displaySdf = new SimpleDateFormat("MMMM d", Locale.getDefault());
    
    private final Map<CalendarDay, Integer> eventCounts = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendar_view);
        recyclerView = view.findViewById(R.id.recycler_calendar_events);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        MaterialButton btnBack = view.findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(filteredEvents, this::showEventDetails);
        recyclerView.setAdapter(adapter);

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(date.getYear(), date.getMonth() - 1, date.getDay());
            updateEventsForDate(cal.getTimeInMillis());
        });

        calculateEventCounts();
        addDecorators();
        
        CalendarDay today = CalendarDay.today();
        calendarView.setSelectedDate(today);
        Calendar cal = Calendar.getInstance();
        updateEventsForDate(cal.getTimeInMillis());

        return view;
    }

    private void calculateEventCounts() {
        eventCounts.clear();
        for (Event event : db.getAllEvents()) {
            if (event.isPublicEvent() && event.getTime() != null && event.getTime().length() >= 10) {
                try {
                    String datePart = event.getTime().substring(0, 10);
                    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(parser.parse(datePart));
                    CalendarDay day = CalendarDay.from(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
                    eventCounts.put(day, eventCounts.getOrDefault(day, 0) + 1);
                } catch (Exception ignored) {}
            }
        }
    }

    private void addDecorators() {
        for (Map.Entry<CalendarDay, Integer> entry : eventCounts.entrySet()) {
            calendarView.addDecorator(new EventCountDecorator(entry.getKey(), entry.getValue()));
        }
    }

    private void updateEventsForDate(long dateMillis) {
        String filterDateStr = sdf.format(dateMillis);
        String displayDateStr = displaySdf.format(dateMillis);
        tvSelectedDate.setText("Events on " + displayDateStr);

        filteredEvents.clear();
        for (Event event : db.getAllEvents()) {
            if (event.isPublicEvent() && event.getTime() != null && event.getTime().startsWith(filterDateStr)) {
                filteredEvents.add(event);
            }
        }
        adapter.notifyDataSetChanged();

        if (filteredEvents.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEventDetails(Event event) {
        EventDetailsDialogFragment dialog = EventDetailsDialogFragment.newInstance(
                event.getEventID(),
                event.getName(),
                event.getDescription(),
                event.getTime(),
                event.getLocation(),
                event.getEntrants() != null ? event.getEntrants().size() : 0,
                event.getTags() != null ? event.getTags() : new ArrayList<>()
        );
        dialog.show(getParentFragmentManager(), "EventDetailsDialog");
    }

    private static class EventCountDecorator implements DayViewDecorator {
        private final CalendarDay day;
        private final int count;

        public EventCountDecorator(CalendarDay day, int count) {
            this.day = day;
            this.count = count;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(this.day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new BadgeSpan(count));
        }
    }

    /**
     * Custom span to draw a small badge with the event count.
     */
    private static class BadgeSpan implements LineBackgroundSpan {
        private final int count;

        public BadgeSpan(int count) {
            this.count = count;
        }

        @Override
        public void drawBackground(@NonNull Canvas canvas, @NonNull Paint paint, int left, int right, int top, int baseline, int bottom, @NonNull CharSequence text, int start, int end, int lineNumber) {
            // Save original paint state
            int oldColor = paint.getColor();
            float oldTextSize = paint.getTextSize();
            Paint.Align oldTextAlign = paint.getTextAlign();
            boolean oldAntiAlias = paint.isAntiAlias();
            
            paint.setAntiAlias(true);
            
            // Draw a small circle in the top right
            float radius = 20f; // Increased radius
            float centerX = right - radius - 2; // Offset from right
            float centerY = top + radius + 2;   // Offset from top
            
            paint.setColor(Color.parseColor("#5B5891")); // button_purple
            canvas.drawCircle(centerX, centerY, radius, paint);
            
            // Draw the count text inside the circle
            paint.setColor(Color.WHITE);
            paint.setTextSize(24f); // Increased text size
            paint.setTextAlign(Paint.Align.CENTER);
            
            // Vertically center the text in the circle
            Rect textBounds = new Rect();
            String countText = String.valueOf(count);
            paint.getTextBounds(countText, 0, countText.length(), textBounds);
            float textY = centerY + (textBounds.height() / 2f);
            
            canvas.drawText(countText, centerX, textY, paint);
            
            // Restore original paint state
            paint.setColor(oldColor);
            paint.setTextSize(oldTextSize);
            paint.setTextAlign(oldTextAlign);
            paint.setAntiAlias(oldAntiAlias);
        }
    }
}
