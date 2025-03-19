package com.itservices.gpxanalyzer.ui.gpxchart.viewmode;

import android.content.Context;

import androidx.annotation.StringRes;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;


@Singleton
public class GpxViewModeMapper implements ViewModeMapper {

    private final WeakReference<Context> contextWeakReference;

    private final Map<GpxViewMode, Integer> viewModeToPrimaryKeyIndex = new HashMap<>();

    @Inject
    public GpxViewModeMapper(@ApplicationContext Context context) {
        contextWeakReference = new WeakReference<>(context);
    }

    @Override
    public void init(List<String> nameUnitList) {
        for (GpxViewMode viewMode : GpxViewMode.values()) {
            viewModeToPrimaryKeyIndex.put(viewMode,
                    getNewPrimaryIndexFromNameStringRes(contextWeakReference.get(), nameUnitList, viewMode.getPrimaryKeyStringId())
            );
        }
    }

    @Override
    public int mapToPrimaryKeyIndexList(Enum<?> viewMode) {
        if (!(viewMode instanceof GpxViewMode)) {
            return -1;
        }

        Integer index = viewModeToPrimaryKeyIndex.get( (GpxViewMode) viewMode);

        assert index != null;

        return index;
    }

    private int getNewPrimaryIndexFromNameStringRes(Context context, List<String> nameUnitList, @StringRes int id) {
        return nameUnitList.indexOf(
                context.getResources().getString(id)
        );
    }
}
