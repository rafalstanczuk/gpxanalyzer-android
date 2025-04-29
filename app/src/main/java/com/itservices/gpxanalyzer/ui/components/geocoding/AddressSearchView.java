package com.itservices.gpxanalyzer.ui.components.geocoding;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.data.model.geocoding.GeocodingResult;
import com.itservices.gpxanalyzer.data.provider.network.geocoding.GeocodingNetworkRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

/**
 * Custom view component that provides address search functionality with
 * auto-suggestions using Maps.co geocoding API.
 */
public class AddressSearchView extends LinearLayout {

    private AppCompatEditText searchEditText;
    private ListView suggestionsListView;
    private ProgressBar progressBar;
    private TextView errorTextView;

    private ArrayAdapter<String> suggestionsAdapter;
    private List<GeocodingResult> geocodingResults = new ArrayList<>();
    private GeocodingNetworkRepository geocodingRepository;
    
    private PublishSubject<String> querySubject = PublishSubject.create();
    private CompositeDisposable disposables = new CompositeDisposable();
    
    private OnLocationSelectedListener locationSelectedListener;

    public AddressSearchView(Context context) {
        super(context);
        init(context);
    }

    public AddressSearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AddressSearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Sets the geocoding repository for this view.
     * Must be called before using the view.
     */
    public void setGeocodingRepository(GeocodingNetworkRepository geocodingRepository) {
        this.geocodingRepository = geocodingRepository;
        setupSearchDebounce();
    }

    /**
     * Sets a listener to be notified when a location is selected.
     */
    public void setOnLocationSelectedListener(OnLocationSelectedListener listener) {
        this.locationSelectedListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disposables.clear();
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        
        LayoutInflater.from(context).inflate(R.layout.view_address_search, this, true);
        
        searchEditText = findViewById(R.id.search_edit_text);
        suggestionsListView = findViewById(R.id.suggestions_list_view);
        progressBar = findViewById(R.id.progress_bar);
        errorTextView = findViewById(R.id.error_text_view);
        
        suggestionsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, new ArrayList<>());
        suggestionsListView.setAdapter(suggestionsAdapter);
        
        setupListeners();
    }

    private void setupListeners() {
        // Text change listener for search input
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                querySubject.onNext(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        
        // IME action listener for search input
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (searchEditText.getText() != null && !searchEditText.getText().toString().isEmpty()) {
                    performSearch(searchEditText.getText().toString());
                }
                return true;
            }
            return false;
        });
        
        // Item click listener for suggestions
        suggestionsListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < geocodingResults.size()) {
                GeocodingResult result = geocodingResults.get(position);
                searchEditText.setText(result.getFormattedAddress());
                suggestionsAdapter.clear();
                suggestionsAdapter.notifyDataSetChanged();
                suggestionsListView.setVisibility(View.GONE);
                
                if (locationSelectedListener != null) {
                    locationSelectedListener.onLocationSelected(result);
                }
            }
        });
    }

    private void setupSearchDebounce() {
        if (geocodingRepository == null) {
            return;
        }
        
        disposables.add(querySubject
                .debounce(300, TimeUnit.MILLISECONDS) // Debounce to avoid too many API calls
                .filter(query -> query.length() >= 3) // Only search for queries with at least 3 characters
                .distinctUntilChanged() // Avoid duplicate queries
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::performSearch, throwable -> {
                    showError("Search error: " + throwable.getMessage());
                }));
    }

    private void performSearch(String query) {
        if (geocodingRepository == null) {
            showError("Geocoding service not configured");
            return;
        }
        
        if (query.trim().isEmpty()) {
            suggestionsAdapter.clear();
            suggestionsAdapter.notifyDataSetChanged();
            suggestionsListView.setVisibility(View.GONE);
            return;
        }
        
        showLoading();
        hideError();
        
        disposables.add(geocodingRepository.geocodeAddress(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(results -> {
                    hideLoading();
                    updateSuggestions(results);
                }, throwable -> {
                    hideLoading();
                    showError("Search error: " + throwable.getMessage());
                }));
    }

    private void updateSuggestions(List<GeocodingResult> results) {
        geocodingResults = results;
        suggestionsAdapter.clear();
        
        for (GeocodingResult result : results) {
            suggestionsAdapter.add(result.getFormattedAddress());
        }
        
        suggestionsAdapter.notifyDataSetChanged();
        suggestionsListView.setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showError(String errorMessage) {
        errorTextView.setText(errorMessage);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorTextView.setVisibility(View.GONE);
    }

    /**
     * Interface for notifying when a location is selected.
     */
    public interface OnLocationSelectedListener {
        void onLocationSelected(GeocodingResult location);
    }
} 