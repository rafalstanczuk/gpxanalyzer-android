# Events Package Explanation

This package defines various event classes used for communication between different components of the GpxAnalyzer application. These events help in decoupling components and managing application state changes or notifications.

## Components

*   **`EventEntrySelection.java`**: Represents an event triggered when a data entry (e.g., a point on a chart or map) is selected.
*   **`EventProgress.java`**: Represents an event indicating the progress of an operation. It holds a `PercentageUpdateEventSourceType` to identify the operation and an `int` for the current progress percentage (0-100).
*   **`EventVisibleChartEntriesTimestamp.java`**: Represents an event indicating the range of timestamps (min and max) currently visible within a specific chart (`ChartSlot`). This is triggered when the chart's visible time range changes.
*   **`GlobalEventWrapper.java`**: A singleton event bus implementation using RxJava (`PublishSubject` and `BehaviorSubject`). It allows components to publish and subscribe to application-wide events such as `EventEntrySelection`, `RequestStatus`, `EventProgress`, and `EventVisibleChartEntriesTimestamp`, facilitating decoupled communication.
*   **`PercentageUpdateEventSourceType.java`**: An `enum` that defines and categorizes the sources of `EventProgress` updates. Each enum value corresponds to a specific class or operation (e.g., `GPX_FILE_DATA_ENTITY_PROVIDER`, `MINIATURE_GENERATION_PROGRESS`) that reports progress.
*   **`RequestStatus.java`**: Represents the status of a request (e.g., loading, success, error), often used in asynchronous operations to update the UI or other components.

## Architecture

The events package facilitates an event-driven architecture or is a key part of a reactive programming model. Components can publish events, and other interested components can subscribe to these events to react accordingly. This promotes loose coupling and makes the system more responsive.

```mermaid
graph TD
    A[Component A (e.g., UI)] -- Publishes Event --> B(EventBus/EventChannel)
    B -- Delivers Event --> C[Component B (e.g., ViewModel)]
    B -- Delivers Event --> D[Component C (e.g., Service)]

    subgraph Event Types
        E[EventEntrySelection]
        F[EventProgress]
        G[EventVisibleChartEntriesTimestamp]
        H[GlobalEventWrapper]
        I[PercentageUpdateEventSourceType]
        J[RequestStatus]
    end

    B --> E
    B --> F
    B --> G
    B --> H
    B --> I
    B --> J
```

*(Diagram illustrates a simplified event-driven flow where components communicate via an event bus using various event types defined in this package.)* 