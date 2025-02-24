package com.itservices.gpxanalyzer.data.gpx;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.data.DataEntity;
import com.itservices.gpxanalyzer.data.gpx.calculation.LocationCalculatorUtil;
import com.itservices.gpxanalyzer.data.gpx.parser.GPXParser;
import com.itservices.gpxanalyzer.data.gpx.parser.domain.Gpx;
import com.itservices.gpxanalyzer.data.gpx.parser.domain.TrackPoint;
import com.itservices.gpxanalyzer.data.gpx.parser.domain.TrackSegment;
import com.itservices.gpxanalyzer.ui.gpxchart.ViewModeMapper;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

@Singleton
public class GPXDataProvider {
    private static List<String> NAME_LIST = new ArrayList<>();
    private static List<String> UNIT_LIST = new ArrayList<>();

    @Inject
    public GPXParser parser;

    private final PublishSubject<Integer> percentageProgressSubject = PublishSubject.create();

    @Inject
    public GPXDataProvider(@ApplicationContext Context context, ViewModeMapper viewModeMapper) {
        NAME_LIST = Arrays.asList(context.getResources().getStringArray(R.array.gpx_name_unit_array));
        UNIT_LIST = Arrays.asList(context.getResources().getStringArray(R.array.gpx_unit_array));

        viewModeMapper.init(NAME_LIST);
    }

    public Observable<Integer> getPercentageProgress() {
        return percentageProgressSubject;
    }

    public Observable<Vector<DataEntity>> provide(Context context, int rawId) {
        return Observable.fromCallable(() -> {
            InputStream inputStream = context.getResources().openRawResource(rawId);
            return loadDataEntity(inputStream);
        });
    }

    public Observable<Vector<DataEntity>> provide(File file) {
        return Observable.fromCallable(() -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                return loadDataEntity(inputStream);
            }
        });
    }

    public Observable<Vector<DataEntity>> provide(InputStream inputStream) {
        return Observable.fromCallable(() -> loadDataEntity(inputStream));
    }

    @NonNull
    private Vector<DataEntity> loadDataEntity(InputStream inputStream) {
        Vector<DataEntity> gpxPointList = new Vector<>();

        Gpx parsedGpx = null;
        try {
            parsedGpx = parser.parse(inputStream);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        if (parsedGpx != null) {
            parsedGpx.getTracks()
                    .forEach(track ->
                            track.getTrackSegments()
                                    .forEach(segment ->
                                            addGpxPointsFromSegment(gpxPointList, segment)
                                    )
                    );
        } else {
            Log.e("GPXDataProvider", "Error parsing gpx track!");
        }

        return gpxPointList;
    }

    private void addGpxPointsFromSegment(Vector<DataEntity> gpxPointList, TrackSegment segment) {

        int maxIteration = segment.getTrackPoints().size() - 1;

        int lastIntPercentageProgress = 0;
        percentageProgressSubject.onNext(lastIntPercentageProgress);

        for (int iTrackPoint = 0; iTrackPoint < maxIteration; iTrackPoint++) {

            Location gpxPointA = createLocation(
                    segment.getTrackPoints().get(iTrackPoint)
            );
            Location gpxPointB = createLocation(
                    segment.getTrackPoints().get(iTrackPoint + 1)
            );

            Location centroidLocation = LocationCalculatorUtil.calculateCentroidManual(Arrays.asList(gpxPointA, gpxPointB));

            float speed = LocationCalculatorUtil.calculateSpeed3D(gpxPointA, gpxPointB);
            centroidLocation.setSpeed(speed);
            centroidLocation.setTime(LocationCalculatorUtil.computeMeanTime(gpxPointA, gpxPointB));

            DataEntity dataEntity = createDataEntity(iTrackPoint, centroidLocation);

            float percentageProgress = 100.0f * ((float) (iTrackPoint + 1) / (float) maxIteration);

            int intPercentageProgress = (int) percentageProgress;

            if (lastIntPercentageProgress != intPercentageProgress) {
                lastIntPercentageProgress = intPercentageProgress;
                percentageProgressSubject.onNext(intPercentageProgress);
            }

            gpxPointList.add(dataEntity);
        }
    }

    @NonNull
    private DataEntity createDataEntity(int iTrackPoint, Location location) {
        DataEntity dataEntity = new DataEntity(iTrackPoint, location.getTime(),
                Arrays.asList((float) location.getAltitude(), location.getSpeed()),
                NAME_LIST,
                UNIT_LIST);

        return dataEntity;
    }

    @NonNull
    private static Location createLocation(TrackPoint trackPoint) {
        Location location = new Location("TrackPoint");

        location.setLatitude(trackPoint.getLatitude());
        location.setLongitude(trackPoint.getLongitude());
        location.setAltitude(trackPoint.getElevation());
        location.setTime(trackPoint.getTime().toDate().getTime());

        return location;
    }

}
