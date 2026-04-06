package com.example.cipher_events.pages;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cipher_events.R;
import com.example.cipher_events.adapters.CarouselEventAdapter;
import com.example.cipher_events.adapters.EventAdapter;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * HomeFragment displays a list of events with filtering options and a carousel for featured events.
 */
public class HomeFragment extends Fragment implements DBProxy.OnDataChangedListener {

    private RecyclerView recyclerView;
    private RecyclerView carouselRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvResetFilters;
    
    private EventAdapter adapter;
    private CarouselEventAdapter carouselAdapter;
    private final ArrayList<Event> allEvents = new ArrayList<>();
    private final ArrayList<Event> displayedEvents = new ArrayList<>();
    private final ArrayList<Event> featuredEvents = new ArrayList<>();

    private MaterialButton btnToday, btnThisWeek, btnCapacity, btnTags, btnNotifications, btnMessages, btnCalendar;

    private String currentFilter = "ALL";
    private String selectedTag = null;

    private final DBProxy db = DBProxy.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private final Handler carouselHandler = new Handler(Looper.getMainLooper());
    private Runnable carouselRunnable;
    private static final long CAROUSEL_DELAY = 3000;
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
        btnCapacity = view.findViewById(R.id.btn_filter_capacity);
        btnTags = view.findViewById(R.id.btn_filter_tags);
        btnNotifications = view.findViewById(R.id.btn_notifications);
        btnMessages = view.findViewById(R.id.btn_messages);
        btnCalendar = view.findViewById(R.id.btn_calendar);
        tvResetFilters = view.findViewById(R.id.tv_view_all);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        
        recyclerView = view.findViewById(R.id.recycler_events);
        carouselRecyclerView = view.findViewById(R.id.recycler_carousel);

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.button_purple));
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.input_background));
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
                        }

                        if (currentItem == RecyclerView.NO_POSITION && layoutManager.getChildCount() > 0) {
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

    private void startAutoRotation() {
        stopAutoRotation();
        carouselHandler.postDelayed(carouselRunnable, CAROUSEL_DELAY);
    }

    private void stopAutoRotation() {
        carouselHandler.removeCallbacks(carouselRunnable);
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

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadEvents();
            new Handler(Looper.getMainLooper()).postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1000);
        });

        btnToday.setOnClickListener(v -> updateFilter("TODAY"));
        btnThisWeek.setOnClickListener(v -> updateFilter("THIS_WEEK"));
        
        if (btnCapacity != null) {
            btnCapacity.setOnClickListener(v -> updateFilter("CAPACITY"));
        }
        
        if (btnTags != null) {
            btnTags.setOnClickListener(v -> showTagSelectionDialog());
        }

        tvResetFilters.setOnClickListener(v -> {
            currentFilter = "ALL";
            selectedTag = null;
            applyFilters();
        });

        btnNotifications.setOnClickListener(v -> navigateTo(new NotificationsFragment()));
        btnMessages.setOnClickListener(v -> navigateTo(new UserInboxFragment()));
        btnCalendar.setOnClickListener(v -> navigateTo(new CalendarFragment()));
    }

    private void navigateTo(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showTagSelectionDialog() {
        Set<String> allTagsSet = new HashSet<>();
        for (Event event : allEvents) {
            if (event.getTags() != null) {
                allTagsSet.addAll(event.getTags());
            }
        }

        List<String> sortedTags = new ArrayList<>(allTagsSet);
        Collections.sort(sortedTags);

        String[] tagsArray = new String[sortedTags.size() + 1];
        tagsArray[0] = "All Tags";
        for (int i = 0; i < sortedTags.size(); i++) {
            tagsArray[i + 1] = sortedTags.get(i);
        }

        int checkedItem = 0;
        if (selectedTag != null) {
            for (int i = 0; i < sortedTags.size(); i++) {
                if (sortedTags.get(i).equals(selectedTag)) {
                    checkedItem = i + 1;
                    break;
                }
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Filter by Tag")
                .setSingleChoiceItems(tagsArray, checkedItem, (dialog, which) -> {
                    if (which == 0) {
                        selectedTag = null;
                    } else {
                        selectedTag = tagsArray[which];
                    }
                    applyFilters();
                    dialog.dismiss();
                })
                .show();
    }

    private void updateFilter(String filter) {
        currentFilter = currentFilter.equals(filter) ? "ALL" : filter;
        applyFilters();
    }

    private void loadEvents() {
        allEvents.clear();
        for (Event event : db.getAllEvents()) {
            if (event.isPublicEvent()) {
                allEvents.add(event);
            }
        }

        featuredEvents.clear();
        List<Event> shuffled = new ArrayList<>(allEvents);
        Collections.shuffle(shuffled);
        for (Event e : shuffled) {
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

        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        String todayStr = sdf.format(today);

        cal.add(Calendar.DAY_OF_YEAR, 7);
        Date nextWeek = cal.getTime();

        for (Event event : allEvents) {
            boolean matchesTag = (selectedTag == null) || (event.getTags() != null && event.getTags().contains(selectedTag));
            if (matchesTag && matchesFilter(event, todayStr, today, nextWeek)) {
                displayedEvents.add(event);
            }
        }

        updateFilterButtonStyles();
        adapter.notifyDataSetChanged();
        
        tvResetFilters.setVisibility((!currentFilter.equals("ALL") || selectedTag != null) ? View.VISIBLE : View.GONE);
    }

    private void updateFilterButtonStyles() {
        int activeColor = ContextCompat.getColor(requireContext(), R.color.button_purple);
        int inactiveColor = Color.parseColor("#22FFFFFF");

        btnToday.setBackgroundTintList(ColorStateList.valueOf(currentFilter.equals("TODAY") ? activeColor : inactiveColor));
        btnThisWeek.setBackgroundTintList(ColorStateList.valueOf(currentFilter.equals("THIS_WEEK") ? activeColor : inactiveColor));
        btnCapacity.setBackgroundTintList(ColorStateList.valueOf(currentFilter.equals("CAPACITY") ? activeColor : inactiveColor));
        btnTags.setBackgroundTintList(ColorStateList.valueOf(selectedTag != null ? activeColor : inactiveColor));
        
        if (selectedTag != null) {
            btnTags.setText(selectedTag);
        } else {
            btnTags.setText("Tags");
        }
    }

    private boolean matchesFilter(Event event, String todayStr, Date today, Date nextWeek) {
        if (currentFilter.equals("ALL")) return true;

        if (currentFilter.equals("CAPACITY")) {
            Integer capacity = event.getWaitingListCapacity();
            int currentEntrants = (event.getEntrants() != null) ? event.getEntrants().size() : 0;
            return (capacity == null || currentEntrants < capacity);
        }

        String eventTime = event.getTime();
        if (eventTime == null || eventTime.isEmpty()) return false;

        try {
            if (currentFilter.equals("TODAY")) {
                return eventTime.startsWith(todayStr);
            } else if (currentFilter.equals("THIS_WEEK")) {
                Date eventDate = sdf.parse(eventTime.substring(0, 10));
                return eventDate != null && !eventDate.before(today) && eventDate.before(nextWeek);
            }
        } catch (Exception e) {
            return false;
        }

        return false;
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

    @Override
    public void onDataChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadEvents);
        }
    }
}