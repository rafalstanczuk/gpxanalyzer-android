package com.itservices.gpxanalyzer.core.data.provider.db.geocoding;

import android.location.Location;
import android.util.Log;

import com.itservices.gpxanalyzer.core.data.model.geocoding.GeocodingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * Repository class for handling geocoding data operations.
 * Provides a clean API for accessing and managing geocoding results.
 */
@Singleton
public class GeocodingLocalRepository {
    private final GeocodingResultDao geocodingResultDao;

    // Approximate meters per degree at the equator
    private static final double METERS_PER_DEGREE = 111320.0;
    private final static String TAG = GeocodingLocalRepository.class.getSimpleName();

    @Inject
    public GeocodingLocalRepository(GeocodingDatabase database) {
        this.geocodingResultDao = database.geocodingResultDao();
    }

    /**
     * Inserts a geocoding result into the database.
     *
     * @param result The geocoding result to insert
     * @return Completable that completes when the operation is done
     */
    public Completable insert(GeocodingResult result) {
        return Single.fromCallable(() -> GeocodingResultEntity.fromGeocodingResult(result))
                .flatMap(geocodingResultDao::upsert)
                .ignoreElement()
                .subscribeOn(Schedulers.io());
    }

    /**
     * Inserts multiple geocoding results into the database.
     *
     * @param results The list of geocoding results to insert
     * @return Completable that completes when the operation is done
     */
    public Completable insertAll(List<GeocodingResult> results) {
        //Log.d(TAG, "insertAll() called with: results = [" + results + "]");

        return Single.fromCallable(() -> results.stream()
                .map(GeocodingResultEntity::fromGeocodingResult)
                .collect(Collectors.toCollection(ArrayList::new)))
                .flatMapCompletable(geocodingResultDao::insertAll)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Finds geocoding results within an epsilon range of the given coordinates.
     * This is useful when dealing with floating-point precision issues.
     *
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param epsilon The maximum allowed difference for both latitude and longitude
     * @return Flowable that emits lists of matching geocoding results
     */
    public Flowable<ArrayList<GeocodingResult>> findByCoordinatesWithEpsilonGrade(double latitude, double longitude, double epsilon) {
        return geocodingResultDao.findByCoordinatesWithEpsilon(latitude, longitude, epsilon)
                .map(entities -> entities.stream()
                        .map(GeocodingResultEntity::toGeocodingResult)
                        .collect(Collectors.toCollection(ArrayList::new)))
                .subscribeOn(Schedulers.io());
    }

    public Flowable<ArrayList<GeocodingResult>> findByLocationWithEpsilonGrade(Location location, double epsilon) {
        return findByCoordinatesWithEpsilonGrade(location.getLatitude(), location.getLongitude(), epsilon);
    }

    public Flowable<ArrayList<GeocodingResult>> findByLocationWithinDistance(Location location, double distanceMeters) {
        return findByCoordinatesWithinDistance(location.getLatitude(), location.getLongitude(), distanceMeters);
    }

    /**
     * Finds geocoding results within a specified distance in meters from the given coordinates.
     * This method converts the distance in meters to degrees for the database query.
     * Note: This is an approximation that works best near the equator.
     *
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param distanceMeters The maximum distance in meters
     * @return Flowable that emits lists of matching geocoding results
     */
    public Flowable<ArrayList<GeocodingResult>> findByCoordinatesWithinDistance(double latitude, double longitude, double distanceMeters) {
        double epsilonInDegrees = distanceMeters / METERS_PER_DEGREE;
        return findByCoordinatesWithEpsilonGrade(latitude, longitude, epsilonInDegrees);
    }

    /**
     * Finds a geocoding result by place ID.
     *
     * @param placeId The place ID to search for
     * @return Maybe that emits the geocoding result if found
     */
    public Maybe<GeocodingResult> findByPlaceId(String placeId) {
        return geocodingResultDao.findByPlaceId(placeId)
                .map(GeocodingResultEntity::toGeocodingResult)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Searches for geocoding results by display name.
     *
     * @param query The search query
     * @return Flowable that emits lists of matching geocoding results
     */
    public Flowable<ArrayList<GeocodingResult>> searchByDisplayName(String query) {
        return geocodingResultDao.searchByDisplayName(query)
                .map(entities -> entities.stream()
                        .map(GeocodingResultEntity::toGeocodingResult)
                        .collect(Collectors.toCollection(ArrayList::new)))
                .subscribeOn(Schedulers.io());
    }

    /**
     * Deletes a geocoding result by place ID.
     *
     * @param placeId The place ID of the result to delete
     * @return Completable that completes when the operation is done
     */
    public Completable deleteByPlaceId(String placeId) {
        return geocodingResultDao.deleteByPlaceId(placeId)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Deletes all geocoding results from the database.
     *
     * @return Completable that completes when the operation is done
     */
    public Completable deleteAll() {
        return geocodingResultDao.deleteAll()
                .subscribeOn(Schedulers.io());
    }
} 