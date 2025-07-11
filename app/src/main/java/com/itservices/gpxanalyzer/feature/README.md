# GPX Analyzer - Feature Module

## Overview

The Feature module contains the main application features that provide user-facing functionality for GPX file analysis and visualization. This module follows Clean Architecture principles, ensuring separation of concerns and maintainability.

## Architecture

The Feature module is organized into two main features:

### 📁 Features Structure
```
feature/
├── gpxlist/           # GPX File Management Feature
│   ├── data/          # Data layer (repositories, providers, models)
│   ├── domain/        # Business logic (use cases)
│   └── ui/            # Presentation layer (fragments, viewmodels, adapters)
└── gpxchart/          # GPX Data Visualization Feature
    ├── data/          # Data layer (data providers, mappers)
    ├── domain/        # Business logic (use cases)
    └── ui/            # Presentation layer (fragments, viewmodels, charts)
```

## Features

### 🗂️ GPX List Feature (`gpxlist/`)

**Purpose**: Manages GPX file discovery, selection, and metadata management.

#### Key Capabilities:
- **File Discovery**: Scans device storage recursively for GPX files
- **Metadata Parsing**: Extracts creator, author, and location information from GPX files
- **Miniature Generation**: Creates map thumbnails for quick file identification
- **Geocoding**: Resolves coordinates to human-readable addresses
- **File Selection**: Provides both automatic discovery and manual file picker options
- **Database Management**: Persists GPX file information for quick access

#### Core Components:
- **Use Cases**:
  - `GetGpxFileInfoListUseCase` - Retrieves stored GPX file information
  - `UpdateGpxFileInfoListUseCase` - Scans and updates GPX file database
  - `SelectGpxFileUseCase` - Handles file selection and permissions
- **Data Providers**:
  - `GpxFileInfoProvider` - Main provider for GPX file operations
  - `GpxFileInfoParser` - Parses GPX metadata
- **UI Components**:
  - `FileSelectorFragment` - File selection dialog
  - `FileSelectorViewModel` - Manages file selection state

### 📊 GPX Chart Feature (`gpxchart/`)

**Purpose**: Visualizes GPX data through interactive charts and analysis tools.

#### Key Capabilities:
- **Multi-Chart Visualization**: Supports multiple synchronized chart views
- **View Modes**: Different data perspectives (Altitude vs Time, Speed vs Time)
- **Chart Synchronization**: Coordinates multiple charts for unified analysis
- **Data Processing**: Transforms raw GPX data into chart-ready formats
- **Interactive Features**: Zoom, scroll, and selection capabilities
- **Customizable Display**: Configurable chart appearance and data representation

#### Core Components:
- **Use Cases**:
  - `LoadChartDataUseCase` - Orchestrates data loading for charts
  - `ChartInitializerUseCase` - Initializes chart components
  - `MultipleSyncedGpxChartUseCase` - Manages multiple chart synchronization
- **Data Providers**:
  - `GpxFileDataEntityProvider` - Converts GPX data to chart entities
  - `ChartProcessedDataProvider` - Processes data for chart rendering
- **UI Components**:
  - `ChartAreaListFragment` - Main chart display container
  - `ChartAreaItem` - Individual chart component wrapper

## Feature Interactions

### Data Flow
1. **File Selection**: `gpxlist` feature discovers and allows selection of GPX files
2. **Data Loading**: Selected files are processed by `gpxchart` feature
3. **Visualization**: Processed data is displayed in interactive charts
4. **Synchronization**: Global event system coordinates between features

### Shared Dependencies
Both features utilize:
- **Core Data Layer**: Shared data models and caching systems
- **Global Events**: `GlobalEventWrapper` for inter-feature communication
- **Core UI Components**: Shared UI utilities and components
- **Domain Services**: Shared business logic services

## Technology Stack

### Architecture Patterns
- **Clean Architecture**: Separation of UI, Domain, and Data layers
- **MVVM**: Model-View-ViewModel pattern for UI layer
- **Repository Pattern**: Data access abstraction
- **Use Case Pattern**: Encapsulation of business logic

### Dependencies
- **RxJava**: Reactive programming for asynchronous operations
- **Dagger Hilt**: Dependency injection framework
- **Android Architecture Components**: LiveData, ViewModel, Data Binding
- **MPAndroidChart**: Chart visualization library
- **Room Database**: Local data persistence

## Key Design Principles

### Single Responsibility
Each feature has a clear, focused responsibility:
- `gpxlist`: File management and metadata
- `gpxchart`: Data visualization and analysis

### Loose Coupling
Features communicate through:
- Global event system
- Shared core components
- Well-defined interfaces

### High Cohesion
Related functionality is grouped together within feature boundaries while maintaining clean interfaces.

### Testability
- Use cases encapsulate business logic for easy testing
- Dependency injection enables test doubles
- Clear separation of concerns facilitates unit testing

## Usage Examples

### Loading and Displaying GPX Data
```java
// 1. Discover GPX files (gpxlist feature)
GetGpxFileInfoListUseCase getFiles;
getFiles.getGpxFileInfoList()
    .subscribe(files -> {
        // 2. Select file and load into charts (gpxchart feature)
        MultipleSyncedGpxChartUseCase chartLoader;
        chartLoader.loadData(chartItems);
    });
```

### File Selection with Permissions
```java
// Handle file selection and permissions
SelectGpxFileUseCase selectFile;
selectFile.checkAndRequestPermissions(activity)
    .subscribe(granted -> {
        if (granted) {
            selectFile.openFilePicker();
        }
    });
```

## Performance Considerations

### Caching Strategy
- **File Metadata**: Cached in local database for quick access
- **Chart Data**: Multi-level caching (raw, processed, rendered)
- **Miniatures**: Generated once and cached as bitmaps

### Threading
- **IO Operations**: Background threads for file operations
- **Data Processing**: Computation threads for heavy processing
- **UI Updates**: Main thread for view updates

### Memory Management
- **Weak References**: Used for context references
- **Disposables**: Proper cleanup of RxJava subscriptions
- **Bitmap Recycling**: Efficient image memory management

## Future Enhancements

### Planned Features
- **Export Functionality**: Export processed data and charts
- **Advanced Filtering**: Filter GPX files by various criteria
- **Custom Chart Types**: Additional visualization modes
- **Batch Processing**: Process multiple files simultaneously

### Performance Improvements
- **Lazy Loading**: Load chart data on demand
- **Progressive Rendering**: Render charts incrementally
- **Background Processing**: Move more operations to background threads

## Contributing

When adding new features or modifying existing ones:

1. **Follow Clean Architecture**: Maintain separation of layers
2. **Use Dependency Injection**: Leverage Dagger Hilt for dependencies
3. **Implement Use Cases**: Encapsulate business logic in use cases
4. **Add Tests**: Write unit tests for new functionality
5. **Update Documentation**: Keep documentation current with changes

## Related Documentation

- [Core Module Documentation](../core/README.md)
- [Domain Module Documentation](../domain/README.md)
- [Architecture Overview](../Explanation.md) 