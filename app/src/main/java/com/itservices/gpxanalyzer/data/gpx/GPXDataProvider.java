package com.itservices.gpxanalyzer.data.gpx;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.itservices.gpxanalyzer.data.gpx.calculation.LocationCalculatorUtil;
import com.itservices.gpxanalyzer.logbook.RequestType;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import itservices.gpxparser.GPXParser;
import itservices.gpxparser.domain.Gpx;
import itservices.gpxparser.domain.TrackPoint;
import itservices.gpxparser.domain.TrackSegment;

@Singleton
public class GPXDataProvider {

    @Inject
    public GPXParser mParser;

    @Inject
    public GPXDataProvider() {

    }

    public Observable<Vector<Location>> provide(Context context, int rawId, MutableLiveData<RequestType> requestTypeLiveData, MutableLiveData<Integer> percentageProgressLiveData) {
        requestTypeLiveData.postValue(RequestType.LOADING);
        return provide(context.getResources().openRawResource(rawId), requestTypeLiveData, percentageProgressLiveData);
    }

    public Observable<Vector<Location>> provide(InputStream in, MutableLiveData<RequestType> requestTypeLiveData, MutableLiveData<Integer> percentageProgressLiveData) {

        Vector<Location> gpxPointList = new Vector<>();

        Gpx parsedGpx = null;
        try {

            parsedGpx = mParser.parse(in);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        requestTypeLiveData.postValue(RequestType.PROCESSING);

        if (parsedGpx != null) {

            parsedGpx.getTracks()
                    .forEach(track ->
                            track.getTrackSegments()
                                    .forEach(segment ->
                                            addGpxPointsFromSegment(gpxPointList, segment, percentageProgressLiveData)
                                    )
                    );
        } else {
            Log.e("GPXDataProvider", "Error parsing gpx track!");
        }

        return Observable.just(gpxPointList);
    }

    private static void addGpxPointsFromSegment(Vector<Location> gpxPointList, TrackSegment segment, MutableLiveData<Integer> percentageProgressLiveData) {

        int maxIteration = segment.getTrackPoints().size() - 1;

        for (int iTrackPoint = 0; iTrackPoint < maxIteration; iTrackPoint++) {

            Location gpxPointA = createLocation(
                    segment.getTrackPoints().get(iTrackPoint)
            );
            Location gpxPointB = createLocation(
                    segment.getTrackPoints().get(iTrackPoint + 1)
            );

            Location gpxPointMeanWithSpeed = LocationCalculatorUtil.calculateCentroidManual( Arrays.asList(gpxPointA, gpxPointB) );

            float speed = LocationCalculatorUtil.calculateSpeed3D(gpxPointA, gpxPointB);
            gpxPointMeanWithSpeed.setSpeed(speed);
            gpxPointMeanWithSpeed.setTime( LocationCalculatorUtil.computeMeanTime(gpxPointA, gpxPointB) );

            Log.d("GPXDataProvider", "gpxPointMeanWithSpeed = [" + gpxPointMeanWithSpeed.getTime() + "]");

            float percentageProgress = 100.0f * ( (float)(iTrackPoint + 1) / (float)maxIteration );

            percentageProgressLiveData.postValue((int) percentageProgress);

            gpxPointList.add( gpxPointMeanWithSpeed );
        }
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
