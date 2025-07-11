package com.itservices.gpxanalyzer.core.data.provider.geocoding.android;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.itservices.gpxanalyzer.core.data.model.geocoding.ForwardGeocodingResponse;
import com.itservices.gpxanalyzer.core.data.model.geocoding.GeocodingResult;
import com.itservices.gpxanalyzer.core.data.provider.geocoding.BaseGeocodingRepository;
import com.itservices.gpxanalyzer.core.data.provider.geocoding.GeocodingException;
import com.itservices.gpxanalyzer.core.events.EventProgress;
import com.itservices.gpxanalyzer.core.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.core.events.PercentageUpdateEventSourceType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.Single;

public class GeocodingAndroidRepository implements BaseGeocodingRepository {

    private static final String TAG = "GeocodingGoogleRepo";
    private final Geocoder androidGeocoder;

    private final GlobalEventWrapper events;
    private final Context context;

    private AtomicInteger progress;
    private AtomicReference<EventProgress> lastEventAtomic = new AtomicReference<>();


@Inject
    public GeocodingAndroidRepository(@ApplicationContext Context context, GlobalEventWrapper events) {
        this.context = context.getApplicationContext();
    this.events = events;
    if (Geocoder.isPresent()) {
            this.androidGeocoder = new Geocoder(this.context, Locale.getDefault());
        } else {
            this.androidGeocoder = null;
            Log.e(TAG, "Android Geocoder is not present on this device.");
        }
    }

    @Override
    public Single<GeocodingResult> reverseGeocode(Location location) {
        if (androidGeocoder == null) {
            return Single.error(new GeocodingException("Android Geocoder is not available."));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return reverseGeocodeTiramisu(location);
        } else {
            return reverseGeocodeLegacy(location);
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private Single<GeocodingResult> reverseGeocodeTiramisu(Location location) {
        return Single.create(emitter -> {
            if (!Geocoder.isPresent() || androidGeocoder == null) { // Check androidGeocoder nullability again
                 emitter.onError(new GeocodingException("Geocoder not present or not initialized"));
                 return;
            }
            try {
                androidGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1, new Geocoder.GeocodeListener() {
                    @Override
                    public void onGeocode(List<Address> addresses) {
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            GeocodingResult result = convertAddressToGeocodingResult(address, location);
                            emitter.onSuccess(result);
                        } else {
                            Log.w(TAG, "No address found by Android Geocoder for location: " + location);
                            emitter.onError(new GeocodingException("No address found by Android Geocoder"));
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        String msg = "Android Geocoder error: " + (errorMessage != null ? errorMessage : "Unknown error");
                        Log.e(TAG, msg + " for location: " + location);
                        emitter.onError(new GeocodingException(msg));
                    }
                });
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "IllegalArgumentException for getFromLocation: " + e.getMessage());
                emitter.onError(new GeocodingException("Invalid location parameters for Geocoder: " + e.getMessage(), e));
            }
        });
    }

    @SuppressWarnings("deprecation")
    private Single<GeocodingResult> reverseGeocodeLegacy(Location location) {
         if (!Geocoder.isPresent() || androidGeocoder == null) { // Check androidGeocoder nullability again
             return Single.error(new GeocodingException("Geocoder not present or not initialized"));
         }
        return Single.fromCallable(() -> {
            try {
                List<Address> addresses = androidGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    return convertAddressToGeocodingResult(addresses.get(0), location);
                } else {
                    Log.w(TAG, "No address found by legacy Android Geocoder for location: " + location);
                    throw new GeocodingException("No address found by legacy Android Geocoder");
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException from legacy Android Geocoder for location: " + location, e);
                throw new GeocodingException("IOException from legacy Android Geocoder: " + e.getMessage(), e);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "IllegalArgumentException for legacy getFromLocation: " + e.getMessage());
                throw new GeocodingException("Invalid location parameters for Geocoder: " + e.getMessage(), e);
            }
        });
    }

    private GeocodingResult convertAddressToGeocodingResult(Address androidAddress, Location originalLocation) {
        GeocodingResult result = new GeocodingResult();
        result.latitude = androidAddress.hasLatitude() ? androidAddress.getLatitude() : originalLocation.getLatitude();
        result.longitude = androidAddress.hasLongitude() ? androidAddress.getLongitude() : originalLocation.getLongitude();

        StringBuilder displayNameBuilder = new StringBuilder();
        for (int i = 0; i <= androidAddress.getMaxAddressLineIndex(); i++) {
            if (i > 0) displayNameBuilder.append(", ");
            displayNameBuilder.append(androidAddress.getAddressLine(i));
        }
        result.displayName = displayNameBuilder.toString();
        if (result.displayName == null || result.displayName.isEmpty()) {
            result.displayName = "Address not available";
        }

        result.street = androidAddress.getThoroughfare();
        result.houseNumber = androidAddress.getSubThoroughfare();
        result.city = androidAddress.getLocality();
        result.suburb = androidAddress.getSubLocality();
        result.state = androidAddress.getAdminArea();
        result.country = androidAddress.getCountryName();
        result.postalCode = androidAddress.getPostalCode();
        
        // Fields from GeocodingResult not directly available in android.location.Address
        // might need to be set to null/default or derived if possible.
        // result.placeId = null; // No direct equivalent
        // result.license = null; // No direct equivalent
        // result.osmType = null; // No direct equivalent
        // result.osmId = null; // No direct equivalent
        // result.importance = 0.0; // No direct equivalent
        // result.boundingBox = null; // No direct equivalent

        return result;
    }

    @Override
    public Single<ForwardGeocodingResponse> geocodeAddress(String address) {
        return Single.error(new UnsupportedOperationException("geocodeAddress not implemented in GeocodingGoogleRepository yet."));
    }

    @Override
    public Single<ForwardGeocodingResponse> geocodeStructuredAddress(String street, String city, String state, String country, String postalCode) {
        return Single.error(new UnsupportedOperationException("geocodeStructuredAddress not implemented in GeocodingGoogleRepository."));
    }
    
    @Override
    public Single<ArrayList<ForwardGeocodingResponse>> batchGeocodeAddresses(List<String> addresses) {
        return Single.error(new UnsupportedOperationException("batchGeocodeAddresses not implemented in GeocodingGoogleRepository."));
    }

    @Override
    public Single<ArrayList<GeocodingResult>> batchReverseGeocode(List<Location> points) {
         if (androidGeocoder == null) {
            return Single.error(new GeocodingException("Android Geocoder is not available."));
        }
        List<Single<GeocodingResult>> singles = new ArrayList<>();
        startEventProgress(points);

        for (Location point : points) {
            singles.add(reverseGeocode(point)
                            .doOnSuccess(geocodingResults -> updateEventProgress(points))
                .onErrorReturnItem(createErrorGeocodingResult(point))
            );
        }
        return Single.zip(singles, objects -> {
            ArrayList<GeocodingResult> results = new ArrayList<>();
            for (Object obj : objects) {
                results.add((GeocodingResult) obj);
            }
            return results;
        });
    }

    private void updateEventProgress(Collection<?> listToProcess) {
        if (progress == null) return; // Guard against null progress, e.g. if startEventProgress wasn't called
        EventProgress currentEvent = EventProgress.create(
                PercentageUpdateEventSourceType.GEOCODING_PROCESSING,
                progress.incrementAndGet(), listToProcess.size()
        );
        if (events != null) { // Guard against null events
            events.onNextChanged(lastEventAtomic.getAndSet(currentEvent), currentEvent);
        }
    }

    private void startEventProgress(Collection<?> listToProcess) {
        progress = new AtomicInteger(0);
        EventProgress initialEvent = EventProgress.create(
                PercentageUpdateEventSourceType.GEOCODING_PROCESSING,
                progress.get(), listToProcess.size()
        );
        lastEventAtomic.set(initialEvent);
        if (events != null) { // Guard against null events
            events.onNext(initialEvent);
        }
    }
    
    private GeocodingResult createErrorGeocodingResult(Location point) {
        GeocodingResult errorResult = new GeocodingResult();
        errorResult.latitude = point.getLatitude();
        errorResult.longitude = point.getLongitude();
        errorResult.displayName = "Error: Geocoding failed for this point";
        // Populate other fields as null or defaults
        return errorResult;
    }
} 