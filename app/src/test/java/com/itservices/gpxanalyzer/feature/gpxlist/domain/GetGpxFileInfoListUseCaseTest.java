package com.itservices.gpxanalyzer.feature.gpxlist.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.location.Location;

import com.itservices.gpxanalyzer.feature.gpxlist.data.model.gpxfileinfo.GpxFileInfo;
import com.itservices.gpxanalyzer.feature.gpxlist.data.provider.GpxFileInfoProvider;

public class GetGpxFileInfoListUseCaseTest {

    // Rule to enable Mockito annotations
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    // Mock the dependency
    @Mock
    private GpxFileInfoProvider mockGpxFileInfoProvider;

    // Inject the mock into the class under test
    @InjectMocks
    private GetGpxFileInfoListUseCase getGpxFileInfoListUseCase;

    @Before
    public void setUp() {
        // No setup needed here anymore
    }

    @Test
    public void getGpxFileInfoList_shouldCallProviderAndReturnList() {
        // Arrange: Prepare mock File objects
        File mockFile1 = mock(File.class);
        when(mockFile1.getPath()).thenReturn("path/to/file1.gpx");
        when(mockFile1.getName()).thenReturn("file1.gpx");
        when(mockFile1.length()).thenReturn(1000L);
        when(mockFile1.lastModified()).thenReturn(System.currentTimeMillis() - 10000);

        File mockFile2 = mock(File.class);
        when(mockFile2.getPath()).thenReturn("path/to/file2.gpx");
        when(mockFile2.getName()).thenReturn("file2.gpx");
        when(mockFile2.length()).thenReturn(2000L);
        when(mockFile2.lastModified()).thenReturn(System.currentTimeMillis() - 20000);

        Location location1 = new Location("test");
        location1.setLatitude(0.0);
        location1.setLongitude(0.0);
        location1.setAltitude(10.0);
        location1.setTime(System.currentTimeMillis() - 5000);

        Location location2 = new Location("test");
        location2.setLatitude(1.0);
        location2.setLongitude(1.0);
        location2.setAltitude(20.0);
        location2.setTime(System.currentTimeMillis() - 15000);

        // Arrange: Prepare mock data locally using the correct constructor
        List<GpxFileInfo> mockFileInfoList = Arrays.asList(
                new GpxFileInfo(1L, mockFile1, "Creator1", "Author1", location1, ""),
                new GpxFileInfo(2L, mockFile2, "Creator2", "Author2", location2, "")
        );

        // Arrange: Configure the mock provider to return a specific Single
        Single<List<GpxFileInfo>> expectedSingle = Single.just(mockFileInfoList);
        when(mockGpxFileInfoProvider.getCached()).thenReturn(expectedSingle);

        // Act: Call the method under test
        Single<List<GpxFileInfo>> actualSingle = getGpxFileInfoListUseCase.getGpxFileInfoList();

        // Assert: Verify the provider method was called
        verify(mockGpxFileInfoProvider).getCached();

        // Assert: Verify the returned Single emits the expected list
        actualSingle.test()
                .assertValue(mockFileInfoList)
                .assertComplete();
    }

    @Test
    public void getGpxFileInfoList_whenProviderReturnsEmptyList_shouldReturnEmptyList() {
        // Arrange: Configure the mock provider to return an empty list
        Single<List<GpxFileInfo>> expectedSingle = Single.just(Collections.emptyList());
        when(mockGpxFileInfoProvider.getCached()).thenReturn(expectedSingle);

        // Act: Call the method under test
        Single<List<GpxFileInfo>> actualSingle = getGpxFileInfoListUseCase.getGpxFileInfoList();

        // Assert: Verify the provider method was called
        verify(mockGpxFileInfoProvider).getCached();

        // Assert: Verify the returned Single emits an empty list
        actualSingle.test()
                .assertValue(Collections.emptyList())
                .assertComplete();
    }

     @Test
    public void getGpxFileInfoList_whenProviderReturnsError_shouldPropagateError() {
        // Arrange: Configure the mock provider to return an error
        Throwable expectedError = new RuntimeException("Provider error");
        Single<List<GpxFileInfo>> expectedSingle = Single.error(expectedError);
        when(mockGpxFileInfoProvider.getCached()).thenReturn(expectedSingle);

        // Act: Call the method under test
        Single<List<GpxFileInfo>> actualSingle = getGpxFileInfoListUseCase.getGpxFileInfoList();

        // Assert: Verify the provider method was called
        verify(mockGpxFileInfoProvider).getCached();

        // Assert: Verify the returned Single emits the expected error
        actualSingle.test()
                .assertError(expectedError);
    }
} 