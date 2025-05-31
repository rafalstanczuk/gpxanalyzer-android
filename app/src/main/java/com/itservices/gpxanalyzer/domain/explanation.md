# Domain Package Explanation

This package contains the core business logic and domain entities of the GpxAnalyzer application. It defines the rules and operations related to GPX data processing and analysis.

## Components

### `service` sub-package

This sub-package holds the service interfaces and their implementations, which encapsulate the application's business logic.

*   **`GpxFileInfoUpdateService.java`**: An interface defining the contract for updating GPX file information. This includes operations such as scanning for GPX files, generating map miniatures for tracks, performing geocoding for track locations, and updating the database with this information.
*   **`GpxFileInfoUpdateServiceImpl.java`**: The concrete implementation of `GpxFileInfoUpdateService`. It orchestrates the scanning of GPX files, generation of track miniatures using a `MiniatureMapView`, fetching/caching of geocoded information for track start points via Android's geocoder and a local geocoding cache, and finally, updating the main GPX file information database. It also emits progress events during these operations.

## Architecture