package com.itservices.gpxanalyzer.feature.gpxlist.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.util.Log;

import com.itservices.gpxanalyzer.core.ui.components.miniature.MiniatureMapView;
import com.itservices.gpxanalyzer.domain.service.GpxFileInfoUpdateService;
import com.itservices.gpxanalyzer.feature.gpxlist.data.model.gpxfileinfo.GpxFileInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.observers.TestObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class UpdateGpxFileInfoListUseCaseTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock private Context mockContext;
    @Mock private GpxFileInfoUpdateService mockGpxFileUpdateService;
    private MiniatureMapView mockMiniatureRenderer;
    @Mock private GpxFileInfo mockGpxFileInfo1;
    @Mock private GpxFileInfo mockGpxFileInfo2;
    @Mock private File mockFile1;
    @Mock private File mockFile2;

    @InjectMocks private UpdateGpxFileInfoListUseCase updateGpxFileInfoListUseCase;

    @Captor private ArgumentCaptor<List<GpxFileInfo>> listCaptor;

    private MockedStatic<Log> logMockedStatic;

    private List<GpxFileInfo> initialList;

    @Before
    public void setUp() {
        logMockedStatic = Mockito.mockStatic(Log.class);
        logMockedStatic.when(() -> Log.d(anyString(), anyString())).thenReturn(0);
        logMockedStatic.when(() -> Log.e(anyString(), anyString())).thenReturn(0);
        logMockedStatic.when(() -> Log.e(anyString(), anyString(), any(Throwable.class))).thenReturn(0);
        logMockedStatic.when(() -> Log.i(anyString(), anyString())).thenReturn(0);

        mockMiniatureRenderer = mock(MiniatureMapView.class);

        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxJavaPlugins.setComputationSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());

        when(mockGpxFileInfo1.file()).thenReturn(mockFile1);
        when(mockFile1.getName()).thenReturn("file1.gpx");
        when(mockGpxFileInfo2.file()).thenReturn(mockFile2);
        when(mockFile2.getName()).thenReturn("file2.gpx");

        initialList = List.of(mockGpxFileInfo1, mockGpxFileInfo2);
    }

    @Test
    public void updateAndGenerateMiniatures_successPath_searchesGeneratesReplacesAndReportsProgress() {
        when(mockGpxFileUpdateService.scanFiles(mockContext)).thenReturn(Single.just(initialList));
        when(mockGpxFileUpdateService.generateMiniatures(anyList())).thenReturn(Completable.complete());
        when(mockGpxFileUpdateService.performGeocoding(anyList())).thenReturn(Completable.complete());
        when(mockGpxFileUpdateService.updateWithGeocodedLocations(anyList())).thenReturn(Completable.complete());
        when(mockGpxFileUpdateService.updateDatabase(anyList())).thenReturn(Completable.complete());

        TestObserver<Void> testObserver = updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(mockContext, mockMiniatureRenderer).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        InOrder inOrder = inOrder(mockGpxFileUpdateService);

        inOrder.verify(mockGpxFileUpdateService).setMiniatureRenderer(mockMiniatureRenderer);
        inOrder.verify(mockGpxFileUpdateService).scanFiles(mockContext);
        inOrder.verify(mockGpxFileUpdateService).generateMiniatures(listCaptor.capture());
        assertEquals(initialList, listCaptor.getValue());
        inOrder.verify(mockGpxFileUpdateService).performGeocoding(initialList);
        inOrder.verify(mockGpxFileUpdateService).updateWithGeocodedLocations(initialList);
        inOrder.verify(mockGpxFileUpdateService).updateDatabase(initialList);

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void updateAndGenerateMiniatures_whenSearchReturnsEmpty_completesWithoutProcessing() {
        when(mockGpxFileUpdateService.scanFiles(mockContext)).thenReturn(Single.just(Collections.emptyList()));

        TestObserver<Void> testObserver = updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(mockContext, mockMiniatureRenderer).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(mockGpxFileUpdateService).setMiniatureRenderer(mockMiniatureRenderer);
        verify(mockGpxFileUpdateService).scanFiles(mockContext);
        verify(mockGpxFileUpdateService, never()).generateMiniatures(anyList());
        verify(mockGpxFileUpdateService, never()).performGeocoding(anyList());
        verify(mockGpxFileUpdateService, never()).updateWithGeocodedLocations(anyList());
        verify(mockGpxFileUpdateService, never()).updateDatabase(anyList());
    }

    @Test
    public void updateAndGenerateMiniatures_whenSearchErrors_propagatesError() {
        Throwable searchError = new RuntimeException("Search failed");
        when(mockGpxFileUpdateService.scanFiles(mockContext)).thenReturn(Single.error(searchError));

        TestObserver<Void> testObserver = updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(mockContext, mockMiniatureRenderer).test();

        testObserver.assertError(searchError);

        verify(mockGpxFileUpdateService).setMiniatureRenderer(mockMiniatureRenderer);
        verify(mockGpxFileUpdateService).scanFiles(mockContext);
        verify(mockGpxFileUpdateService, never()).generateMiniatures(anyList());
        verify(mockGpxFileUpdateService, never()).performGeocoding(anyList());
        verify(mockGpxFileUpdateService, never()).updateWithGeocodedLocations(anyList());
        verify(mockGpxFileUpdateService, never()).updateDatabase(anyList());
    }

    @Test
    public void updateAndGenerateMiniatures_whenMiniatureRequestErrors_propagatesError() {
        Throwable miniatureError = new RuntimeException("Miniature request failed");
        when(mockGpxFileUpdateService.scanFiles(mockContext)).thenReturn(Single.just(initialList));
        when(mockGpxFileUpdateService.generateMiniatures(anyList())).thenReturn(Completable.error(miniatureError));

        TestObserver<Void> testObserver = updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(mockContext, mockMiniatureRenderer).test();

        testObserver.assertError(miniatureError);

        verify(mockGpxFileUpdateService).setMiniatureRenderer(mockMiniatureRenderer);
        verify(mockGpxFileUpdateService).scanFiles(mockContext);
        verify(mockGpxFileUpdateService).generateMiniatures(initialList);

        verify(mockGpxFileUpdateService, never()).performGeocoding(anyList());
        verify(mockGpxFileUpdateService, never()).updateWithGeocodedLocations(anyList());
        verify(mockGpxFileUpdateService, never()).updateDatabase(anyList());
    }

    @Test
    public void updateAndGenerateMiniatures_whenMiniatureResultErrors_propagatesError() {
        Throwable resultError = new RuntimeException("Geocoding failed");
        when(mockGpxFileUpdateService.scanFiles(mockContext)).thenReturn(Single.just(initialList));
        when(mockGpxFileUpdateService.generateMiniatures(anyList())).thenReturn(Completable.complete());
        when(mockGpxFileUpdateService.performGeocoding(anyList())).thenReturn(Completable.error(resultError));

        TestObserver<Void> testObserver = updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(mockContext, mockMiniatureRenderer).test();

        testObserver.assertError(resultError);

        InOrder inOrder = inOrder(mockGpxFileUpdateService);
        inOrder.verify(mockGpxFileUpdateService).setMiniatureRenderer(mockMiniatureRenderer);
        inOrder.verify(mockGpxFileUpdateService).scanFiles(mockContext);
        inOrder.verify(mockGpxFileUpdateService).generateMiniatures(initialList);
        inOrder.verify(mockGpxFileUpdateService).performGeocoding(initialList);

        inOrder.verify(mockGpxFileUpdateService, never()).updateWithGeocodedLocations(anyList());
        inOrder.verify(mockGpxFileUpdateService, never()).updateDatabase(anyList());
    }

    @Test
    public void updateAndGenerateMiniatures_whenReplaceAllErrors_propagatesError() {
        Throwable replaceError = new RuntimeException("Database update failed");
        when(mockGpxFileUpdateService.scanFiles(mockContext)).thenReturn(Single.just(initialList));
        when(mockGpxFileUpdateService.generateMiniatures(anyList())).thenReturn(Completable.complete());
        when(mockGpxFileUpdateService.performGeocoding(anyList())).thenReturn(Completable.complete());
        when(mockGpxFileUpdateService.updateWithGeocodedLocations(anyList())).thenReturn(Completable.complete());
        when(mockGpxFileUpdateService.updateDatabase(anyList())).thenReturn(Completable.error(replaceError));

        TestObserver<Void> testObserver = updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(mockContext, mockMiniatureRenderer).test();

        testObserver.assertError(replaceError);

        InOrder inOrder = inOrder(mockGpxFileUpdateService);
        inOrder.verify(mockGpxFileUpdateService).setMiniatureRenderer(mockMiniatureRenderer);
        inOrder.verify(mockGpxFileUpdateService).scanFiles(mockContext);
        inOrder.verify(mockGpxFileUpdateService).generateMiniatures(listCaptor.capture());
        assertEquals(initialList, listCaptor.getValue());
        inOrder.verify(mockGpxFileUpdateService).performGeocoding(initialList);
        inOrder.verify(mockGpxFileUpdateService).updateWithGeocodedLocations(initialList);
        inOrder.verify(mockGpxFileUpdateService).updateDatabase(initialList);
    }

    @After
    public void tearDown() {
        RxJavaPlugins.reset();
        RxAndroidPlugins.reset();
        if (logMockedStatic != null) {
            logMockedStatic.close();
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if ((expected == null && actual != null) || (expected != null && !expected.equals(actual))) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }
} 