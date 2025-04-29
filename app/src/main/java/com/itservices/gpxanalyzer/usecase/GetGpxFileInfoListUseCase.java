package com.itservices.gpxanalyzer.usecase;

import com.itservices.gpxanalyzer.data.model.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.data.provider.GpxFileInfoProvider;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;

/**
 * Use case responsible for retrieving a list of GPX file information objects ({@link GpxFileInfo}).
 * It encapsulates the logic for fetching and potentially filtering GPX file metadata from a provider.
 */
@Singleton
public class GetGpxFileInfoListUseCase {
    private static final String TAG = GetGpxFileInfoListUseCase.class.getSimpleName();
    /**
     * Provider responsible for accessing GPX file information.
     */
    @Inject
    GpxFileInfoProvider gpxFileInfoProvider;

    /**
     * Constructor for dependency injection.
     */
    @Inject
    public GetGpxFileInfoListUseCase() {
    }

    /**
     * Retrieves a list of {@link GpxFileInfo} objects.
     * Delegates the call to the injected {@link GpxFileInfoProvider} to get and filter the files.
     *
     * @return A {@link Single} that emits a list of {@link GpxFileInfo} objects upon successful retrieval.
     */
    public Single<List<GpxFileInfo>> getGpxFileInfoList() {
        return gpxFileInfoProvider
                .getAndFilterGpxFiles();
    }
}

