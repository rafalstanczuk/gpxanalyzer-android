package com.itservices.gpxanalyzer.feature.gpxchart.data.provider.file;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.itservices.gpxanalyzer.R;
import com.itservices.gpxanalyzer.core.data.cache.rawdata.LoadDataCache;
import com.itservices.gpxanalyzer.core.data.mapper.LocationMapper;
import com.itservices.gpxanalyzer.core.data.model.entity.DataEntity;
import com.itservices.gpxanalyzer.core.data.model.entity.DataMeasure;
import com.itservices.gpxanalyzer.core.events.EventProgress;
import com.itservices.gpxanalyzer.core.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.core.utils.location.LocationCalculatorUtil;
import com.itservices.gpxanalyzer.core.data.parser.GPXParser;
import com.itservices.gpxanalyzer.core.data.parser.domain.Gpx;
import com.itservices.gpxanalyzer.core.data.parser.domain.TrackSegment;
import com.itservices.gpxanalyzer.feature.gpxchart.ui.viewmode.GpxViewModeMapper;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.Single;

/**
 * A provider class that converts GPX data into DataEntity objects.
 * This class is responsible for parsing GPX files and transforming the track points
 * into a format suitable for analysis and visualization.
 *
 * The provider supports:
 * - Parsing GPX files
 * - Converting track points to DataEntity objects
 * - Handling multiple tracks and segments
 * - Managing file input streams
 */
public final class GpxFileDataEntityProvider extends FileDataEntityProvider {

    @RawRes
    private static int DEFAULT_RAW_GPX_DATA = R.raw.skiing20250121t091423;

    private static List<String> NAME_LIST = new ArrayList<>();
    private static List<String> UNIT_LIST = new ArrayList<>();

    private final WeakReference<Context> contextWeakReference;

    @Inject
    public GPXParser parser;

    @Inject
    public LoadDataCache dataCachedProvider;

    @Inject
    GlobalEventWrapper globalEventWrapper;

    @Inject
    public GpxFileDataEntityProvider(@ApplicationContext Context context, GpxViewModeMapper viewModeMapper) {
        contextWeakReference = new WeakReference<>(context);

        NAME_LIST = Arrays.asList(context.getResources().getStringArray(R.array.gpx_name_unit_array));
        UNIT_LIST = Arrays.asList(context.getResources().getStringArray(R.array.gpx_unit_array));

        viewModeMapper.init(NAME_LIST);
    }

    @Override
    public Single<Vector<DataEntity>> provideDefault() {
        return contextWeakReference == null || contextWeakReference.get() == null ?
                super.provideDefault()
                :
                provideInternal(contextWeakReference.get(), DEFAULT_RAW_GPX_DATA);
    }

    @Override
    public Single<Vector<DataEntity>> provide(@NonNull InputStream inputStream) {
        return Single.fromCallable(() -> loadDataEntity(inputStream));
    }

    public Single<Vector<DataEntity>> provide(@NonNull File file) {
        return Single.fromCallable(() -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                return loadDataEntity(inputStream);
            }
        });
    }

    private Single<Vector<DataEntity>> provideInternal(Context context, int rawId) {
        return Single.fromCallable(() -> {
            InputStream inputStream = context.getResources().openRawResource(rawId);
            return loadDataEntity(inputStream);
        });
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

        dataCachedProvider.init(UNIT_LIST.size());

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

    private void addGpxPointsFromSegment(@NonNull Vector<DataEntity> gpxPointList, @NonNull TrackSegment segment) {

        int maxIteration = segment.getTrackPoints().size() - 1;

        EventProgress lastEventProgress = EventProgress.create(GpxFileDataEntityProvider.class, 0, maxIteration);
        globalEventWrapper.onNext(lastEventProgress);

        for (int iTrackPoint = 0; iTrackPoint < maxIteration; iTrackPoint++) {

            Location gpxPointA = LocationMapper.mapFrom(
                    segment.getTrackPoints().get(iTrackPoint)
            );
            Location gpxPointB = LocationMapper.mapFrom(
                    segment.getTrackPoints().get(iTrackPoint + 1)
            );

            Location centroidLocation = LocationCalculatorUtil.calculateCentroidLocation(gpxPointA, gpxPointB);

            DataEntity dataEntity = createDataEntity(iTrackPoint, centroidLocation);

            dataCachedProvider.accept(dataEntity);

            EventProgress currentEventProgress = EventProgress.create(GpxFileDataEntityProvider.class, iTrackPoint + 1, maxIteration);

            lastEventProgress = globalEventWrapper.onNextChanged(lastEventProgress, currentEventProgress);

            gpxPointList.add(dataEntity);
        }
    }

    @NonNull
    private static DataEntity createDataEntity(int iTrackPoint, Location location) {
        return new DataEntity(iTrackPoint, location.getTime(),
                List.of(
                        new DataMeasure((float) location.getAltitude(),
                                0.1f,
                                NAME_LIST.get(0),
                                UNIT_LIST.get(0)
                                ),
                        new DataMeasure(location.getSpeed(),
                                0.1f,
                                NAME_LIST.get(1),
                                UNIT_LIST.get(1)
                        )

                ),
                location
        );
    }
}
