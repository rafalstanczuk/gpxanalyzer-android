package com.itservices.gpxanalyzer.feature.gpxchart.domain;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.disposables.Disposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

import android.util.Log;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.itservices.gpxanalyzer.core.events.RequestStatus;
import com.itservices.gpxanalyzer.core.utils.common.ConcurrentUtil;
import com.itservices.gpxanalyzer.feature.gpxchart.ui.item.ChartAreaItem;

public class MultipleSyncedGpxChartUseCaseTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock private LoadChartDataUseCase mockChartDataLoader;
    @Mock private ChartInitializerUseCase mockChartInitializer; // Although injected, not directly used by this class's logic, but needed for constructor
    @Mock private ChartAreaItem mockChartAreaItem1;
    @Mock private ChartAreaItem mockChartAreaItem2;
    @Mock private Disposable mockDisposable1;
    @Mock private Disposable mockDisposable2;

    @InjectMocks private MultipleSyncedGpxChartUseCase multipleSyncedGpxChartUseCase;

    private List<ChartAreaItem> chartAreaItems;
    private MockedStatic<ConcurrentUtil> concurrentUtilMockedStatic;
    private MockedStatic<Log> logMockedStatic;

    @Before
    public void setUp() {
        // Setup static mocks first
        concurrentUtilMockedStatic = Mockito.mockStatic(ConcurrentUtil.class);
        logMockedStatic = Mockito.mockStatic(Log.class);
        logMockedStatic.when(() -> Log.d(anyString(), anyString())).thenReturn(0);
        logMockedStatic.when(() -> Log.e(anyString(), anyString(), any(Throwable.class))).thenReturn(0);
        logMockedStatic.when(() -> Log.i(anyString(), anyString())).thenReturn(0);
        logMockedStatic.when(() -> Log.w(anyString(), anyString())).thenReturn(0);

        // --- Setup test data ---
        chartAreaItems = List.of(mockChartAreaItem1, mockChartAreaItem2);

        // --- Configure RxJava Schedulers --- 
        // Use trampoline scheduler for immediate execution in tests
        Scheduler immediate = Schedulers.trampoline();
        RxJavaPlugins.setInitIoSchedulerHandler(scheduler -> immediate);
        RxJavaPlugins.setInitComputationSchedulerHandler(scheduler -> immediate);
        RxJavaPlugins.setInitNewThreadSchedulerHandler(scheduler -> immediate);
        RxJavaPlugins.setInitSingleSchedulerHandler(scheduler -> immediate);
        // Only mock the main thread scheduler - Revert this
        // RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> immediate);

        // --- Mock other dependencies ---
        // Mock the underlying loadData call
        when(mockChartDataLoader.loadData(anyList(), eq(mockChartInitializer)))
                .thenReturn(Observable.just(RequestStatus.DONE)
                    .doOnError(e -> System.err.println("Caught test error: " + e.getMessage())));
    }

    @Test
    public void loadData_withValidList_callsLoaderAndStoresDisposable() {
        multipleSyncedGpxChartUseCase.loadData(chartAreaItems);

        // Verify loader was called
        verify(mockChartDataLoader).loadData(eq(chartAreaItems), eq(mockChartInitializer));

        // Verify a disposable is stored (don't assert specific mock instance)
        assertNotNull(multipleSyncedGpxChartUseCase.loadDataDisposable);

        // Verify tryToDispose wasn't called for the actual loadDataDisposable yet (only potentially for initial null)
        concurrentUtilMockedStatic.verify(() -> ConcurrentUtil.tryToDispose(null), times(1)); // For initial state
        concurrentUtilMockedStatic.verify(() -> ConcurrentUtil.tryToDispose(multipleSyncedGpxChartUseCase.loadDataDisposable), never());
    }

    @Test
    public void loadData_calledTwice_disposesPreviousDisposable() {
        // First call - let it store the actual disposable from subscribe()
        multipleSyncedGpxChartUseCase.loadData(chartAreaItems);
        Disposable firstDisposable = multipleSyncedGpxChartUseCase.loadDataDisposable;
        assertNotNull(firstDisposable); // Ensure a disposable was stored

        // Second call
        multipleSyncedGpxChartUseCase.loadData(chartAreaItems);
        Disposable secondDisposable = multipleSyncedGpxChartUseCase.loadDataDisposable;
        assertNotNull(secondDisposable);
        // Verify the first disposable is not the same as the second one
        assertNotEquals(firstDisposable, secondDisposable);

        // Verify tryToDispose was called with the first disposable before the second call
        concurrentUtilMockedStatic.verify(() -> ConcurrentUtil.tryToDispose(firstDisposable), times(1));

        // Verify loader was called twice
        verify(mockChartDataLoader, times(2)).loadData(eq(chartAreaItems), eq(mockChartInitializer));
    }

    @Test
    public void loadData_withNullList_doesNothing() {
        multipleSyncedGpxChartUseCase.loadData(null);
        verifyNoInteractions(mockChartDataLoader);
        concurrentUtilMockedStatic.verifyNoInteractions();
        assertNull(multipleSyncedGpxChartUseCase.loadDataDisposable);
    }

    @Test
    public void loadData_withEmptyList_doesNothing() {
        multipleSyncedGpxChartUseCase.loadData(Collections.emptyList());
        verifyNoInteractions(mockChartDataLoader);
        concurrentUtilMockedStatic.verifyNoInteractions();
        assertNull(multipleSyncedGpxChartUseCase.loadDataDisposable);
    }

    @Test
    public void disposeAll_callsTryToDisposeWithStoredDisposable() {
        // Load data first to store a disposable
        multipleSyncedGpxChartUseCase.loadData(chartAreaItems);
        Disposable storedDisposable = multipleSyncedGpxChartUseCase.loadDataDisposable;
        assertNotNull(storedDisposable);

        // Call disposeAll
        multipleSyncedGpxChartUseCase.disposeAll();

        // Verify tryToDispose was called with the stored disposable
        concurrentUtilMockedStatic.verify(() -> ConcurrentUtil.tryToDispose(storedDisposable), times(1));
    }

    @Test
    public void disposeAll_whenNoDisposableStored_callsTryToDisposeWithNull() {
        // Call disposeAll without loading data first
        multipleSyncedGpxChartUseCase.disposeAll();

        // Verify tryToDispose was called with null
        concurrentUtilMockedStatic.verify(() -> ConcurrentUtil.tryToDispose(null), times(1));
    }


    @After
    public void tearDown() {
        RxJavaPlugins.reset();
        RxAndroidPlugins.reset();
        multipleSyncedGpxChartUseCase.disposeAll(); // Ensure disposables are cleared
        concurrentUtilMockedStatic.close();
        if (logMockedStatic != null) {
             logMockedStatic.close();
        }
        multipleSyncedGpxChartUseCase.loadDataDisposable = null;
    }

    // Helper to assertNotNull without adding JUnit dependency if not present
    private static void assertNotNull(Object object) {
        if (object == null) {
            throw new AssertionError("Expected object to be not null");
        }
    }
     // Helper to assertNull without adding JUnit dependency if not present
    private static void assertNull(Object object) {
        if (object != null) {
            throw new AssertionError("Expected object to be null");
        }
    }
     // Helper to assertEquals without adding JUnit dependency if not present
    private static void assertEquals(Object expected, Object actual) {
        if ((expected == null && actual != null) || (expected != null && !expected.equals(actual))) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }
    // Helper to assertNotEquals without adding JUnit dependency if not present
    private static void assertNotEquals(Object unexpected, Object actual) {
        if ((unexpected == null && actual == null) || (unexpected != null && unexpected.equals(actual))) {
            throw new AssertionError("Expected objects to be different, but both were " + actual);
        }
    }
} 