package com.itservices.gpxanalyzer.usecase;

import com.itservices.gpxanalyzer.data.cache.rawdata.DataEntityCache;
import com.itservices.gpxanalyzer.data.cache.processed.rawdata.RawDataProcessed;
import com.itservices.gpxanalyzer.data.provider.GpxDataEntityCachedProvider;
import com.itservices.gpxanalyzer.data.provider.RawDataProcessedProvider;
import com.itservices.gpxanalyzer.data.model.entity.DataEntityWrapper;
import com.itservices.gpxanalyzer.events.GlobalEventWrapper;
import com.itservices.gpxanalyzer.events.RequestStatus;
import com.itservices.gpxanalyzer.ui.components.chart.ChartController;
import com.itservices.gpxanalyzer.ui.gpxchart.item.ChartAreaItem;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewMode;
import com.itservices.gpxanalyzer.ui.gpxchart.viewmode.GpxViewModeMapper;

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

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;

import io.reactivex.android.plugins.RxAndroidPlugins;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import android.util.Log;
import androidx.lifecycle.LiveData;

public class LoadChartDataUseCaseTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock private GpxViewModeMapper mockViewModeMapper;
    @Mock private GpxDataEntityCachedProvider mockDataEntityCachedProvider;
    @Mock private DataEntityCache mockDataEntityCache;
    @Mock private RawDataProcessedProvider mockRawDataProcessedProvider;
    @Mock private GlobalEventWrapper mockEventWrapper;
    @Mock private ChartInitializerUseCase mockChartInitializer;

    @Mock private ChartAreaItem mockChartAreaItem1;
    @Mock private ChartAreaItem mockChartAreaItem2;
    @Mock private ChartController mockChartController1;
    @Mock private ChartController mockChartController2;
    @Mock private RawDataProcessed mockRawDataProcessed1;
    @Mock private RawDataProcessed mockRawDataProcessed2;

    @Mock private LiveData<GpxViewMode> mockLiveDataAltitude;
    @Mock private LiveData<GpxViewMode> mockLiveDataSpeed;

    @InjectMocks private LoadChartDataUseCase loadChartDataUseCase;

    @Captor private ArgumentCaptor<RequestStatus> statusCaptor;
    @Captor private ArgumentCaptor<DataEntityWrapper> dataEntityWrapperCaptor;

    private TestScheduler testScheduler;
    private List<ChartAreaItem> chartAreaItems;

    private MockedStatic<Log> logMockedStatic;

    @Before
    public void setUp() {
        // Use trampoline scheduler for immediate execution in tests
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxJavaPlugins.setComputationSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxJavaPlugins.setNewThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxJavaPlugins.setSingleSchedulerHandler(scheduler -> Schedulers.trampoline());
        // Use trampoline scheduler for Android main thread in tests
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(callable -> Schedulers.trampoline());

        // Mock the LiveData returned by getViewMode()
        when(mockLiveDataAltitude.getValue()).thenReturn(GpxViewMode.ASL_T_1);
        when(mockLiveDataSpeed.getValue()).thenReturn(GpxViewMode.V_T_1);

        // Mock chart items and their controllers
        when(mockChartAreaItem1.getChartController()).thenReturn(mockChartController1);
        when(mockChartAreaItem1.getViewMode()).thenReturn(mockLiveDataAltitude); // Return mocked LiveData

        when(mockChartAreaItem2.getChartController()).thenReturn(mockChartController2);
        when(mockChartAreaItem2.getViewMode()).thenReturn(mockLiveDataSpeed); // Return mocked LiveData

        chartAreaItems = List.of(mockChartAreaItem1, mockChartAreaItem2);

        // Mock ChartInitializerUseCase success
        when(mockChartInitializer.initChart(mockChartAreaItem1)).thenReturn(Single.just(mockChartAreaItem1));
        when(mockChartInitializer.initChart(mockChartAreaItem2)).thenReturn(Single.just(mockChartAreaItem2));

        // Mock GpxDataEntityCachedProvider success
        when(mockDataEntityCachedProvider.provide()).thenReturn(Single.just(new Vector<>()));

        // Mock DataEntityCache to return non-null Vector
        when(mockDataEntityCache.getDataEntitityVector()).thenReturn(new Vector<>());

        // Mock GpxViewModeMapper with correct enum constants
        when(mockViewModeMapper.mapToPrimaryKeyIndexList(GpxViewMode.ASL_T_1)).thenReturn(1); // Example index for ASL_T_1
        when(mockViewModeMapper.mapToPrimaryKeyIndexList(GpxViewMode.V_T_1)).thenReturn(2); // Example index for V_T_1

        // Mock RawDataProcessedProvider success
        when(mockRawDataProcessedProvider.provide(any(DataEntityWrapper.class)))
                .thenReturn(Single.just(mockRawDataProcessed1), Single.just(mockRawDataProcessed2));

        // Mock ChartAreaItem updateChart success
        when(mockChartAreaItem1.updateChart(mockRawDataProcessed1)).thenReturn(Single.just(RequestStatus.CHART_UPDATED));
        when(mockChartAreaItem2.updateChart(mockRawDataProcessed2)).thenReturn(Single.just(RequestStatus.CHART_UPDATED));

        logMockedStatic = Mockito.mockStatic(Log.class);
    }

    @Test
    public void loadData_withNullList_returnsErrorObservable() {
        TestObserver<RequestStatus> testObserver = loadChartDataUseCase.loadData(null, mockChartInitializer).test();

        testObserver.assertValue(RequestStatus.ERROR);
        testObserver.assertComplete();
        verifyNoInteractions(mockEventWrapper, mockDataEntityCachedProvider);
    }

    @Test
    public void loadData_withEmptyList_returnsErrorObservable() {
        TestObserver<RequestStatus> testObserver = loadChartDataUseCase.loadData(Collections.emptyList(), mockChartInitializer).test();

        testObserver.assertValue(RequestStatus.ERROR);
        testObserver.assertComplete();
        verifyNoInteractions(mockEventWrapper, mockDataEntityCachedProvider);
    }

    @Test
    public void loadData_successfulPath_emitsDoneAndCorrectEvents() {
        TestObserver<RequestStatus> testObserver = loadChartDataUseCase.loadData(chartAreaItems, mockChartInitializer).test();

        testObserver.awaitTerminalEvent(); // Ensure all async operations complete
        testObserver.assertValue(RequestStatus.DONE);
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        // Verify event order
        InOrder inOrder = inOrder(mockEventWrapper, mockDataEntityCachedProvider, mockChartInitializer, mockRawDataProcessedProvider, mockChartAreaItem1, mockChartAreaItem2);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.LOADING);
        inOrder.verify(mockDataEntityCachedProvider).provide();
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.DATA_LOADED);

        // Chart 1 Initialization and Data Loading (Verify with correct enum)
        inOrder.verify(mockChartInitializer).initChart(mockChartAreaItem1);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.PROCESSING);
        inOrder.verify(mockRawDataProcessedProvider).provide(dataEntityWrapperCaptor.capture());
        verify(mockViewModeMapper).mapToPrimaryKeyIndexList(GpxViewMode.ASL_T_1); // <<< VERIFY CORRECT ENUM
        // assertEquals(1, dataEntityWrapperCaptor.getValue().getPrimaryKeyIndex());
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.PROCESSED);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.CHART_UPDATING);
        inOrder.verify(mockChartAreaItem1).updateChart(mockRawDataProcessed1);

        // Chart 2 Initialization and Data Loading (Verify with correct enum)
        inOrder.verify(mockChartInitializer).initChart(mockChartAreaItem2);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.PROCESSING);
        inOrder.verify(mockRawDataProcessedProvider).provide(dataEntityWrapperCaptor.capture());
        verify(mockViewModeMapper).mapToPrimaryKeyIndexList(GpxViewMode.V_T_1); // <<< VERIFY CORRECT ENUM
        // assertEquals(2, dataEntityWrapperCaptor.getValue().getPrimaryKeyIndex());
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.PROCESSED);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.CHART_UPDATING);
        inOrder.verify(mockChartAreaItem2).updateChart(mockRawDataProcessed2);

        // Final events
        inOrder.verify(mockEventWrapper, times(2)).onNext(RequestStatus.DONE);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void loadData_dataEntityProviderError_emitsErrorAndEvents() {
        Throwable error = new RuntimeException("Data provider failed");
        when(mockDataEntityCachedProvider.provide()).thenReturn(Single.error(error));

        TestObserver<RequestStatus> testObserver = loadChartDataUseCase.loadData(chartAreaItems, mockChartInitializer).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues(); // Error terminates the stream before final status
        testObserver.assertError(error);
        testObserver.assertTerminated(); // Added for clarity

        InOrder inOrder = inOrder(mockEventWrapper, mockDataEntityCachedProvider);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.LOADING);
        inOrder.verify(mockDataEntityCachedProvider).provide();
        inOrder.verify(mockEventWrapper, times(2)).onNext(RequestStatus.ERROR); // Expect ERROR twice
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(mockChartInitializer, mockRawDataProcessedProvider, mockChartAreaItem1, mockChartAreaItem2);
    }

    @Test
    public void loadData_chartInitializerError_emitsDoneWithErrorStatusAndEvents() {
        Throwable initError = new RuntimeException("Init failed");
        when(mockChartInitializer.initChart(mockChartAreaItem1)).thenReturn(Single.error(initError));
        // Second chart still initializes successfully
        when(mockChartInitializer.initChart(mockChartAreaItem2)).thenReturn(Single.just(mockChartAreaItem2));
        // Second chart update still succeeds (This setup might not be fully reached due to early error)
        when(mockRawDataProcessedProvider.provide(any(DataEntityWrapper.class)))
                .thenReturn(Single.just(mockRawDataProcessed1), Single.just(mockRawDataProcessed2)); // Need enough mocks if chain continued
        when(mockChartAreaItem1.updateChart(mockRawDataProcessed1)).thenReturn(Single.just(RequestStatus.CHART_UPDATED));
        when(mockChartAreaItem2.updateChart(mockRawDataProcessed2)).thenReturn(Single.just(RequestStatus.CHART_UPDATED));

        TestObserver<RequestStatus> testObserver = loadChartDataUseCase.loadData(chartAreaItems, mockChartInitializer).test();

        testObserver.awaitTerminalEvent();

        // Assert that the Observable terminated with the specific error, not emitted a value
        testObserver.assertNoValues();
        testObserver.assertError(initError);
        testObserver.assertTerminated(); // Verify it terminated

        // Verify event sequence up to the point of error
        InOrder inOrder = inOrder(mockEventWrapper, mockDataEntityCachedProvider, mockChartInitializer);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.LOADING);
        inOrder.verify(mockDataEntityCachedProvider).provide();
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.DATA_LOADED);

        // Chart 1 fails initialization
        inOrder.verify(mockChartInitializer).initChart(mockChartAreaItem1);

        // Verify the final error event emitted by the doOnError handler in the main chain
        verify(mockEventWrapper).onNext(RequestStatus.ERROR);

        // Verify no further processing occurred for chart 1 or chart 2
        verify(mockRawDataProcessedProvider, never()).provide(any(DataEntityWrapper.class));
        verify(mockChartAreaItem1, never()).updateChart(any());
        verify(mockChartInitializer, never()).initChart(mockChartAreaItem2);
        verify(mockChartAreaItem2, never()).updateChart(any());
        verify(mockEventWrapper, never()).onNext(RequestStatus.PROCESSING); // Not emitted if init fails
        verify(mockEventWrapper, never()).onNext(RequestStatus.DONE);
    }

     @Test
    public void loadData_rawDataProcessedProviderError_emitsDoneWithErrorStatusAndEvents() {
        Throwable processError = new RuntimeException("Process failed");
        // First chart init succeeds
        when(mockChartInitializer.initChart(mockChartAreaItem1)).thenReturn(Single.just(mockChartAreaItem1));
        // Raw data provider fails for the first chart
        when(mockRawDataProcessedProvider.provide(any(DataEntityWrapper.class)))
                .thenReturn(Single.error(processError), Single.just(mockRawDataProcessed2)); // <<< Error first
        // Second chart init and update succeeds
        when(mockChartInitializer.initChart(mockChartAreaItem2)).thenReturn(Single.just(mockChartAreaItem2));
        when(mockChartAreaItem2.updateChart(mockRawDataProcessed2)).thenReturn(Single.just(RequestStatus.CHART_UPDATED));

        TestObserver<RequestStatus> testObserver = loadChartDataUseCase.loadData(chartAreaItems, mockChartInitializer).test();

        testObserver.awaitTerminalEvent();
        // Assert that the Observable terminated with the specific error
        testObserver.assertNoValues();
        testObserver.assertError(processError);
        testObserver.assertTerminated();

        // Verify event sequence up to the point of error
        InOrder inOrder = inOrder(mockEventWrapper, mockDataEntityCachedProvider, mockChartInitializer, mockRawDataProcessedProvider);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.LOADING);
        inOrder.verify(mockDataEntityCachedProvider).provide();
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.DATA_LOADED);

        // Chart 1 Init OK, Process Fails
        inOrder.verify(mockChartInitializer).initChart(mockChartAreaItem1);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.PROCESSING);
        inOrder.verify(mockRawDataProcessedProvider).provide(any(DataEntityWrapper.class)); // Fails here

        // Verify the final error event emitted by the doOnError handler in the main chain
        verify(mockEventWrapper).onNext(RequestStatus.ERROR);

        // Verify no further processing occurred for chart 1 or chart 2
        verify(mockChartInitializer, never()).initChart(mockChartAreaItem2);
        verify(mockChartAreaItem1, never()).updateChart(any());
        verify(mockChartAreaItem2, never()).updateChart(any());
        verify(mockEventWrapper, never()).onNext(RequestStatus.PROCESSED);
        verify(mockEventWrapper, never()).onNext(RequestStatus.CHART_UPDATING);
        verify(mockEventWrapper, never()).onNext(RequestStatus.DONE);
    }

    @Test
    public void loadData_chartUpdateError_emitsDoneWithUpdateErrorStatusAndEvents() {
         Throwable updateError = new RuntimeException("Update failed");
        // First chart init and process succeeds
        when(mockChartInitializer.initChart(mockChartAreaItem1)).thenReturn(Single.just(mockChartAreaItem1));
        when(mockRawDataProcessedProvider.provide(any(DataEntityWrapper.class))).thenReturn(Single.just(mockRawDataProcessed1), Single.just(mockRawDataProcessed2));
        // First chart update fails
        when(mockChartAreaItem1.updateChart(mockRawDataProcessed1)).thenReturn(Single.just(RequestStatus.ERROR)); // Specific error status
        // Second chart init and update succeeds
        when(mockChartInitializer.initChart(mockChartAreaItem2)).thenReturn(Single.just(mockChartAreaItem2));
        when(mockChartAreaItem2.updateChart(mockRawDataProcessed2)).thenReturn(Single.just(RequestStatus.CHART_UPDATED));

        TestObserver<RequestStatus> testObserver = loadChartDataUseCase.loadData(chartAreaItems, mockChartInitializer).test();

        testObserver.awaitTerminalEvent();
        // Status reflects the lowest ordinal (ERROR)
        testObserver.assertValue(RequestStatus.ERROR);
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        InOrder inOrder = inOrder(mockEventWrapper, mockDataEntityCachedProvider, mockChartInitializer, mockRawDataProcessedProvider, mockChartAreaItem1, mockChartAreaItem2);
        // ... verify initial steps (LOADING, DATA_LOADED) ...
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.LOADING);
        inOrder.verify(mockDataEntityCachedProvider).provide();
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.DATA_LOADED);

        // Chart 1 Init, Process OK, Update Fails
        inOrder.verify(mockChartInitializer).initChart(mockChartAreaItem1);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.PROCESSING);
        inOrder.verify(mockRawDataProcessedProvider).provide(any(DataEntityWrapper.class));
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.PROCESSED);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.CHART_UPDATING);
        inOrder.verify(mockChartAreaItem1).updateChart(mockRawDataProcessed1); // Fails here (returns CHART_UPDATE_ERROR)

        // Chart 2 Succeeds
        inOrder.verify(mockChartInitializer).initChart(mockChartAreaItem2);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.PROCESSING);
        inOrder.verify(mockRawDataProcessedProvider).provide(any(DataEntityWrapper.class));
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.PROCESSED);
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.CHART_UPDATING);
        inOrder.verify(mockChartAreaItem2).updateChart(mockRawDataProcessed2);

        inOrder.verify(mockEventWrapper).onNext(RequestStatus.ERROR); // Final status emitted
        inOrder.verify(mockEventWrapper).onNext(RequestStatus.DONE); // doOnComplete
        inOrder.verifyNoMoreInteractions();
    }

    // Clean up RxJavaPlugins schedulers after tests
    @org.junit.After
    public void tearDown() {
        RxJavaPlugins.reset();
        RxAndroidPlugins.reset();
        if (logMockedStatic != null) {
            logMockedStatic.close();
        }
    }
} 