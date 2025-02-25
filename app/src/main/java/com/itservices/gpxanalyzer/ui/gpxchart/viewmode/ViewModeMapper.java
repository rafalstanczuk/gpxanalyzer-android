package com.itservices.gpxanalyzer.ui.gpxchart.viewmode;

import android.content.Context;

import androidx.annotation.StringRes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;


@Singleton
public class ViewModeMapper {

    private final Context context;

    private final Map<ViewMode, Integer> viewModeToPrimaryKeyIndex = new HashMap<>();

    @Inject
    public ViewModeMapper(@ApplicationContext Context context) {
        this.context = context;
    }

    public void init(List<String> nameUnitList) {
        for (ViewMode viewMode : ViewMode.values()) {
            viewModeToPrimaryKeyIndex.put(viewMode,
                    getNewPrimaryIndexFromNameStringRes(context, nameUnitList, viewMode.getPrimaryKeyStringId())
            );
        }
    }

    public int mapToPrimaryKeyIndexList(ViewMode viewMode) {
        Integer index = viewModeToPrimaryKeyIndex.get(viewMode);

        assert index != null;

        return index;
    }

    private int getNewPrimaryIndexFromNameStringRes(Context context, List<String> nameUnitList, @StringRes int id) {
        return nameUnitList.indexOf(
                context.getResources().getString(id)
        );
    }
}
