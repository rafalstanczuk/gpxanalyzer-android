package com.itservices.gpxanalyzer.feature.gpxchart.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.reactivex.Single;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.itservices.gpxanalyzer.core.events.RequestStatus;
import com.itservices.gpxanalyzer.core.ui.components.chart.ChartController;
import com.itservices.gpxanalyzer.feature.gpxchart.ui.item.ChartAreaItem;

public class ChartInitializerUseCaseTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ChartAreaItem mockChartAreaItem;

    @Mock
    private ChartController mockChartController;

    @InjectMocks
    private ChartInitializerUseCase chartInitializerUseCase;

    @Before
    public void setUp() {
        // Link the mock controller to the mock item
        when(mockChartAreaItem.getChartController()).thenReturn(mockChartController);
    }

    @Test
    public void initChart_shouldCallControllerInitAndReturnItemOnSuccess() {
        // Arrange: Configure the mock controller to return success status
        // Use RequestStatus.DONE or another appropriate success status
        Single<RequestStatus> successSingle = Single.just(RequestStatus.DONE);
        when(mockChartController.initChart()).thenReturn(successSingle);

        // Act: Call the use case method
        Single<ChartAreaItem> resultSingle = chartInitializerUseCase.initChart(mockChartAreaItem);

        // Assert: Verify the controller's initChart was called
        verify(mockChartController).initChart();

        // Assert: Verify the result Single emits the original item and completes
        resultSingle.test()
                .assertValue(mockChartAreaItem)
                .assertComplete();
    }

    @Test
    public void initChart_shouldPropagateErrorFromController() {
        // Arrange: Configure the mock controller to return an error
        Throwable expectedError = new RuntimeException("Initialization failed");
        // The error type remains Single<RequestStatus>
        Single<RequestStatus> errorSingle = Single.error(expectedError);
        when(mockChartController.initChart()).thenReturn(errorSingle);

        // Act: Call the use case method
        Single<ChartAreaItem> resultSingle = chartInitializerUseCase.initChart(mockChartAreaItem);

        // Assert: Verify the controller's initChart was called
        verify(mockChartController).initChart();

        // Assert: Verify the result Single emits the expected error
        resultSingle.test()
                .assertError(expectedError);
    }
} 