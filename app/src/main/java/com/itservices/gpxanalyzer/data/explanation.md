# Data Package Explanation

This package is responsible for all data handling in the GpxAnalyzer application. It includes data sources (network, database, file system), data models, mappers, repositories, and caching mechanisms.

## Main Sub-Packages and their Roles

*   **`provider`**: Contains repository implementations and data providers that abstract the origin of the data (e.g., network, local database, file).
    *   **`altitude`**: Specific providers for fetching altitude data (e.g., from network APIs).
        *   `AltitudeRepository.java`: Repository to get altitude data.
    *   **`geocoding`**: Providers for geocoding (converting addresses to coordinates) and reverse geocoding (coordinates to addresses).
        *   `android`
            *   `GeocodingAndroidRepository.java`: Geocoding using Android's built-in geocoder.
        *   `network`
            *   `GeocodingNetworkRepository.java`, `GeocodingNetworkRouterRepository.java`: Geocoding using network APIs.
            *   `GeocodingRateLimitInterceptor.java`, `GeocodingRequestQueue.java`: Utilities for managing API rate limits and request queuing.
        *   `BaseGeocodingRepository.java`: Base class for geocoding repositories.
    *   **`file`**: Providers for accessing and parsing GPX files from device storage.
        *   `DeviceStorageSearchedFileProvider.java`: Searches for GPX files on device.
        *   `GpxFileDataEntityProvider.java`: Parses GPX files into data entities.
        *   `GpxFileInfoParser.java`: Parses basic info from GPX files.
        *   `GpxFileValidator.java`: Validates GPX files.
    *   **`db`**: Contains Room database definitions, DAOs (Data Access Objects), and local repositories for persisting and querying data.
        *   `geocoding`: Database entities and DAOs for caching geocoding results.
            *   `GeocodingDatabase.java`, `GeocodingResultEntity.java`, `GeocodingResultDao.java`, `GeocodingLocalRepository.java`.
        *   `gpxfileinfo`: Database entities and DAOs for storing GPX file metadata.
            *   `GpxFileInfoDatabase.java`, `GpxFileInfoEntity.java`, `GpxFileInfoDao.java`, `GpxFileInfoRepository.java`.
    *   `ChartProcessedDataProvider.java`: Provides processed data ready for chart display.
    *   `GpxDataEntityCachedProvider.java`: Provides cached GPX data entities.
    *   `GpxFileInfoProvider.java`: Provides GPX file information.
    *   `RawDataProcessedProvider.java`: Provides raw data that has undergone some initial processing.

*   **`network`**: Defines Retrofit service interfaces for network API calls.
    *   `AltitudeService.java`: Service for altitude-related APIs.
    *   `GeocodingService.java`: Service for geocoding APIs.

*   **`mapper`**: Contains classes responsible for mapping data between different layers or models (e.g., network DTOs to domain models, or domain models to database entities).
    *   `GeoPointEntityMapper.java`, `LineDataSetMapper.java`, `LocationMapper.java`, `TrendTypeMapper.java`.

*   **`model`**: Defines Plain Old Java Objects (POJOs) or data classes representing the structure of data, often used for network responses or as domain-specific data structures.
    *   **`geocoding`**: Models for geocoding API responses (e.g., `GeocodingResult.java`, `ReverseGeocodingResponse.java`).
    *   **`gpxfileinfo`**: Model for GPX file information (`GpxFileInfo.java`).
    *   **`opentopodata`**: Models for OpenTopoData API responses (e.g., `OpenTopoDataResponse.java`).

*   **`cumulative`**: Contains classes related to calculating and managing cumulative statistics or trends from GPX data.
    *   `CumulativeProcessedDataType.java`, `CumulativeStatistics.java`, `TrendBoundaryCumulativeMapper.java`, `TrendBoundaryDataEntity.java`, `TrendStatistics.java`, `TrendType.java`.

*   **`raw`**: Defines models and utilities for raw GPX track point data.
    *   `DataEntity.java`, `DataEntityWrapper.java`, `DataMeasure.java`, `GeoPointEntity.java`.

*   **`dsp` (Digital Signal Processing)**: Includes classes for signal processing tasks on GPX data, such as filtering or transformations (e.g., Fast Fourier Transform - FFT).
    *   `FFT.java`, `FFTProcessor.java`, `NoiseFilter.java`, `SignalBuffer.java`, `SignalSamplingProperties.java`, `SignalSpectrum.java`.

*   **`extrema`**: Functionality for detecting and analyzing extrema (maximums and minimums) in the data, possibly for identifying significant points or segments in a track.
    *   **`detector`**: Components for detecting extrema segments.
        *   `ExtremaSegmentDetector.java`, `PrimitiveDataEntity.java`, `Segment.java`.
    *   `ExtremaSegmentListMapper.java`, `WaveletLagDataSmoother.java`.

*   **`statistics`**: Classes for calculating various statistics from GPX data points or entities.
    *   `DataEntityStatistics.java`, `DataEntityStatisticsOperations.java`, `GeoPointStatistics.java`, `GeoPointStatisticsFactory.java`, `GeoPointStatisticsOperations.java`.

*   **`cache`**: Manages in-memory or disk caching of data to improve performance and reduce redundant fetching.
    *   **`processed`**: Caching for data that has undergone some processing.
        *   `chart`: Caching specifically for chart data.
            *   `ChartProcessedDataCachedProvider.java`, `EntryCacheMap.java`, `TrendBoundaryEntryProvider.java`.
        *   `rawdata`: Caching for processed raw data.
            *   `RawDataProcessedCachedProvider.java`.
    *   **`rawdata`**: Caching for raw, unprocessed data.
        *   `DataEntityCache.java`, `GeoPointCache.java`, `LoadDataCache.java`.


## Architecture

The `data` package generally follows a repository pattern, where repositories provide a clean API for data access to the rest of the application (e.g., use cases). These repositories then delegate to specific data sources like network services, local databases, or file parsers. Mappers are used extensively to transform data between layers. Caching strategies are implemented to optimize data retrieval.

```mermaid
graph TD
    A[Use Cases / Domain Layer] --> B(Repositories in data.provider)

    B --> C{Network Services (data.network)}
    B --> D{Databases (data.provider.db)}
    B --> E{File System (data.provider.file)}
    B --> F{Cache (data.cache)}

    C --> G[External APIs]
    D --> H[Local Storage]
    E --> I[GPX Files]

    J[data.model] -- Defines data structures for --> C
    J -- Defines data structures for --> D
    J -- Defines data structures for --> B

    K[data.mapper] -- Maps data between --> C
    K -- and --> B
    K -- Maps data between --> D
    K -- and --> B

    L[data.raw] -- Raw Data Models & Utils --> B
    M[data.dsp] -- DSP Algorithms --> B
    N[data.cumulative] -- Cumulative Stats --> B
    O[data.extrema] -- Extrema Detection --> B
    P[data.statistics] -- Statistical Analysis --> B

    F -- Provides cached data to --> B
    B -- Stores data in --> F
```

*(Diagram illustrates the flow of data from various sources through repositories to the upper layers, highlighting the role of mappers, models, and specialized data processing packages.)* 