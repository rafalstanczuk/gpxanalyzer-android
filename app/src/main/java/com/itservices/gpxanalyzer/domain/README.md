# GPX Analyzer - Domain Layer

## Overview

The domain layer represents the core business logic of the GPX Analyzer application. It is responsible for processing, analyzing, and extracting meaningful insights from GPX track data. The domain layer is organized into three main functional areas that work together to provide comprehensive GPS track analysis capabilities.

## Architecture

The domain layer follows a clean architecture pattern with clear separation of concerns:

```
domain/
├── service/          # Application services and workflows
├── cumulative/       # Trend analysis and cumulative statistics
└── extrema/          # Extrema detection and signal processing
    └── detector/     # Core detection algorithms
```

## Core Components

### 1. Service Layer (`service/`)

**Purpose**: Orchestrates high-level application workflows for GPX file processing.

**Key Components**:
- **`GpxFileInfoUpdateService`**: Interface defining operations for GPX file lifecycle management
  - File scanning and discovery
  - Miniature map generation
  - Geocoding operations
  - Database updates

**Responsibilities**:
- Coordinate complex workflows involving multiple data sources
- Provide clean interfaces for UI layer interactions
- Manage asynchronous operations using RxJava

### 2. Cumulative Analysis (`cumulative/`)

**Purpose**: Analyzes GPS tracks to identify trends and calculate cumulative statistics for visualization.

**Key Components**:

#### Data Models
- **`CumulativeStatistics`**: Encapsulates cumulative values with accuracy and units
- **`TrendStatistics`**: Statistical measurements for trend segments
- **`TrendBoundaryDataEntity`**: Complete trend boundary with associated data entities
- **`TrendType`**: Enumeration of trend types (UP, DOWN, CONSTANT) with visualization properties

#### Processing
- **`TrendBoundaryCumulativeMapper`**: Core processor that converts extrema segments into trend boundaries
- **`CumulativeProcessedDataType`**: Defines different cumulative calculation strategies

**Key Features**:
- Identifies coherent trend segments (ascents, descents, flat sections)
- Calculates both segment-specific and track-wide cumulative statistics
- Supports multiple visualization modes for data analysis
- Tracks accuracy and units for statistical measurements

### 3. Extrema Detection (`extrema/`)

**Purpose**: Implements advanced signal processing algorithms to detect significant points and segments in GPS data.

**Key Components**:

#### Core Detection
- **`ExtremaSegmentDetector`**: Advanced algorithm for detecting local extrema and trend segments
- **`ExtremaSegmentListMapper`**: High-level interface for converting data into segments
- **`WaveletLagDataSmoother`**: Wavelet-based noise reduction and adaptive smoothing

#### Data Models
- **`PrimitiveDataEntity`**: Simplified data structure for extrema detection algorithms
- **`Segment`**: Represents a detected segment with start/end indices, times, and values
- **`SegmentTrendType`**: Classification of segment trends
- **`SegmentThresholds`**: Configuration for detection sensitivity

#### Support Classes
- **`DataPrimitiveMapper`**: Converts complex data entities to primitive format

**Advanced Features**:
- **Wavelet Analysis**: Uses FFT-based frequency analysis for optimal window size selection
- **Adaptive Smoothing**: Adjusts smoothing parameters based on data characteristics
- **Noise Reduction**: Intelligent filtering that preserves important signal features
- **Multi-stage Detection**: Combines derivative analysis with amplitude thresholding

## Data Flow

### Primary Processing Pipeline

1. **Data Input**: Raw GPX data entities with timestamps, values, and accuracy
2. **Primitive Conversion**: Transform complex entities to simplified format for processing
3. **Noise Analysis**: FFT analysis to determine optimal smoothing parameters
4. **Signal Smoothing**: Apply adaptive wavelet-based smoothing
5. **Extrema Detection**: Identify local minima and maxima using derivative analysis
6. **Segment Creation**: Convert extrema into coherent trend segments
7. **Statistical Analysis**: Calculate cumulative statistics and trend properties
8. **Boundary Mapping**: Create final trend boundaries for visualization

### Data Transformations

```
DataEntityWrapper → PrimitiveDataEntity → Smoothed Data → Extrema → Segments → TrendBoundaries
```

## Key Algorithms

### Extrema Detection Algorithm

The extrema detection uses a sophisticated multi-stage approach:

1. **Preprocessing**: Filter data by accuracy thresholds
2. **Adaptive Window Calculation**: Use wavelet analysis to determine optimal smoothing window
3. **Signal Smoothing**: Apply weighted moving average with adaptive window
4. **Derivative Analysis**: Calculate discrete derivatives to find slope changes
5. **Extrema Identification**: Detect sign changes in derivatives (slope reversals)
6. **Segment Formation**: Group data points between extrema into coherent segments
7. **Gap Filling**: Add missing segments to ensure complete coverage

### Wavelet-Based Smoothing

The smoothing algorithm incorporates:

- **FFT Analysis**: Frequency domain analysis for noise characterization
- **Adaptive Window Sizing**: Window size adjusts based on signal characteristics
- **Noise Threshold Calculation**: Dynamic threshold based on signal-to-noise ratio
- **Multiple Window Functions**: Support for Triangular, Hanning, and Gaussian windows

### Cumulative Statistics Calculation

Two main calculation modes:

1. **Segment-Based**: Cumulative values reset at each segment boundary
2. **Track-Wide**: Continuous accumulation across the entire track

## Design Patterns

### Strategy Pattern
- Multiple cumulative calculation strategies (`CumulativeProcessedDataType`)
- Different window functions for smoothing (`WindowType`)

### Builder Pattern
- Complex data entity construction with optional parameters
- Statistical object creation with validation

### Observer Pattern
- RxJava streams for asynchronous data processing
- Event-driven updates for UI components

### Factory Pattern
- Creation of appropriate detection algorithms based on data characteristics
- Window function generation based on type and parameters

## Usage Examples

### Basic Extrema Detection
```java
// Convert data to segments
Single<Vector<Segment>> segments = ExtremaSegmentListMapper.mapFrom(dataWrapper);

// Process into trend boundaries
List<TrendBoundaryDataEntity> trends = TrendBoundaryCumulativeMapper.mapFrom(
    dataWrapper, 
    segmentList
);
```

### Service Operations
```java
// Complete GPX file processing workflow
gpxService.scanFiles(context)
    .flatMapCompletable(files -> 
        gpxService.generateMiniatures(files)
            .andThen(gpxService.performGeocoding(files))
            .andThen(gpxService.updateDatabase(files))
    );
```

## Performance Considerations

- **Memory Efficiency**: Streaming processing for large GPX files
- **Computational Optimization**: FFT-based algorithms for signal processing
- **Accuracy Trade-offs**: Configurable thresholds for speed vs. precision
- **Asynchronous Processing**: Non-blocking operations using RxJava

## Testing

The domain layer includes comprehensive unit tests for:
- Extrema detection accuracy
- Statistical calculation correctness
- Data transformation integrity
- Edge case handling

## Dependencies

### External Libraries
- **Apache Commons Math**: Advanced mathematical operations (FFT, statistics)
- **RxJava**: Reactive programming and async operations
- **Android Support Libraries**: For UI integration

### Internal Dependencies
- **Core Data Layer**: Entity models and data providers
- **Core Utils**: Common utilities and helper functions

## Future Enhancements

- **Machine Learning Integration**: Pattern recognition for activity classification
- **Advanced Filtering**: Kalman filters for GPS noise reduction
- **Multi-dimensional Analysis**: Support for 3D track analysis
- **Real-time Processing**: Live track analysis capabilities
- **Custom Algorithm Plugins**: Extensible algorithm framework 