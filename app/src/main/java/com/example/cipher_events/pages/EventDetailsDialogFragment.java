package com.example.cipher_events.pages;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cipher_events.R;
import com.example.cipher_events.adapters.EventCommentAdapter;
import com.example.cipher_events.comment.EventComment;
import com.example.cipher_events.database.DBProxy;
import com.example.cipher_events.database.Event;
import com.example.cipher_events.database.Organizer;
import com.example.cipher_events.database.User;
import com.example.cipher_events.message.MessageThread;
import com.example.cipher_events.message.MessagingService;
import com.example.cipher_events.notifications.Message;
import com.example.cipher_events.notifications.Notifier;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EventDetailsDialogFragment extends DialogFragment implements DBProxy.OnDataChangedListener {

    private static final String TAG = "EventDetailsDialog";
    private boolean isOrganizerView = false;
    private String eventId;
    private String currentDeviceID;

    private final DBProxy db = DBProxy.getInstance();
    private final Notifier notifier = Notifier.getInstance();
    private final MessagingService messagingService = new MessagingService();

    private EventCommentAdapter commentAdapter;

    private TextView title;
    private TextView attendees;
    private TextView description;
    private TextView descriptionLabel;
    private TextView detailDate;
    private TextView detailLocation;
    private ImageView btnCenterOnEvent;
    private ImageView banner;
    private ImageView favoriteButton;
    private ImageView closeButton;
    private Button closeButtonBottom;
    private Button actionButton;
    private Button notifyButton;
    private Button messageButton;
    private Button organizerMessageButton;
    private Button deleteButton;
    private Button editButton;
    private View lotteryContainer;
    private TextView lotteryText;
    private ChipGroup tagContainer;

    private View organizerContainer;
    private ImageView organizerImage;
    private TextView organizerName;

    private View organizerBadge;
    private View organizerStatsCard;
    private TextView tvStatsWaitlist;
    private TextView tvStatsCapacity;
    private View entrantActionContainer;
    private View organizerActionContainer;
    private Button organizerViewWaitlistButton;
    private MapView organizerMapView;
    private MapView entrantMapView;
    private View entrantMapContainer;

    private InterstitialAd mInterstitialAd;
    private NativeAd currentNativeAd;

    // For Image Upload in Edit Dialog
    private Uri selectedEditImageUri;
    private ImageView ivEditDialogBanner;
    private final ActivityResultLauncher<Intent> pickEditImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedEditImageUri = result.getData().getData();
                    if (ivEditDialogBanner != null) {
                        Glide.with(this).load(selectedEditImageUri).placeholder(R.drawable.gray_placeholder).into(ivEditDialogBanner);
                    }
                }
            }
    );

    public static EventDetailsDialogFragment newInstance(
            String eventId,
            String name,
            String description,
            String time,
            String location,
            int waitlistCount,
            ArrayList<String> tags
    ) {
        return newInstance(eventId, name, description, time, location, waitlistCount, tags, false, null);
    }

    public static EventDetailsDialogFragment newInstance(
            String eventId,
            String name,
            String description,
            String time,
            String location,
            int waitlistCount,
            ArrayList<String> tags,
            boolean isOrganizerView
    ) {
        return newInstance(eventId, name, description, time, location, waitlistCount, tags, isOrganizerView, null);
    }

    public static EventDetailsDialogFragment newInstance(
            String eventId,
            String name,
            String description,
            String time,
            String location,
            int waitlistCount,
            ArrayList<String> tags,
            boolean isOrganizerView,
            String currentDeviceID
    ) {
        EventDetailsDialogFragment fragment = new EventDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("name", name);
        args.putString("description", description);
        args.putString("time", time);
        args.putString("location", location);
        args.putInt("waitlistCount", waitlistCount);
        args.putStringArrayList("tags", tags);
        args.putBoolean("isOrganizerView", isOrganizerView);
        args.putString("currentDeviceID", currentDeviceID);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        View view = inflater.inflate(R.layout.dialog_event_details, container, false);

        title = view.findViewById(R.id.detail_title);
        attendees = view.findViewById(R.id.detail_attendees);
        descriptionLabel = view.findViewById(R.id.description_label);
        description = view.findViewById(R.id.detail_description);
        detailDate = view.findViewById(R.id.detail_date);
        detailLocation = view.findViewById(R.id.detail_location);
        btnCenterOnEvent = view.findViewById(R.id.btn_center_on_event);
        banner = view.findViewById(R.id.detail_banner);
        favoriteButton = view.findViewById(R.id.btn_favorite);
        closeButton = view.findViewById(R.id.btn_close);
        closeButtonBottom = view.findViewById(R.id.btn_close_bottom);
        lotteryContainer = view.findViewById(R.id.lottery_container);
        lotteryText = view.findViewById(R.id.detail_lottery_text);
        actionButton = view.findViewById(R.id.scan_button);
        notifyButton = view.findViewById(R.id.notify_button);
        messageButton = view.findViewById(R.id.message_button);
        organizerMessageButton = view.findViewById(R.id.organizer_message_button);
        deleteButton = view.findViewById(R.id.delete_event_button);
        editButton = view.findViewById(R.id.edit_event_button);
        tagContainer = view.findViewById(R.id.detail_tags_container);

        organizerContainer = view.findViewById(R.id.organizer_container);
        organizerImage = view.findViewById(R.id.organizer_image);
        organizerName = view.findViewById(R.id.organizer_name);

        organizerBadge = view.findViewById(R.id.organizer_badge);
        organizerStatsCard = view.findViewById(R.id.organizer_stats_card);
        tvStatsWaitlist = view.findViewById(R.id.tv_stats_waitlist);
        tvStatsCapacity = view.findViewById(R.id.tv_stats_capacity);
        entrantActionContainer = view.findViewById(R.id.entrant_action_container);
        organizerActionContainer = view.findViewById(R.id.organizer_action_container);
        organizerViewWaitlistButton = view.findViewById(R.id.organizer_view_waitlist_button);
        
        organizerMapView = view.findViewById(R.id.map_view);
        entrantMapView = view.findViewById(R.id.entrant_map_view);
        entrantMapContainer = view.findViewById(R.id.entrant_map_container);

        RecyclerView rvComments = view.findViewById(R.id.rv_comments);
        EditText etCommentInput = view.findViewById(R.id.et_comment_input);
        TextView tvCommentError = view.findViewById(R.id.tv_comment_error);
        Button btnPostComment = view.findViewById(R.id.btn_post_comment);

        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentAdapter = new EventCommentAdapter();
        rvComments.setAdapter(commentAdapter);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
            isOrganizerView = args.getBoolean("isOrganizerView", false);
            currentDeviceID = args.getString("currentDeviceID");
        }

        if (currentDeviceID == null && db.getCurrentUser() != null) {
            currentDeviceID = db.getCurrentUser().getDeviceID();
        }

        if (currentDeviceID == null) {
            currentDeviceID = android.provider.Settings.Secure.getString(
                    requireContext().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID
            );
        }

        boolean isAdmin = DBProxy.getInstance().getAdmin(currentDeviceID) != null;
        commentAdapter.setup(currentDeviceID, isAdmin, isOrganizerView, comment -> {
            new com.example.cipher_events.comment.EntrantCommentService()
                    .deleteComment(eventId, comment.getCommentID());
            refreshUI();
        });

        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dismiss());
        }
        if (closeButtonBottom != null) {
            closeButtonBottom.setOnClickListener(v -> dismiss());
        }

        setupViewMode();

        btnPostComment.setOnClickListener(v -> {
            String commentText = etCommentInput.getText().toString().trim();
            if (commentText.isEmpty()) {
                tvCommentError.setText("Comment cannot be empty");
                tvCommentError.setVisibility(View.VISIBLE);
            } else if (commentText.length() > 200) {
                tvCommentError.setText("Comment is too long (max 200 chars)");
                tvCommentError.setVisibility(View.VISIBLE);
            } else {
                tvCommentError.setVisibility(View.GONE);
                postComment(commentText);
                etCommentInput.setText("");
            }
        });

        etCommentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCommentError.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupMapView(organizerMapView);
        setupMapView(entrantMapView);

        if (btnCenterOnEvent != null) {
            btnCenterOnEvent.setOnClickListener(v -> centerMapOnEvent());
        }

        // Load native video ad
        refreshNativeAd(view);

        // Preload video interstitial ad
        loadInterstitialAd();

        refreshUI();

        return view;
    }

    private void refreshNativeAd(View root) {
        AdLoader adLoader = new AdLoader.Builder(requireContext(), "ca-app-pub-3940256099942544/1044960115")
                .forNativeAd(nativeAd -> {
                    if (currentNativeAd != null) {
                        currentNativeAd.destroy();
                    }
                    currentNativeAd = nativeAd;
                    FrameLayout frameLayout = root.findViewById(R.id.native_ad_container);
                    NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.ad_unified, null);
                    populateNativeAdView(nativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.e(TAG, "Native ad failed to load: " + adError.getMessage());
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .setVideoOptions(new VideoOptions.Builder()
                                .setStartMuted(true)
                                .build())
                        .build())
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        // Sample Interstitial Video Unit ID: ca-app-pub-3940256099942544/5224354917
        InterstitialAd.load(requireContext(),"ca-app-pub-3940256099942544/5224354917", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }

    private void setupMapView(MapView map) {
        if (map != null) {
            map.setMultiTouchControls(true);
            map.getController().setZoom(12.0);
            map.getController().setCenter(new GeoPoint(53.5461, -113.4938));
        }
    }

    private void centerMapOnEvent() {
        Event event = db.getEvent(eventId);
        if (event != null) {
            if (event.getLatitude() != null && event.getLongitude() != null) {
                MapView activeMap = isOrganizerView ? organizerMapView : entrantMapView;
                if (activeMap != null) {
                    activeMap.getController().animateTo(new GeoPoint(event.getLatitude(), event.getLongitude()));
                }
            } else {
                // If coordinates missing, attempt geocode once and center
                new Thread(() -> {
                    try {
                        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocationName(event.getLocation(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            double lat = addresses.get(0).getLatitude();
                            double lng = addresses.get(0).getLongitude();
                            event.setLatitude(lat);
                            event.setLongitude(lng);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    MapView activeMap = isOrganizerView ? organizerMapView : entrantMapView;
                                    if (activeMap != null) {
                                        activeMap.getController().animateTo(new GeoPoint(lat, lng));
                                        updateMapMarkers(activeMap, event);
                                    }
                                });
                            }
                        }
                    } catch (Exception ignored) {}
                }).start();
            }
        }
    }

    private void displayTags(ArrayList<String> tags) {
        if (tagContainer == null) return;
        tagContainer.removeAllViews();
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                Chip chip = new Chip(requireContext());
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.input_background);
                chip.setTextColor(Color.WHITE);
                chip.setChipStrokeWidth(0);
                tagContainer.addView(chip);
            }
            tagContainer.setVisibility(View.VISIBLE);
            if (getView() != null && getView().findViewById(R.id.detail_tags_label) != null) {
                getView().findViewById(R.id.detail_tags_label).setVisibility(View.VISIBLE);
            }
        } else {
            tagContainer.setVisibility(View.GONE);
            if (getView() != null && getView().findViewById(R.id.detail_tags_label) != null) {
                getView().findViewById(R.id.detail_tags_label).setVisibility(View.GONE);
            }
        }
    }

    private void setupViewMode() {
        if (isOrganizerView) {
            organizerBadge.setVisibility(View.VISIBLE);
            organizerStatsCard.setVisibility(View.VISIBLE);
            organizerActionContainer.setVisibility(View.VISIBLE);
            entrantActionContainer.setVisibility(View.GONE);
            entrantMapContainer.setVisibility(View.GONE);
            
            organizerViewWaitlistButton.setOnClickListener(v -> {
                dismiss();
                WaitingListFragment fragment = WaitingListFragment.newInstance(eventId, "organizer");
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });

            lotteryContainer.setVisibility(View.GONE);
            notifyButton.setOnClickListener(v -> showNotificationInputDialog());

            if (organizerMessageButton != null) {
                organizerMessageButton.setOnClickListener(v -> openOrganizerMessages());
            }

            deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
            editButton.setOnClickListener(v -> showEditEventDialog());
            
            favoriteButton.setVisibility(View.GONE);
            if (organizerContainer != null) organizerContainer.setVisibility(View.GONE);
        } else {
            organizerBadge.setVisibility(View.GONE);
            organizerStatsCard.setVisibility(View.GONE);
            organizerActionContainer.setVisibility(View.GONE);
            entrantActionContainer.setVisibility(View.VISIBLE);
            entrantMapContainer.setVisibility(View.VISIBLE);

            actionButton.setText("Join Waitlist");
            actionButton.setOnClickListener(v -> {
                if (eventId != null) {
                    showAdBeforeScan();
                } else {
                    Toast.makeText(getContext(), "Error: Event ID missing", Toast.LENGTH_SHORT).show();
                }
            });
            
            lotteryContainer.setVisibility(View.VISIBLE);
            lotteryText.setText(
                    "⚠️ Disclaimer\n" +
                            "Some events use a lottery system when more people join than there are available spots.\n\n" +
                            "What this means for you:\n" +
                            "• When you join, you're entered into the lottery.\n" +
                            "• Everyone who joins before the deadline has the same chance.\n" +
                            "• Joining earlier does not increase your odds.\n" +
                            "• If you're selected, you'll receive a confirmation.\n" +
                            "• If you're not selected, you may be placed on a waitlist.\n\n" +
                            "This system helps keep things fair and avoids first-come-first-served pressure."
            );

            if (messageButton != null) {
                messageButton.setText("Message");
                messageButton.setVisibility(View.VISIBLE);
                messageButton.setOnClickListener(v -> openEntrantChat());
            }

            favoriteButton.setVisibility(View.VISIBLE);
            if (organizerContainer != null) organizerContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showAdBeforeScan() {
        if (mInterstitialAd != null) {
            showInterstitialAd();
        } else {
            // If real ad not loaded yet, show a loading dialog and fetch it
            final AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                    .setMessage("Loading advertisement...")
                    .setCancelable(false)
                    .create();
            loadingDialog.show();

            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(requireContext(), "ca-app-pub-3940256099942544/5224354917", adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            loadingDialog.dismiss();
                            mInterstitialAd = interstitialAd;
                            showInterstitialAd();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            loadingDialog.dismiss();
                            Log.e(TAG, "Ad failed to load: " + loadAdError.getMessage());
                            // Proceed to scan if ad fails to load to not block user
                            proceedToScan();
                        }
                    });
        }
    }

    private void showInterstitialAd() {
        if (mInterstitialAd == null) {
            proceedToScan();
            return;
        }
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                mInterstitialAd = null;
                loadInterstitialAd(); // Preload next one
                proceedToScan();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                mInterstitialAd = null;
                proceedToScan();
            }
        });
        mInterstitialAd.show(requireActivity());
    }

    private void proceedToScan() {
        if (eventId != null) {
            QrScannerDialogFragment qrDialog = QrScannerDialogFragment.newInstance(eventId);
            qrDialog.show(getParentFragmentManager(), "QrScannerDialog");
        }
    }

    private void showEditEventDialog() {
        Event event = db.getEvent(eventId);
        if (event == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_event, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ivEditDialogBanner = dialogView.findViewById(R.id.iv_edit_event_banner);
        View btnClose = dialogView.findViewById(R.id.btn_edit_event_close);
        View btnChangeImage = dialogView.findViewById(R.id.btn_edit_event_change_image);
        EditText etTitle = dialogView.findViewById(R.id.et_edit_event_title);
        EditText etDescription = dialogView.findViewById(R.id.et_edit_event_description);
        EditText etLocation = dialogView.findViewById(R.id.et_edit_event_location);
        EditText etDate = dialogView.findViewById(R.id.et_edit_event_date);
        EditText etTime = dialogView.findViewById(R.id.et_edit_event_time);
        EditText etCapacity = dialogView.findViewById(R.id.et_edit_event_capacity);
        EditText etTags = dialogView.findViewById(R.id.et_edit_event_tags);
        SwitchMaterial swPublic = dialogView.findViewById(R.id.switch_edit_event_public);
        Button btnCancel = dialogView.findViewById(R.id.btn_edit_event_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_edit_event_save);

        // Set current values
        etTitle.setText(event.getName());
        etDescription.setText(event.getDescription());
        etLocation.setText(event.getLocation());
        
        String currentDateTime = event.getTime() != null ? event.getTime() : "";
        String currentDate = "";
        String currentTime = "";
        if (currentDateTime.contains(" ")) {
            String[] parts = currentDateTime.split(" ");
            if (parts.length > 0) currentDate = parts[0];
            if (parts.length > 1) currentTime = parts[1];
        } else {
            currentDate = currentDateTime;
        }
        etDate.setText(currentDate);
        etTime.setText(currentTime);
        etCapacity.setText(event.getWaitingListCapacity() != null ? String.valueOf(event.getWaitingListCapacity()) : "");
        
        if (event.getTags() != null) {
            StringBuilder tagsBuilder = new StringBuilder();
            for (int i = 0; i < event.getTags().size(); i++) {
                tagsBuilder.append(event.getTags().get(i));
                if (i < event.getTags().size() - 1) tagsBuilder.append(", ");
            }
            etTags.setText(tagsBuilder.toString());
        }
        swPublic.setChecked(event.isPublicEvent());

        // Banner loading
        String currentBannerUrl = event.getPosterPictureURL();
        if (currentBannerUrl != null && !currentBannerUrl.isEmpty()) {
            Glide.with(this).load(currentBannerUrl).placeholder(R.drawable.gray_placeholder).into(ivEditDialogBanner);
        }

        selectedEditImageUri = null;
        btnChangeImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickEditImageLauncher.launch(intent);
        });

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        etDate.setOnClickListener(v -> showDatePicker(etDate));
        etTime.setOnClickListener(v -> showTimePicker(etTime));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newTitle = etTitle.getText().toString().trim();
            String newDesc = etDescription.getText().toString().trim();
            String newLoc = etLocation.getText().toString().trim();
            String newDateStr = etDate.getText().toString().trim();
            String newTimeStr = etTime.getText().toString().trim();
            String capStr = etCapacity.getText().toString().trim();
            String tagsStr = etTags.getText().toString().trim();

            if (newTitle.isEmpty()) {
                Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            event.setName(newTitle);
            event.setDescription(newDesc);
            event.setLocation(newLoc);
            event.setTime(newDateStr + " " + newTimeStr);
            
            if (selectedEditImageUri != null) {
                event.setPosterPictureURL(selectedEditImageUri.toString());
            }
            
            if (!capStr.isEmpty()) {
                try {
                    event.setWaitingListCapacity(Integer.parseInt(capStr));
                } catch (NumberFormatException ignored) {}
            } else {
                event.setWaitingListCapacity(null);
            }

            ArrayList<String> tagsList = new ArrayList<>();
            if (!tagsStr.isEmpty()) {
                String[] parts = tagsStr.split(",");
                for (String part : parts) {
                    String tag = part.trim();
                    if (!tag.isEmpty()) tagsList.add(tag);
                }
            }
            event.setTags(tagsList);
            event.setPublicEvent(swPublic.isChecked());

            db.updateEvent(event);
            Toast.makeText(getContext(), "Event updated!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            refreshUI();
        });

        dialog.show();
    }

    private void showDatePicker(EditText etDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year1, month1, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
            etDate.setText(date);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker(EditText etTime) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minute1) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
            etTime.setText(time);
        }, hour, minute, false);
        timePickerDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Event event = db.getEvent(eventId);
                    if (event != null) {
                        db.deleteEvent(event);
                        Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openOrganizerMessages() {
        if (eventId == null || currentDeviceID == null) return;
        dismiss();
        MessageThreadsFragment fragment = MessageThreadsFragment.newInstance(eventId, currentDeviceID);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void openEntrantChat() {
        if (eventId == null || currentDeviceID == null) return;

        try {
            MessageThread thread = messagingService.openThread(eventId, currentDeviceID);
            dismiss();
            DirectChatFragment fragment = DirectChatFragment.newInstance(
                    eventId,
                    thread.getThreadID(),
                    currentDeviceID,
                    false
            );
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } catch (IllegalArgumentException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadComments() {
        Event event = db.getEvent(eventId);
        if (event != null && event.getComments() != null) {
            commentAdapter.setComments(event.getComments());
        }
    }

    private void postComment(String message) {
        User currentUser = db.getCurrentUser();
        String deviceID = currentUser != null ? currentUser.getDeviceID() : "unknown";
        String authorName = (currentUser != null) ? currentUser.getName() : "Anonymous";
        String role = isOrganizerView ? "organizer" : "entrant";
        String timestamp = new SimpleDateFormat("yyyy-MM-0dd HH:mm", Locale.getDefault())
                .format(new Date());

        EventComment newComment = new EventComment(deviceID, authorName, role, message, timestamp);
        Event event = db.getEvent(eventId);
        if (event != null) {
            event.getComments().add(newComment);
            db.updateEvent(event);
            Toast.makeText(getContext(), "Comment posted!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotificationInputDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_send_notification, null);
        
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        AutoCompleteTextView groupSpinner = dialogView.findViewById(R.id.spinner_notification_group);
        EditText titleInput = dialogView.findViewById(R.id.et_notification_title);
        EditText bodyInput = dialogView.findViewById(R.id.et_notification_body);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSend = dialogView.findViewById(R.id.btn_send);

        String[] groups = {"Invited", "Cancelled", "Enrolled"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_dropdown_simple,
                groups
        );
        groupSpinner.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String titleStr = titleInput.getText().toString().trim();
            String bodyStr = bodyInput.getText().toString().trim();
            String selectedGroup = groupSpinner.getText().toString();

            if (!titleStr.isEmpty() && !bodyStr.isEmpty()) {
                sendNotificationToGroup(titleStr, bodyStr, selectedGroup);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(),
                        "Title and message cannot be empty",
                        Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void sendNotificationToGroup(String title, String body, String group) {
        switch (group) {
            case "Invited":
                notifyInvitedEntrants(title, body);
                break;
            case "Cancelled":
                notifyCancelledEntrants(title, body);
                break;
            case "Enrolled":
                notifyEnrolledEntrants(title, body);
                break;
        }
    }

    private void notifyInvitedEntrants(String title, String body) {
        Event e = db.getEvent(eventId);
        if (e == null) return;
        Message m = new Message(title, body, e.getOrganizer());
        List<String> deviceIDs = e.getInvitedEntrants().stream()
                .map(User::getDeviceID)
                .collect(Collectors.toList());
        notifier.sendBulkMessages(deviceIDs, m);
    }

    private void notifyCancelledEntrants(String title, String body) {
        Event e = db.getEvent(eventId);
        if (e == null) return;
        Message m = new Message(title, body, e.getOrganizer());
        List<String> deviceIDs = e.getCancelledEntrants().stream()
                .map(User::getDeviceID)
                .collect(Collectors.toList());
        notifier.sendBulkMessages(deviceIDs, m);
    }

    private void notifyEnrolledEntrants(String title, String body) {
        Event e = db.getEvent(eventId);
        if (e == null) return;
        Message m = new Message(title, body, e.getOrganizer());
        List<String> deviceIDs = e.getEnrolledEntrants().stream()
                .map(User::getDeviceID)
                .collect(Collectors.toList());
        notifier.sendBulkMessages(deviceIDs, m);
    }

    private void refreshUI() {
        Event event = db.getEvent(eventId);
        if (event != null) {
            title.setText(event.getName());
            int count = (event.getEntrants() != null) ? event.getEntrants().size() : 0;
            attendees.setText(count + " people in waitlist");
            
            if (isOrganizerView) {
                tvStatsWaitlist.setText(String.valueOf(count));
                Integer cap = event.getWaitingListCapacity();
                tvStatsCapacity.setText(cap != null ? String.valueOf(cap) : "∞");
                updateMapMarkers(organizerMapView, event);
            } else {
                updateMapMarkers(entrantMapView, event);
            }
            
            String desc = event.getDescription();
            if (desc != null && !desc.isEmpty()) {
                description.setText(desc);
                descriptionLabel.setVisibility(View.VISIBLE);
                description.setVisibility(View.VISIBLE);
            } else {
                descriptionLabel.setVisibility(View.GONE);
                description.setVisibility(View.GONE);
            }
            
            if (detailDate != null) detailDate.setText(event.getTime());
            if (detailLocation != null) detailLocation.setText(event.getLocation());

            if (event.getPosterPictureURL() != null && !event.getPosterPictureURL().isEmpty()) {
                Glide.with(this).load(event.getPosterPictureURL()).placeholder(R.drawable.gray_placeholder).into(banner);
            } else {
                banner.setImageResource(R.drawable.gray_placeholder);
            }

            // Organizer UI
            String organizerId = event.getOrganizerID();
            Organizer organizer = (organizerId != null) ? db.getOrganizer(organizerId) : null;
            if (organizer != null && organizerName != null) {
                organizerName.setText(organizer.getName());
                if (organizer.getProfilePictureURL() != null && !organizer.getProfilePictureURL().isEmpty()) {
                    Glide.with(this)
                            .load(organizer.getProfilePictureURL())
                            .placeholder(R.drawable.gray_placeholder)
                            .into(organizerImage);
                } else {
                    organizerImage.setImageResource(R.drawable.gray_placeholder);
                }
            }

            // Favourite logic
            User currentUser = db.getCurrentUser();
            if (currentUser != null && favoriteButton != null && !isOrganizerView) {
                favoriteButton.setVisibility(View.VISIBLE);
                if (currentUser.isFavorite(eventId)) {
                    favoriteButton.setImageResource(R.drawable.baseline_star_24);
                } else {
                    favoriteButton.setImageResource(R.drawable.baseline_star_border_24);
                }

                favoriteButton.setOnClickListener(v -> {
                    if (currentUser.isFavorite(eventId)) {
                        currentUser.removeFavoriteEvent(eventId);
                    } else {
                        currentUser.addFavoriteEvent(eventId);
                    }
                    db.updateUser(currentUser);
                    refreshUI();
                });
            } else if (favoriteButton != null) {
                favoriteButton.setVisibility(View.GONE);
            }

            displayTags(event.getTags());
            loadComments();
        }
    }

    private void updateMapMarkers(MapView map, Event event) {
        if (map == null) return;

        map.getOverlays().clear();

        // Use a background thread for geocoding if coordinates are missing
        new Thread(() -> {
            Double lat = event.getLatitude();
            Double lng = event.getLongitude();

            if (lat == null || lng == null) {
                String locName = event.getLocation();
                if (locName != null && !locName.isEmpty()) {
                    try {
                        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocationName(locName, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            lat = addresses.get(0).getLatitude();
                            lng = addresses.get(0).getLongitude();
                            event.setLatitude(lat);
                            event.setLongitude(lng);
                            // Not updating DB here to avoid recursion, just using for display
                        }
                    } catch (Exception ignored) {}
                }
            }

            final Double finalLat = lat;
            final Double finalLng = lng;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Add Event Location Pin (Gold)
                    if (finalLat != null && finalLng != null) {
                        GeoPoint eventPoint = new GeoPoint(finalLat, finalLng);
                        Marker eventMarker = new Marker(map);
                        eventMarker.setPosition(eventPoint);
                        eventMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_event_pin));
                        eventMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        eventMarker.setTitle("EVENT: " + event.getName());
                        eventMarker.setSubDescription(event.getLocation());
                        map.getOverlays().add(eventMarker);
                        
                        // Focus on event location
                        map.getController().animateTo(eventPoint);
                    }

                    // Add Entrant Pins (Red)
                    ArrayList<User> entrants = event.getEntrants();
                    if (entrants != null && !entrants.isEmpty()) {
                        for (User entrant : entrants) {
                            if (entrant.getLatitude() != null && entrant.getLongitude() != null) {
                                GeoPoint point = new GeoPoint(entrant.getLatitude(), entrant.getLongitude());

                                Marker marker = new Marker(map);
                                marker.setPosition(point);
                                marker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_custom_pin));
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                marker.setTitle(entrant.getName());
                                map.getOverlays().add(marker);
                            }
                        }
                    }
                    map.invalidate();
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        db.addListener(this);
        if (organizerMapView != null) organizerMapView.onResume();
        if (entrantMapView != null) entrantMapView.onResume();
        refreshUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        db.removeListener(this);
        if (organizerMapView != null) organizerMapView.onPause();
        if (entrantMapView != null) entrantMapView.onPause();
    }

    @Override
    public void onDestroy() {
        if (currentNativeAd != null) {
            currentNativeAd.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onDataChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::refreshUI);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.95);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}