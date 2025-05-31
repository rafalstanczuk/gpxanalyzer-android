# UI Package Explanation

This package contains all UI-related components of the GpxAnalyzer application, including Fragments, ViewModels, Adapters, custom views, and UI utility classes. It is structured into several sub-packages to organize different aspects of the user interface.

## Main Sub-Packages and their Roles

*   **`gpxchart`**: Manages the display of GPX data in chart format.
    *   `ChartAreaListFragment.java`: Fragment for displaying a list of chart areas.
    *   `ChartAreaListViewModel.java`: ViewModel for `ChartAreaListFragment`, handling data and logic for chart display.
    *   **`item`**: Components related to individual items within the chart list.
        *   `ChartAreaItem.java`: Represents a single chart area item.
        *   `ChartAreaItemAdapter.java`: Adapter for the RecyclerView displaying chart areas.
        *   `SpeedDialFabHelperZoom.java`, `SpeedDialFabHelperChartSettings.java`: Helpers for Speed Dial FABs related to chart zooming and settings.
    *   **`viewmode`**: Defines different view modes for GPX data (e.g., elevation, speed) and mappers.
        *   `GpxViewMode.java`, `GpxViewModeMapper.java`, `ViewModeSeverity.java`, `ViewModeMapper.java`.

*   **`storage`**: Components related to selecting and managing GPX files from device storage.
    *   `FileSelectorFragment.java`: Fragment for allowing users to select GPX files.
    *   `FileSelectorViewModel.java`: ViewModel for `FileSelectorFragment`.
    *   `FileAdapter.java`: Adapter for displaying a list of files.
    *   `FileInfoItem.java`: Represents an item in the file list.

*   **`mapper`**: UI-specific mappers, primarily for transforming data models into UI-displayable formats.
    *   `FileInfoItemMapper.java`: Maps `GpxFileInfo` to `FileInfoItem` for display.

*   **`components`**: Reusable custom UI components used throughout the application.
    *   **`chart`**: Core components for rendering charts, using the MPAndroidChart library.
        *   `DataEntityLineChart.java`: Custom line chart view.
        *   `ChartController.java`: Manages chart interactions and updates.
        *   `ChartProvider.java`: Provides configured chart instances.
        *   **`extras`**: Additional views or helpers for charts (e.g., overlays, info displays).
        *   **`palette`**: Color management for charts.
        *   **`settings`**: Configuration and settings for charts (axis formatters, background, highlighting).
        *   **`entry`**: Custom chart data entry types.
        *   **`legend`**: Custom legend components.
    *   **`fab`**: Components related to Floating Action Buttons (FABs), including Speed Dial FABs.
        *   `SpeedDialFabView.java`.
    *   **`geocoding`**: UI components for address search and reverse geocoding display.
        *   `AddressSearchView.java`, `ReverseGeocodingHelper.java`.
    *   **`mapview`**: Components for displaying GPX tracks on a map, using the osmdroid library.
        *   `DataMapView.java`: Custom map view.
        *   `MapViewController.java`: Controller for map interactions, overlays, and markers.
        *   `MapConfig.java`, `MapOperations.java`, `MapReadinessManager.java`.
    *   **`miniature`**: Components for displaying miniature or preview versions of maps or charts.
        *   `MiniatureMapView.java`, `GpxFileInfoMiniatureProvider.java`.

*   **`utils`**: General UI utility classes.
    *   `StringUtils.java`: String manipulation utilities specific to UI needs.

## Architecture

The UI package primarily follows the MVVM (Model-View-ViewModel) architecture pattern. Fragments (Views) observe data from ViewModels and update the UI accordingly. ViewModels fetch and prepare data, often interacting with UseCases from the `usecase` package or Repositories from the `data` package. Custom components encapsulate specific UI logic and rendering, promoting reusability.

```mermaid
graph TD
    A[User Interaction] --> B(Fragments/Activities in ui.gpxchart, ui.storage)
    B --> C{ViewModels (e.g., ChartAreaListViewModel, FileSelectorViewModel)}
    C --> D[Use Cases (usecase package)]
    D --> E[Data Layer (data package)]

    B -- Uses --> F(Custom UI Components in ui.components)
    F --> G(ui.components.chart)
    F --> H(ui.components.mapview)
    F --> I(ui.components.fab)
    F --> J(ui.components.geocoding)
    F --> K(ui.components.miniature)

    L[ui.mapper] -- Maps data for --> B
    M[ui.utils] -- Provides utilities for --> B
    M -- Provides utilities for --> F
```

*(Diagram shows user interaction flowing through Fragments/Activities to ViewModels, which use UseCases to interact with the data layer. UI components, mappers, and utils support the views.)* 