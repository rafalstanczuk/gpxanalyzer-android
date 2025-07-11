# Core Module Technical Explanation

This document provides an in-depth technical explanation of the core module's architecture, design patterns, and implementation details. It serves as a comprehensive guide for developers working on or extending the GPX Analyzer application.

## Table of Contents
- [Architecture Patterns](#architecture-patterns)
- [Component Deep Dive](#component-deep-dive)
- [Data Flow & Processing](#data-flow--processing)
- [Event System Architecture](#event-system-architecture)
- [UI Component Framework](#ui-component-framework)
- [Performance Optimizations](#performance-optimizations)
- [Error Handling Strategy](#error-handling-strategy)
- [Testing Architecture](#testing-architecture)

## Architecture Patterns

### Clean Architecture Implementation

The core module implements a variant of Clean Architecture with the following layers:

```mermaid
graph TB
    subgraph "Clean Architecture Layers"
        subgraph "Presentation Layer"
            UI_COMP["`**UI Components**
            - DataEntityLineChart
            - DataMapView
            - SpeedDialFabView`"]
            
            CONTROLLERS["`**Controllers**
            - ChartController
            - MapViewController`"]
        end
        
        subgraph "Application Layer"
            PROVIDERS["`**Data Providers**
            - GpxDataEntityCachedProvider
            - ChartProcessedDataProvider`"]
            
            EVENT_BUS["`**Event Management**
            - GlobalEventWrapper
            - Event Types`"]
        end
        
        subgraph "Domain Layer"
            ENTITIES["`**Domain Models**
            - DataEntity
            - Gpx
            - GeoPointEntity`"]
            
            SERVICES["`**Domain Services**
            - LocationCalculatorUtil
            - GPXParser`"]
        end
        
        subgraph "Infrastructure Layer"
            REPOS["`**Repositories**
            - AltitudeRepository
            - GeocodingAndroidRepository`"]
            
            CACHE["`**Caching**
            - DataEntityCache
            - EntryCacheMap`"]
            
            NETWORK["`**Network**
            - AltitudeService
            - Retrofit APIs`"]
        end
    end
    
    UI_COMP --> CONTROLLERS
    CONTROLLERS --> PROVIDERS
    CONTROLLERS --> EVENT_BUS
    PROVIDERS --> ENTITIES
    PROVIDERS --> SERVICES
    PROVIDERS --> REPOS
    REPOS --> CACHE
    REPOS --> NETWORK
    
    classDef presentation fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef application fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef domain fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef infrastructure fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class UI_COMP,CONTROLLERS presentation
    class PROVIDERS,EVENT_BUS application
    class ENTITIES,SERVICES domain
    class REPOS,CACHE,NETWORK infrastructure
```

### Dependency Injection Pattern

The module uses Dagger Hilt for comprehensive dependency injection:

```mermaid
graph LR
    subgraph "DI Container Structure"
        subgraph "Singleton Scope"
            DB[Database Modules]
            NET[Network Module]
            CACHE_S[Cache Singletons]
        end
        
        subgraph "Activity Scope"
            CTRL[Controllers]
            PROVIDERS[Data Providers]
        end
        
        subgraph "Fragment Scope"
            UI[UI Components]
            ADAPTERS[View Adapters]
        end
    end
    
    DB --> PROVIDERS
    NET --> PROVIDERS
    CACHE_S --> PROVIDERS
    PROVIDERS --> CTRL
    CTRL --> UI
    
    classDef singleton fill:#ffebee,stroke:#c62828,stroke-width:2px
    classDef activity fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef fragment fill:#e1f5fe,stroke:#0277bd,stroke-width:2px
    
    class DB,NET,CACHE_S singleton
    class CTRL,PROVIDERS activity
    class UI,ADAPTERS fragment
```

## Component Deep Dive

### Data Layer Architecture

The data layer implements a sophisticated caching and processing pipeline:

```mermaid
graph TD
    subgraph "Data Processing Pipeline"
        INPUT["`**Input Sources**
        - GPX Files (Raw)
        - External APIs
        - User Selections`"]
        
        PARSER["`**Parsing Layer**
        - GPXParser
        - Domain Object Creation
        - Validation`"]
        
        TRANSFORM["`**Transformation Layer**
        - DataEntity Mapping
        - Statistical Processing
        - Coordinate Conversion`"]
        
        CACHE_L1["`**L1 Cache (Raw)**
        - DataEntityCache
        - File-based Caching
        - Memory Management`"]
        
        CACHE_L2["`**L2 Cache (Processed)**
        - ChartProcessedDataCachedProvider
        - UI-specific Processing
        - Lazy Evaluation`"]
        
        OUTPUT["`**Output**
        - Chart Data
        - Map Overlays
        - Statistical Results`"]
    end
    
    INPUT --> PARSER
    PARSER --> TRANSFORM
    TRANSFORM --> CACHE_L1
    CACHE_L1 --> CACHE_L2
    CACHE_L2 --> OUTPUT
    
    CACHE_L1 -.-> TRANSFORM
    CACHE_L2 -.-> OUTPUT
    
    classDef input fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef processing fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef cache fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef output fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class INPUT input
    class PARSER,TRANSFORM processing
    class CACHE_L1,CACHE_L2 cache
    class OUTPUT output
```

### Event System Implementation

The event system uses RxJava for reactive programming patterns:

```mermaid
graph TB
    subgraph "Event System Architecture"
        subgraph "Event Sources"
            CHART[Chart Interactions]
            MAP[Map Interactions]
            FAB[FAB Actions]
            DATA_OPS[Data Operations]
        end
        
        subgraph "Event Bus (GlobalEventWrapper)"
            PUB_SUBJ["`**PublishSubject**
            - EventEntrySelection`"]
            
            BEH_SUBJ["`**BehaviorSubject**
            - RequestStatus
            - EventProgress
            - VisibleChartEntries`"]
        end
        
        subgraph "Event Consumers"
            UI_SYNC[UI Synchronization]
            PROGRESS[Progress Updates]
            STATE_MGMT[State Management]
        end
        
        subgraph "Event Processing"
            FILTER[Event Filtering]
            TRANSFORM_E[Event Transformation]
            THROTTLE[Throttling/Debouncing]
        end
    end
    
    CHART --> PUB_SUBJ
    MAP --> PUB_SUBJ
    FAB --> BEH_SUBJ
    DATA_OPS --> BEH_SUBJ
    
    PUB_SUBJ --> FILTER
    BEH_SUBJ --> FILTER
    
    FILTER --> TRANSFORM_E
    TRANSFORM_E --> THROTTLE
    
    THROTTLE --> UI_SYNC
    THROTTLE --> PROGRESS
    THROTTLE --> STATE_MGMT
    
    classDef source fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef eventBus fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef processing fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef consumer fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class CHART,MAP,FAB,DATA_OPS source
    class PUB_SUBJ,BEH_SUBJ eventBus
    class FILTER,TRANSFORM_E,THROTTLE processing
    class UI_SYNC,PROGRESS,STATE_MGMT consumer
```

## Data Flow & Processing

### GPX Processing Workflow

```mermaid
sequenceDiagram
    participant User
    participant UI as UI Component
    participant Controller
    participant Provider as Data Provider
    participant Parser as GPX Parser
    participant Cache
    participant DB as Database
    participant API as External API
    
    User->>UI: Select GPX File
    UI->>Controller: File Selection Event
    Controller->>Provider: Request GPX Data
    
    alt File Not Cached
        Provider->>Parser: Parse GPX File
        Parser->>Parser: Validate & Transform
        Parser->>Provider: Return Gpx Object
        Provider->>Provider: Create DataEntities
        Provider->>Cache: Store in Cache
    else File Cached
        Provider->>Cache: Retrieve from Cache
    end
    
    Provider->>Controller: Return DataEntity Vector
    Controller->>UI: Update Display
    
    par Async Operations
        Provider->>API: Request Altitude Data
        API->>Provider: Return Elevation Results
        Provider->>DB: Store Geocoding Data
    end
    
    Provider->>UI: Update with Enhanced Data
```

### Chart-Map Synchronization

```mermaid
sequenceDiagram
    participant Chart
    participant EventBus as Global Event Bus
    participant Map
    participant Controller as Map Controller
    
    Chart->>Chart: User Selects Point
    Chart->>EventBus: Emit EntrySelection Event
    EventBus->>Map: Broadcast Selection
    Map->>Controller: Process Selection
    Controller->>Controller: Calculate GeoPoint
    Controller->>Map: Show Marker
    
    Chart->>Chart: User Changes Visible Range
    Chart->>EventBus: Emit VisibleRange Event
    EventBus->>Map: Broadcast Range Change
    Map->>Controller: Process Range Change
    Controller->>Map: Draw Range Polyline
```

## UI Component Framework

### Chart Component Architecture

The chart components are built on MPAndroidChart with extensive customizations:

```mermaid
classDiagram
    class DataEntityLineChart {
        +ChartComponents components
        +GridBackgroundDrawer backgroundDrawer
        +ChartSlot chartSlot
        +initChart(ChartComponents)
        +updateData(LineData)
        +highlightValue(Highlight)
    }
    
    class ChartController {
        +ChartProvider chartProvider
        +GlobalEventWrapper eventWrapper
        +bindChart(DataEntityLineChart)
        +onValueSelected(Entry, Highlight)
        +onChartGestureStart(MotionEvent, ChartTouchListener.ChartGesture)
    }
    
    class ChartProvider {
        +ChartProcessedDataProvider dataProvider
        +ChartComponents components
        +registerBinding(DataEntityLineChart)
        +initChart(): Single<RequestStatus>
        +updateDataChart(): Single<RequestStatus>
    }
    
    class ChartComponents {
        +LimitLinesBoundaries limitLines
        +LineChartScaler scaler
        +PaletteColorDeterminer palette
        +LineChartSettings settings
        +init(DataEntityWrapper)
        +loadChartSettings(DataEntityLineChart)
    }
    
    DataEntityLineChart --> ChartController
    ChartController --> ChartProvider
    ChartProvider --> ChartComponents
    
    class LineChartSettings {
        +configureAppearance()
        +configureInteraction()
        +configureLegend()
        +configureAxes()
    }
    
    class LineChartScaler {
        +scaleXAxis()
        +scaleYAxis()
        +setVisibleRange()
        +fitScreen()
    }
    
    ChartComponents --> LineChartSettings
    ChartComponents --> LineChartScaler
```

### Map Component Integration

The map components integrate OSMDroid with custom overlay management:

```mermaid
classDiagram
    class DataMapView {
        +MapViewController controller
        +OverlayViewToReloadLayoutView overlay
        +init(Context)
        +configureMap()
    }
    
    class MapViewController {
        +MapReadinessManager readinessManager
        +GeoPointCache geoPointCache
        +GlobalEventWrapper eventWrapper
        +bind(DataMapView)
        +onMapReady()
        +handleEntrySelection(EventEntrySelection)
    }
    
    class MapOperations {
        <<interface>>
        +setCenter(GeoPoint)
        +setZoom(double)
        +setBoundingBox(BoundingBox)
    }
    
    class MapOverlayOperations {
        <<interface>>
        +addMarker(GeoPoint, String)
        +addPolyline(List<GeoPoint>, int, float)
        +clearMarkers()
        +clearPolylines()
    }
    
    class MapReadinessManager {
        +AtomicBoolean isMapReady
        +PublishSubject<Boolean> mapReadySubject
        +bind(MapView)
        +setMapReady()
        +observeMapReady(): Observable<Boolean>
    }
    
    DataMapView --> MapViewController
    MapViewController ..|> MapOperations
    MapViewController ..|> MapOverlayOperations
    MapViewController --> MapReadinessManager
```

## Performance Optimizations

### Memory Management Strategy

```mermaid
graph TB
    subgraph "Memory Management"
        subgraph "Weak References"
            WEAK_CHART[Chart WeakReference]
            WEAK_MAP[Map WeakReference]
            WEAK_MARKER[Marker WeakReference]
        end
        
        subgraph "Cache Management"
            LRU[LRU Cache Strategy]
            SIZE_LIMIT[Size-based Eviction]
            TIME_LIMIT[Time-based Expiration]
        end
        
        subgraph "Resource Disposal"
            RX_DISPOSE[RxJava Disposables]
            CALLBACK_CLEAR[Callback Clearing]
            LISTENER_REMOVE[Listener Removal]
        end
        
        subgraph "Lifecycle Awareness"
            ACTIVITY_AWARE[Activity Lifecycle]
            FRAGMENT_AWARE[Fragment Lifecycle]
            VIEW_AWARE[View Lifecycle]
        end
    end
    
    WEAK_CHART --> RX_DISPOSE
    WEAK_MAP --> RX_DISPOSE
    WEAK_MARKER --> CALLBACK_CLEAR
    
    LRU --> SIZE_LIMIT
    SIZE_LIMIT --> TIME_LIMIT
    
    RX_DISPOSE --> ACTIVITY_AWARE
    CALLBACK_CLEAR --> FRAGMENT_AWARE
    LISTENER_REMOVE --> VIEW_AWARE
    
    classDef memory fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef cache fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef disposal fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef lifecycle fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class WEAK_CHART,WEAK_MAP,WEAK_MARKER memory
    class LRU,SIZE_LIMIT,TIME_LIMIT cache
    class RX_DISPOSE,CALLBACK_CLEAR,LISTENER_REMOVE disposal
    class ACTIVITY_AWARE,FRAGMENT_AWARE,VIEW_AWARE lifecycle
```

### Async Processing Pipeline

```mermaid
graph LR
    subgraph "Threading Strategy"
        subgraph "IO Thread Pool"
            FILE_IO[File Operations]
            NETWORK_IO[Network Requests]
            DB_IO[Database Operations]
        end
        
        subgraph "Computation Thread Pool"
            PARSING[GPX Parsing]
            CALCULATIONS[Math Calculations]
            TRANSFORMATIONS[Data Transformations]
        end
        
        subgraph "Main Thread"
            UI_UPDATES[UI Updates]
            EVENT_DISPATCH[Event Dispatching]
            USER_INPUT[User Input Handling]
        end
    end
    
    FILE_IO --> PARSING
    NETWORK_IO --> TRANSFORMATIONS
    DB_IO --> CALCULATIONS
    
    PARSING --> UI_UPDATES
    CALCULATIONS --> UI_UPDATES
    TRANSFORMATIONS --> UI_UPDATES
    
    UI_UPDATES --> EVENT_DISPATCH
    EVENT_DISPATCH --> USER_INPUT
    
    classDef io fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef computation fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef main fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    
    class FILE_IO,NETWORK_IO,DB_IO io
    class PARSING,CALCULATIONS,TRANSFORMATIONS computation
    class UI_UPDATES,EVENT_DISPATCH,USER_INPUT main
```

## Error Handling Strategy

### Comprehensive Error Management

```mermaid
graph TB
    subgraph "Error Handling Architecture"
        subgraph "Error Types"
            PARSE_ERR[Parsing Errors]
            NET_ERR[Network Errors]
            IO_ERR[File I/O Errors]
            UI_ERR[UI Errors]
        end
        
        subgraph "Error Processing"
            CATCH[Exception Catching]
            LOG[Error Logging]
            TRANSFORM_ERR[Error Transformation]
            RECOVERY[Recovery Strategies]
        end
        
        subgraph "User Feedback"
            STATUS[Status Updates]
            PROGRESS[Progress Indication]
            ALERTS[Error Alerts]
            FALLBACK[Fallback UI]
        end
        
        subgraph "Error Recovery"
            RETRY[Retry Mechanisms]
            CACHE_FALLBACK[Cache Fallback]
            DEFAULT_DATA[Default Data]
            GRACEFUL_DEGRADATION[Graceful Degradation]
        end
    end
    
    PARSE_ERR --> CATCH
    NET_ERR --> CATCH
    IO_ERR --> CATCH
    UI_ERR --> CATCH
    
    CATCH --> LOG
    LOG --> TRANSFORM_ERR
    TRANSFORM_ERR --> RECOVERY
    
    RECOVERY --> STATUS
    RECOVERY --> PROGRESS
    RECOVERY --> ALERTS
    RECOVERY --> FALLBACK
    
    RECOVERY --> RETRY
    RECOVERY --> CACHE_FALLBACK
    RECOVERY --> DEFAULT_DATA
    RECOVERY --> GRACEFUL_DEGRADATION
    
    classDef error fill:#ffebee,stroke:#c62828,stroke-width:2px
    classDef processing fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef feedback fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef recovery fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    
    class PARSE_ERR,NET_ERR,IO_ERR,UI_ERR error
    class CATCH,LOG,TRANSFORM_ERR,RECOVERY processing
    class STATUS,PROGRESS,ALERTS,FALLBACK feedback
    class RETRY,CACHE_FALLBACK,DEFAULT_DATA,GRACEFUL_DEGRADATION recovery
```

## Testing Architecture

### Testing Strategy Overview

The core module employs a comprehensive testing strategy:

```mermaid
graph TB
    subgraph "Testing Pyramid"
        subgraph "Unit Tests"
            UTIL_TEST[Utility Function Tests]
            MODEL_TEST[Model Validation Tests]
            CALC_TEST[Calculation Logic Tests]
        end
        
        subgraph "Integration Tests"
            PROVIDER_TEST[Data Provider Tests]
            CACHE_TEST[Cache Integration Tests]
            EVENT_TEST[Event System Tests]
        end
        
        subgraph "UI Tests"
            CHART_TEST[Chart Component Tests]
            MAP_TEST[Map Component Tests]
            INTERACTION_TEST[User Interaction Tests]
        end
        
        subgraph "End-to-End Tests"
            WORKFLOW_TEST[Complete Workflow Tests]
            PERF_TEST[Performance Tests]
            ERROR_TEST[Error Scenario Tests]
        end
    end
    
    UTIL_TEST --> PROVIDER_TEST
    MODEL_TEST --> PROVIDER_TEST
    CALC_TEST --> CACHE_TEST
    
    PROVIDER_TEST --> CHART_TEST
    CACHE_TEST --> MAP_TEST
    EVENT_TEST --> INTERACTION_TEST
    
    CHART_TEST --> WORKFLOW_TEST
    MAP_TEST --> PERF_TEST
    INTERACTION_TEST --> ERROR_TEST
    
    classDef unit fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef integration fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    classDef ui fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef e2e fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class UTIL_TEST,MODEL_TEST,CALC_TEST unit
    class PROVIDER_TEST,CACHE_TEST,EVENT_TEST integration
    class CHART_TEST,MAP_TEST,INTERACTION_TEST ui
    class WORKFLOW_TEST,PERF_TEST,ERROR_TEST e2e
```

### Mock and Test Double Strategy

```mermaid
classDiagram
    class TestModule {
        <<Hilt Test Module>>
        +provideMockDataProvider()
        +provideMockEventWrapper()
        +provideMockRepository()
    }
    
    class MockDataProvider {
        +provide(): Single<Vector<DataEntity>>
        +simulateLoading()
        +simulateError()
    }
    
    class MockEventWrapper {
        +TestSubject entrySelection
        +TestSubject requestStatus
        +captureEvents()
        +verifyEventSequence()
    }
    
    class TestDataFactory {
        +createSampleGpxData()
        +createErrorScenarios()
        +createPerformanceTestData()
    }
    
    TestModule --> MockDataProvider
    TestModule --> MockEventWrapper
    TestModule --> TestDataFactory
```

## Key Implementation Insights

### 1. Reactive State Management
The application uses RxJava extensively for managing state changes and ensuring UI consistency. The `GlobalEventWrapper` acts as a central nervous system, coordinating between different components without tight coupling.

### 2. Multi-Level Caching Strategy
The caching system operates on multiple levels:
- **L1 Cache**: Raw parsed data stored in memory
- **L2 Cache**: Processed, UI-ready data with lazy evaluation
- **L3 Cache**: Persistent storage for metadata and preferences

### 3. Component Lifecycle Management
All components are designed to be lifecycle-aware, preventing memory leaks and ensuring proper resource management through weak references and disposable patterns.

### 4. Modular UI Architecture
UI components are designed as self-contained modules with clear interfaces, making them reusable across different features and easy to test in isolation.

### 5. Performance-First Design
The architecture prioritizes performance through async processing, lazy loading, and efficient memory management, ensuring smooth operation even with large GPX files.

---

*This technical explanation provides the foundation for understanding and extending the core module architecture.* 