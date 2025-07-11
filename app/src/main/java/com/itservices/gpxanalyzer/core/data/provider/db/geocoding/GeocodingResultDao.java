package com.itservices.gpxanalyzer.core.data.provider.db.geocoding;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Data Access Object for GeocodingResultEntity.
 * Defines database operations for geocoding results.
 */
@Dao
public interface GeocodingResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(GeocodingResultEntity result);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<GeocodingResultEntity> results);

    @Query("SELECT * FROM geocoding_results WHERE " +
           "ABS(latitude - :latitude) <= :epsilon AND " +
           "ABS(longitude - :longitude) <= :epsilon")
    Flowable<List<GeocodingResultEntity>> findByCoordinatesWithEpsilon(double latitude, double longitude, double epsilon);

    @Query("SELECT * FROM geocoding_results WHERE placeId = :placeId")
    Maybe<GeocodingResultEntity> findByPlaceId(String placeId);

    @Query("SELECT * FROM geocoding_results WHERE displayName LIKE '%' || :query || '%'")
    Flowable<List<GeocodingResultEntity>> searchByDisplayName(String query);

    @Query("DELETE FROM geocoding_results WHERE placeId = :placeId")
    Completable deleteByPlaceId(String placeId);

    @Query("DELETE FROM geocoding_results")
    Completable deleteAll();

    default Single<GeocodingResultEntity> upsert(GeocodingResultEntity result) {
        return Single.fromCallable(() -> {
            GeocodingResultEntity existing = findByPlaceIdSync(result.placeId);
            GeocodingResultEntity toInsert = new GeocodingResultEntity(result);
            
            if (existing != null) {
                toInsert.id = existing.id;
            }
            
            insertSync(toInsert);
            return toInsert;
        });
    }

    @Query("SELECT * FROM geocoding_results WHERE placeId = :placeId")
    GeocodingResultEntity findByPlaceIdSync(String placeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSync(GeocodingResultEntity result);
} 