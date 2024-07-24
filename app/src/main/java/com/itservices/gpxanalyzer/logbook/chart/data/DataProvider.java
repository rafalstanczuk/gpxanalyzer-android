package com.itservices.gpxanalyzer.logbook.chart.data;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import itservices.gpxparser.GPXParser;
import itservices.gpxparser.domain.Extensions;
import itservices.gpxparser.domain.Gpx;
import itservices.gpxparser.domain.Track;
import itservices.gpxparser.domain.TrackPoint;
import itservices.gpxparser.domain.TrackSegment;

@Singleton
public class DataProvider implements IDataProvider {

    @Inject
    public GPXParser mParser;

    @Inject
    public DataProvider() {

    }

    public Observable<Vector<Measurement>> provide(Context context, int rawId) {

        Vector<Measurement> measurementList = new Vector<>();

        Gpx parsedGpx = null;
        try {
            //InputStream in = getAssets().open("test20230719.gpx");
            InputStream in = context.getResources().openRawResource(rawId);
            parsedGpx = mParser.parse(in); // consider doing this on a background thread
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        if (parsedGpx != null) {
            // log stuff
            List<Track> tracks = parsedGpx.getTracks();

            for (int i = 0; i < tracks.size(); i++) {

                Track track = tracks.get(i);
                //Log.d(TAG, "track " + i + ":");
                List<TrackSegment> segments = track.getTrackSegments();
                for (int j = 0; j < segments.size(); j++) {
                    TrackSegment segment = segments.get(j);
                    //Log.d(TAG, "  segment " + j + ":");
                    for (TrackPoint trackPoint : segment.getTrackPoints()) {
						/*String msg = "    point: lat " + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude()
								+ ", elev " + trackPoint.getElevation()
								+ ", time " + trackPoint.getTime();*/
                        Extensions ext = trackPoint.getExtensions();
                        Double speed;
                        if (ext != null) {
                            speed = ext.getSpeed();
                            //msg = msg.concat(", speed " + speed);
                        }
                        //Log.d(TAG, msg);




                        Measurement measurement = new Measurement();

                        measurement.measurement = trackPoint.getElevation();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(trackPoint.getTime().toDate());

                        measurement.timestamp = calendar;

                        measurementList.add(measurement);

                    }
                }
            }

        } else {
            Log.e("DataProvider", "Error parsing gpx track!");
        }

        return Observable.just(measurementList);
    }
}
