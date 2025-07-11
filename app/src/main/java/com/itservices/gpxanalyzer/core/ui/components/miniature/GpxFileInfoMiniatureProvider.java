package com.itservices.gpxanalyzer.core.ui.components.miniature;

import android.util.Log;

import androidx.annotation.NonNull;

import com.itservices.gpxanalyzer.feature.gpxlist.data.model.gpxfileinfo.GpxFileInfo;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class GpxFileInfoMiniatureProvider {
    private static final String TAG = GpxFileInfoMiniatureProvider.class.getSimpleName();
    private final PublishSubject<GpxFileInfo> gpxFileInfoPublishSubject = PublishSubject.create();
    @Inject
    public GpxFileInfoMiniatureProvider() {
    }

    public Completable requestForGenerateMiniature(@NonNull MiniatureMapView miniatureMapViewRendererBind, GpxFileInfo gpxFileInfo) {
        return Completable.fromAction(() -> {
            //Log.d(GpxFileInfoMiniatureProvider.class.getSimpleName(), "generateMiniature() called with: miniatureMapViewRendererBind = [" + miniatureMapViewRendererBind + "], gpxFileInfo = [" + gpxFileInfo + "]");

            miniatureMapViewRendererBind.generateBitmap(gpxFileInfo.firstPointLocation(), bitmap -> {
                gpxFileInfo.setMiniatureBitmap(bitmap);

                gpxFileInfoPublishSubject.onNext(gpxFileInfo);

                //Log.d("GpxFileInfoMiniatureProvider", "generateMiniature() called with: bitmap = [" + bitmap + "]");

                return null;
            });
        });
    }

    public Observable<GpxFileInfo> getGpxFileInfoWithMiniature() {
        return gpxFileInfoPublishSubject.hide();
    }
}
