package com.itservices.gpxanalyzer.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.util.Log;

import com.itservices.gpxanalyzer.data.model.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.data.provider.GpxFileInfoProvider;
import com.itservices.gpxanalyzer.events.EventProgress;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.ui.components.miniature.GpxFileInfoMiniatureProvider;
import com.itservices.gpxanalyzer.ui.components.miniature.MiniatureMapView;

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

    @Mock private GpxFileInfoProvider mockGpxFileInfoProvider;
    @Mock private GpxFileInfoMiniatureProvider mockMiniatureProvider;
    @Mock private GlobalEventWrapper mockGlobalEventWrapper;
    @Mock private Context mockContext;
    private MiniatureMapView mockMiniatureRenderer;
    @Mock private GpxFileInfo mockGpxFileInfo1;
    @Mock private GpxFileInfo mockGpxFileInfo2;
    @Mock private File mockFile1;
    @Mock private File mockFile2;
    @Mock private EventProgress mockProgressInitial;
    @Mock private EventProgress mockProgress1;
    @Mock private EventProgress mockProgress2;

    @InjectMocks private UpdateGpxFileInfoListUseCase updateGpxFileInfoListUseCase;

    @Captor private ArgumentCaptor<List<GpxFileInfo>> listCaptor;
    @Captor private ArgumentCaptor<EventProgress> progressCaptor;

    private MockedStatic<Log> logMockedStatic;
    private MockedStatic<EventProgress> eventProgressMockedStatic;

    private List<GpxFileInfo> initialList;

    @Before
    public void setUp() {
        logMockedStatic = Mockito.mockStatic(Log.class);
        logMockedStatic.when(() -> Log.d(anyString(), anyString())).thenReturn(0);
        logMockedStatic.when(() -> Log.e(anyString(), anyString())).thenReturn(0);
        logMockedStatic.when(() -> Log.e(anyString(), anyString(), any(Throwable.class))).thenReturn(0);
        logMockedStatic.when(() -> Log.i(anyString(), anyString())).thenReturn(0);

        eventProgressMockedStatic = Mockito.mockStatic(EventProgress.class);

        mockMiniatureRenderer = mock(MiniatureMapView.class);

        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxJavaPlugins.setComputationSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());

        when(mockGpxFileInfo1.file()).thenReturn(mockFile1);
        when(mockFile1.getName()).thenReturn("file1.gpx");
        when(mockGpxFileInfo2.file()).thenReturn(mockFile2);
        when(mockFile2.getName()).thenReturn("file2.gpx");

        initialList = List.of(mockGpxFileInfo1, mockGpxFileInfo2);

        when(mockGpxFileInfoProvider.searchAndParseGpxFilesRecursively(mockContext))
                .thenReturn(Single.just(initialList));

        when(mockMiniatureProvider.requestForGenerateMiniature(eq(mockMiniatureRenderer), any(GpxFileInfo.class)))
                .thenReturn(Completable.complete());
        when(mockMiniatureProvider.getGpxFileInfoWithMiniature())
                .thenReturn(Observable.just(mockGpxFileInfo1, mockGpxFileInfo2));

        eventProgressMockedStatic.when(() -> EventProgress.create(eq(GpxFileInfoMiniatureProvider.class), eq(0), eq(2))).thenReturn(mockProgressInitial);
        eventProgressMockedStatic.when(() -> EventProgress.create(eq(GpxFileInfoMiniatureProvider.class), eq(1), eq(2))).thenReturn(mockProgress1);
        eventProgressMockedStatic.when(() -> EventProgress.create(eq(GpxFileInfoMiniatureProvider.class), eq(2), eq(2))).thenReturn(mockProgress2);

        when(mockGlobalEventWrapper.onNextChanged(any(), any())).thenAnswer(inv -> inv.getArgument(1));

        when(mockGpxFileInfoProvider.replaceAll(anyList())).thenReturn(Completable.complete());
    }

    @Test
    public void updateAndGenerateMiniatures_successPath_searchesGeneratesReplacesAndReportsProgress() {
        TestObserver<Void> testObserver = updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(mockContext, mockMiniatureRenderer).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        InOrder inOrder = inOrder(mockGpxFileInfoProvider, mockMiniatureProvider, mockGlobalEventWrapper);

        inOrder.verify(mockGpxFileInfoProvider).searchAndParseGpxFilesRecursively(mockContext);

        inOrder.verify(mockGlobalEventWrapper).onNext(progressCaptor.capture());
        assertEquals(mockProgressInitial, progressCaptor.getValue());

        inOrder.verify(mockMiniatureProvider).requestForGenerateMiniature(mockMiniatureRenderer, mockGpxFileInfo1);
        inOrder.verify(mockGlobalEventWrapper).onNextChanged(mockProgressInitial, mockProgress1);

        inOrder.verify(mockMiniatureProvider).requestForGenerateMiniature(mockMiniatureRenderer, mockGpxFileInfo2);
        inOrder.verify(mockGlobalEventWrapper).onNextChanged(mockProgress1, mockProgress2);

        inOrder.verify(mockGpxFileInfoProvider).replaceAll(listCaptor.capture());
        assertEquals(initialList, listCaptor.getValue());

        inOrder.verifyNoMoreInteractions();
        
        verify(mockMiniatureProvider, times(2)).getGpxFileInfoWithMiniature();
    }

    @Test
    public void updateAndGenerateMiniatures_whenSearchReturnsEmpty_completesWithoutProcessing() {
        when(mockGpxFileInfoProvider.searchAndParseGpxFilesRecursively(mockContext)).thenReturn(Single.just(Collections.emptyList()));

        TestObserver<Void> testObserver = updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(mockContext, mockMiniatureRenderer).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(mockGpxFileInfoProvider).searchAndParseGpxFilesRecursively(mockContext);
        verifyNoInteractions(mockMiniatureProvider, mockGlobalEventWrapper);
        verify(mockGpxFileInfoProvider, never()).replaceAll(anyList());
    }

    @Test
    public void updateAndGenerateMiniatures_whenSearchErrors_propagatesError() {
        Throwable searchError = new RuntimeException("Search failed");
        when(mockGpxFileInfoProvider.searchAndParseGpxFilesRecursively(mockContext)).thenReturn(Single.error(searchError));

        TestObserver<Void> testObserver = updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(mockContext, mockMiniatureRenderer).test();

        testObserver.assertError(searchError);

        verify(mockGpxFileInfoProvider).searchAndParseGpxFilesRecursively(mockContext);
        verifyNoInteractions(mockMiniatureProvider, mockGlobalEventWrapper);
        verify(mockGpxFileInfoProvider, never()).replaceAll(anyList());
    }

    @Test
    public void updateAndGenerateMiniatures_whenMiniatureRequestErrors_propagatesError() {
        Throwable miniatureError = new RuntimeException("Miniature request failed");
        when(mockMiniatureProvider.requestForGenerateMiniature(mockMiniatureRenderer, mockGpxFileInfo1)).thenReturn(Completable.error(miniatureError));

        TestObserver<Void> testObserver = updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(mockContext, mockMiniatureRenderer).test();

        testObserver.assertError(miniatureError);

        InOrder inOrder = inOrder(mockGpxFileInfoProvider, mockMiniatureProvider, mockGlobalEventWrapper);
        inOrder.verify(mockGpxFileInfoProvider).searchAndParseGpxFilesRecursively(mockContext);
        inOrder.verify(mockGlobalEventWrapper).onNext(mockProgressInitial);
        inOrder.verify(mockMiniatureProvider).requestForGenerateMiniature(mockMiniatureRenderer, mockGpxFileInfo1);

        inOrder.verify(mockMiniatureProvider, never()).requestForGenerateMiniature(mockMiniatureRenderer, mockGpxFileInfo2);
        inOrder.verify(mockGpxFileInfoProvider, never()).replaceAll(anyList());
    }

    @Test
    public void updateAndGenerateMiniatures_whenMiniatureResultErrors_propagatesError() {
        Throwable resultError = new RuntimeException("Miniature result failed");
        when(mockMiniatureProvider.getGpxFileInfoWithMiniature()).thenReturn(Observable.error(resultError));

        TestObserver<Void> testObserver = updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(mockContext, mockMiniatureRenderer).test();

        testObserver.assertError(resultError);

        InOrder inOrder = inOrder(mockGpxFileInfoProvider, mockMiniatureProvider, mockGlobalEventWrapper);
        inOrder.verify(mockGpxFileInfoProvider).searchAndParseGpxFilesRecursively(mockContext);
        inOrder.verify(mockGlobalEventWrapper).onNext(mockProgressInitial);
        inOrder.verify(mockMiniatureProvider).requestForGenerateMiniature(mockMiniatureRenderer, mockGpxFileInfo1);
        inOrder.verify(mockMiniatureProvider).getGpxFileInfoWithMiniature();

        inOrder.verify(mockMiniatureProvider, never()).requestForGenerateMiniature(mockMiniatureRenderer, mockGpxFileInfo2);
        inOrder.verify(mockGpxFileInfoProvider, never()).replaceAll(anyList());
    }

    @Test
    public void updateAndGenerateMiniatures_whenReplaceAllErrors_propagatesError() {
        Throwable replaceError = new RuntimeException("Replace failed");
        when(mockGpxFileInfoProvider.replaceAll(anyList())).thenReturn(Completable.error(replaceError));

        TestObserver<Void> testObserver = updateGpxFileInfoListUseCase.updateAndGenerateMiniatures(mockContext, mockMiniatureRenderer).test();

        testObserver.assertError(replaceError);

        InOrder inOrder = inOrder(mockGpxFileInfoProvider, mockMiniatureProvider, mockGlobalEventWrapper);
        inOrder.verify(mockGpxFileInfoProvider).searchAndParseGpxFilesRecursively(mockContext);
        inOrder.verify(mockGlobalEventWrapper).onNext(mockProgressInitial);
        inOrder.verify(mockMiniatureProvider).requestForGenerateMiniature(mockMiniatureRenderer, mockGpxFileInfo1);
        inOrder.verify(mockGlobalEventWrapper).onNextChanged(mockProgressInitial, mockProgress1);
        inOrder.verify(mockMiniatureProvider).requestForGenerateMiniature(mockMiniatureRenderer, mockGpxFileInfo2);
        inOrder.verify(mockGlobalEventWrapper).onNextChanged(mockProgress1, mockProgress2);
        inOrder.verify(mockGpxFileInfoProvider).replaceAll(listCaptor.capture());
        assertEquals(initialList, listCaptor.getValue());
    }

    @After
    public void tearDown() {
        RxJavaPlugins.reset();
        RxAndroidPlugins.reset();
        if (eventProgressMockedStatic != null) {
            eventProgressMockedStatic.close();
        }
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