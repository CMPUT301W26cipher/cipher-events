package com.example.cipher_events.pages;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.CarouselEventAdapter;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * HomeFragment displays a list of events with filtering options and a carousel for featured events.
 */
public class HomeFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private RecyclerView carouselRecyclerView;
    private EventAdapter adapter;
    private CarouselEventAdapter carouselAdapter;
    private final ArrayList<Event> allEvents = new ArrayList<>();
    private final ArrayList<Event> displayedEvents = new ArrayList<>();
    private final ArrayList<Event> featuredEvents = new ArrayList<>();

    private MaterialButton btnToday, btnThisWeek, btnPublic, btnCapacity;
    
    private String currentFilter = "ALL";

    private final DBProxy db = DBProxy.getInstance();

    private final Handler carouselHandler = new Handler(Looper.getMainLooper());
    private Runnable carouselRunnable;
    private static final long CAROUSEL_DELAY = 2000;
    private CarouselSnapHelper snapHelper;
    private boolean isFirstLoad = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupRecyclerViews();
        setupListeners();
        loadEvents();
        setupAutoRotation();

        return view;
    }

    private void initializeViews(View view) {
        btnToday = view.findViewById(R.id.btn_filter_today);
        btnThisWeek = view.findViewById(R.id.btn_filter_this_week);
        btnPublic = view.findViewById(R.id.btn_filter_public);
        btnCapacity = view.findViewById(R.id.btn_filter_capacity);
        recyclerView = view.findViewById(R.id.recycler_events);
        carouselRecyclerView = view.findViewById(R.id.recycler_carousel);
    }

    private void setupRecyclerViews() {
        // Main List
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(displayedEvents, this::showEventDetails);
        adapter.setOnFavoriteClickListener(this::toggleFavorite);
        recyclerView.setAdapter(adapter);

        // Carousel
        carouselRecyclerView.setLayoutManager(new CarouselLayoutManager());
        carouselAdapter = new CarouselEventAdapter(featuredEvents, this::showEventDetails);
        carouselAdapter.setOnFavoriteClickListener(this::toggleFavorite);
        carouselRecyclerView.setAdapter(carouselAdapter);
        
        snapHelper = new CarouselSnapHelper();
        snapHelper.attachToRecyclerView(carouselRecyclerView);

        // Reset auto-rotate timer when user interacts with the carousel
        carouselRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    stopAutoRotation();
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    startAutoRotation();
                }
            }
        });
    }

    private void toggleFavorite(Event event) {
        User currentUser = db.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isFavorite(event.getEventID())) {
                currentUser.removeFavoriteEvent(event.getEventID());
            } else {
                currentUser.addFavoriteEvent(event.getEventID());
            }
            db.updateUser(currentUser);
            // UI will refresh via onDataChanged listener
        }
    }

    private void setupAutoRotation() {
        carouselRunnable = new Runnable() {
            @Override
            public void run() {
                if (carouselRecyclerView != null && !featuredEvents.isEmpty()) {
                    RecyclerView.LayoutManager layoutManager = carouselRecyclerView.getLayoutManager();
                    if (layoutManager != null && snapHelper != null) {
                        View snapView = snapHelper.findSnapView(layoutManager);
                        int currentItem = RecyclerView.NO_POSITION;
                        
                        if (snapView != null) {
                            currentItem = layoutManager.getPosition(snapView);
                        } else if (layoutManager.getChildCount() > 0) {
                            // Fallback to first visible child if snapView is null
                            View firstChild = layoutManager.getChildAt(0);
                            if (firstChild != null) {
                                currentItem = layoutManager.getPosition(firstChild);
                            }
                        }

                        if (currentItem != RecyclerView.NO_POSITION) {
                            carouselRecyclerView.smoothScrollToPosition(currentItem + 1);
                        }
                    }
                }
                carouselHandler.postDelayed(this, CAROUSEL_DELAY);
            }
        };
    }

    private void showEventDetails(Event event) {
        EventDetailsDialogFragment dialog = EventDetailsDialogFragment.newInstance(
                event.getEventID(),
                event.getName(),
                event.getDescription(),
                event.getTime(),
                event.getLocation(),
                event.getEntrants() != null ? event.getEntrants().size() : 0,
                new ArrayList<>() // Placeholder for tags
        );
        dialog.show(getParentFragmentManager(), "EventDetailsDialog");
    }

    private void setupListeners() {
        btnToday.setOnClickListener(v -> updateFilter("TODAY"));
        btnThisWeek.setOnClickListener(v -> updateFilter("THIS_WEEK"));
        btnPublic.setOnClickListener(v -> updateFilter("PUBLIC"));
        if (btnCapacity != null) {
            btnCapacity.setOnClickListener(v -> updateFilter("CAPACITY"));
        }
    }

    private void updateFilter(String filter) {
        currentFilter = currentFilter.equals(filter) ? "ALL" : filter;
        applyFilters();
    }

    private void loadEvents() {
        allEvents.clear();
        allEvents.addAll(db.getAllEvents());
        
        featuredEvents.clear();
        for (Event e : allEvents) {
            if (featuredEvents.size() >= 5) break;
            featuredEvents.add(e);
        }
        carouselAdapter.notifyDataSetChanged();
        
        if (isFirstLoad && !featuredEvents.isEmpty()) {
            carouselRecyclerView.scrollToPosition(carouselAdapter.getStartingPosition());
            isFirstLoad = false;
        }

        applyFilters();
    }

    private void applyFilters() {
        displayedEvents.clear();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        String todayStr = sdf.format(today);

        cal.add(Calendar.DAY_OF_YEAR, 7);
        Date nextWeek = cal.getTime();

        for (Event event : allEvents) {
            if (matchesFilter(event, todayStr, today, nextWeek)) {
                displayedEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();
        updateButtonUI();
    }

    private boolean matchesFilter(Event event, String todayStr, Date today, Date nextWeek) {
        if (currentFilter.equals("ALL")) return true;

        if (currentFilter.equals("PUBLIC")) {
            return event.isPublicEvent();
        }

        if (currentFilter.equals("CAPACITY")) {
            Integer capacity = event.getWaitingListCapacity();
            int currentEntrants = (event.getEntrants() != null) ? event.getEntrants().size() : 0;
            return (capacity == null || currentEntrants < capacity);
        }

        if (currentFilter.equals("TODAY") || currentFilter.equals("THIS_WEEK")) {
            try {
                if (event.getTime() == null || event.getTime().length() < 10) return false;
                
                String eventDateStr = event.getTime().substring(0, 10);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date eventDate = sdf.parse(eventDateStr);
                
                if (currentFilter.equals("TODAY")) {
                    return eventDateStr.equals(todayStr);
                } else {
                    return eventDate != null && !eventDate.before(today) && eventDate.before(nextWeek);
                }
            } catch (Exception e) {
                return false; 
            }
        }
        return false;
    }

    private void updateButtonUI() {
        setButtonStyle(btnToday, "TODAY".equals(currentFilter));
        setButtonStyle(btnThisWeek, "THIS_WEEK".equals(currentFilter));
        setButtonStyle(btnPublic, "PUBLIC".equals(currentFilter));
        if (btnCapacity != null) {
            setButtonStyle(btnCapacity, "CAPACITY".equals(currentFilter));
        }
    }

    private void setButtonStyle(MaterialButton button, boolean isSelected) {
        int backgroundColor = isSelected ? R.color.button_purple : R.color.input_background;
        float alpha = isSelected ? 1.0f : 0.7f;
        
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), backgroundColor)));
        button.setAlpha(alpha);
        button.setStrokeWidth(isSelected ? 0 : 2);
        button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.button_purple)));
    }

    @Override
    public void onResume() {
        super.onResume();
        db.addListener(this);
        loadEvents();
        startAutoRotation();
    }

    @Override
    public void onPause() {
        super.onPause();
        db.removeListener(this);
        stopAutoRotation();
    }

    private void startAutoRotation() {
        stopAutoRotation();
        if (carouselRunnable != null) {
            carouselHandler.postDelayed(carouselRunnable, CAROUSEL_DELAY);
        }
    }

    private void stopAutoRotation() {
        if (carouselRunnable != null) {
            carouselHandler.removeCallbacks(carouselRunnable);
        }
    }

    @Override
    public void onDataChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadEvents);
        }
    }
}
