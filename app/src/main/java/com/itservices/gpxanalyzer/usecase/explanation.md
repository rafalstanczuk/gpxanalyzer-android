# Use Case Package Explanation

This package contains use cases, which represent specific actions or business operations that a user can perform or that the system executes. Use cases orchestrate interactions between the domain layer (services, entities) and the presentation layer (ViewModels).

## Components

*   **`ChartInitializerUseCase.java`**: Responsible for initializing a chart by calling the `initChart()` method on a `ChartAreaItem`'s `ChartController`. It returns the `ChartAreaItem` upon completion.
*   **`GetGpxFileInfoListUseCase.java`**: Fetches a list of `GpxFileInfo` objects by calling `getAndFilterGpxFiles()` on a `GpxFileInfoProvider`.
*   **`LoadChartDataUseCase.java`**: Orchestrates loading data into charts. This involves fetching raw GPX data via `GpxDataEntityCachedProvider`, initializing charts (using `ChartInitializerUseCase`), then fetching processed data for each chart's specific view mode (via `RawDataProcessedProvider` and `GpxViewModeMapper`), and finally updating the chart views using `ChartAreaItem.updateChart()`. It emits `RequestStatus` events throughout this process.
*   **`MultipleSyncedGpxChartUseCase.java`**: Manages the data loading for a list of `ChartAreaItem` instances using `LoadChartDataUseCase` and `ChartInitializerUseCase`. It ensures that any previous load operation is disposed before a new one begins, effectively serializing load requests for a given set of charts.
*   **`SelectGpxFileUseCase.java`**: Handles user selection of GPX files. It manages Android Activity Result Launchers for file picking and permission requests, checks and requests file access permissions, launches the system file picker, copies the selected file's URI to internal app storage (using `FileProviderUtils`), maintains a list of available GPX files, and notifies other components of selections and permission changes via RxJava Subjects.
*   **`UpdateGpxFileInfoListUseCase.java`**: Orchestrates a full update and processing flow for GPX files. It uses `GpxFileInfoUpdateService` to scan for GPX files on the device, generate map miniatures for them, perform geocoding for their start locations, and finally update the application's database with all this new information.

## Architecture

Use cases in this package encapsulate application-specific business rules and orchestrate the flow of data from data sources (via repositories or services) to the UI (often through ViewModels), and vice-versa. They are a key part of a clean architecture, promoting separation of concerns and testability.

```mermaid
graph TD
    A[ViewModel/Presenter] --> B(UseCase)
    B --> C[Domain Services/Repositories]
    C --> D[Data Sources (DB, Network)]

    subgraph Use Cases
        E[ChartInitializerUseCase]
        F[GetGpxFileInfoListUseCase]
        G[LoadChartDataUseCase]
        H[MultipleSyncedGpxChartUseCase]
        I[SelectGpxFileUseCase]
        J[UpdateGpxFileInfoListUseCase]
    end

    A --> E
    A --> F
    A --> G
    A --> H
    A --> I
    A --> J
```

*(Diagram illustrates how ViewModels or Presenters interact with UseCases, which in turn interact with domain services or repositories to perform specific application tasks.)* 