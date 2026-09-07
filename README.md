# Art Gallery

Art Gallery is a portfolio Android app for discovering works from the Art Institute of Chicago. It is designed to demonstrate modern Android development, thoughtful product design, clean architecture, offline support, accessibility, testing, and performance—not just API consumption.

The app uses the public [Art Institute of Chicago API](https://api.artic.edu/docs/#introduction) and its [IIIF image service](https://api.artic.edu/docs/#iiif-image-api).

> Status: Milestone 1 is in progress. The network and paginated listing data layers are implemented. The next step is the Discover ViewModel and its Koin registration, followed by the Compose gallery.

## Learning workflow

This is a developer-led portfolio project. The developer implements the application; Codex acts as a mentor by explaining tradeoffs, guiding small exercises, and reviewing the resulting code. Application implementation is not delegated to Codex. Unclear requirements must be clarified before proceeding. Documentation may be edited directly when requested.

## Current progress

Implemented in the repository:

- Application startup and Koin network/data modules.
- Core dependencies for networking, pagination, image loading, navigation, and testing.
- Artwork listing API contract, response DTOs, and DTO-to-domain mapping.
- `ArtworkSummary` and the `ArtworkRepository` contract.
- `ArtworkPagingSource` and `NetworkArtworkRepository`.
- Tests for the dependency graph, network configuration, deserialization, mapping, paging, and repository output.

These entries describe code present in the repository, not a completed quality gate. Tests were inspected but not executed during the documentation review.

The project is currently in implementation slice 3 (Discover), with its data layer in place. The detail data contract from slice 2 is still pending. The Compose application shell currently contains an empty `Scaffold`.

The immediate exercise is to create `DiscoverViewModel`, inject `ArtworkRepository`, expose its paginated flow as a property using `cachedIn(viewModelScope)`, and register the ViewModel in a feature Koin module. After reviewing that step, connect the flow to Compose through `LazyPagingItems`, then add the adaptive grid, image loading, load states, retries, and state restoration.

## Product vision

Art Gallery should feel like a small museum companion:

- Discover artwork through an adaptive, paginated gallery.
- Open an artwork to inspect its image, artist, history, medium, dimensions, and location.
- Search and filter the collection without losing browsing context.
- Save works into personal collections and assemble a private exhibition.
- Explore artwork through color, related works, deep-zoom imagery, and audio tours.
- Continue browsing useful cached content when the network is unavailable.

## First release

The first vertical slice will deliver a complete, testable flow:

1. A paginated artwork overview displayed as an adaptive grid.
2. Loading placeholders, empty states, initial-load errors, append errors, and retry actions.
3. Artwork details loaded by artwork ID.
4. Full-screen artwork imagery with basic pan and zoom.
5. Navigation that preserves the gallery's scroll position.
6. Unit tests for mapping and presentation logic, plus Compose UI tests for the main states.

For the overview, the client will request only the fields it renders, for example:

```text
id,title,artist_title,date_display,image_id,thumbnail,is_public_domain,color
```

Image URLs will be derived from the API response's `config.iiif_url`; the host will not be hardcoded.

## Roadmap

### Milestone 1 — Discover and details

- Establish network, domain, and presentation boundaries.
- Add a Koin application graph and feature ViewModels.
- Implement artwork DTOs and explicit DTO-to-domain mapping.
- Implement the artwork listing with Paging 3.
- Build an adaptive Compose grid for phones and larger screens.
- Add type-safe list-to-detail navigation using the artwork ID.
- Implement details, public-domain attribution, and external museum links.
- Cover loading, content, empty, and recoverable error states.

#### Milestone 1 implementation slices

Work will proceed in small vertical slices so the application remains buildable and reviewable after each change:

1. **Foundation:** add Retrofit, kotlinx.serialization, Paging, Coil, Navigation, Koin, and test dependencies; create the `Application` class and Koin modules; add network permission and a graph smoke test.
2. **Data contract:** model the list and detail responses, map nullable DTOs into stable domain models, create the API service and repository contract, and validate representative JSON fixtures.
3. **Discover:** implement the `PagingSource`, repository integration, ViewModel, adaptive grid, image loading, load states, retry behavior, and state restoration.
4. **Details:** implement ID-based navigation, independent detail loading, metadata presentation, attribution, external links, and basic image zoom.
5. **Quality gate:** add repository and ViewModel tests, Compose screen tests, accessibility checks, lint, formatting, and CI before declaring the milestone complete.

Milestone 1 is complete when a fresh install can browse multiple pages, retain loaded pages and grid position across configuration changes without redundant reloads, restore screen context after system-initiated process death (network reloads are allowed), open a detail by ID, recover from simulated failures, and pass the automated quality checks. Persistent offline recovery belongs to Milestone 2.

### Milestone 2 — Offline and discovery

- Add Room and Paging `RemoteMediator` for offline-first browsing.
- Define refresh and stale-data policies explicitly.
- Add debounced full-text search with cancellable requests.
- Add filters for public-domain status, artist, style, classification, and date.
- Persist recent searches and filter preferences.
- Add favorites and named personal collections.

### Milestone 3 — Portfolio differentiators

- Add a high-resolution IIIF deep-zoom viewer.
- Build an “Explore by color” experience using dominant HSL metadata.
- Let users create and reorder a personal exhibition with curator notes.
- Recommend related works using artist, style, material, and subject metadata.
- Add audio tours with Media3 playback and accessible transcripts.
- Add deep links, sharing, and an optional daily-artwork Glance widget.

### Milestone 4 — Production quality

- Complete TalkBack, large-text, contrast, and reduced-motion checks.
- Add screenshot tests for phone, tablet, dark theme, and large font scales.
- Add Macrobenchmark coverage and a Baseline Profile for startup and scrolling.
- Add static analysis, formatting, unit tests, and builds to CI.
- Document performance results and architectural tradeoffs.

## Architecture

The project will start as a single Gradle application module organized by feature. This keeps build and navigation complexity low while preserving boundaries that can be extracted into modules if the app grows enough to justify it.

```text
app/src/main/kotlin/com/novack/artgalleryv2/
├── core/
│   ├── common/
│   ├── database/
│   ├── designsystem/
│   ├── model/
│   └── network/
├── feature/
│   ├── artwork/
│   ├── collections/
│   ├── discover/
│   ├── search/
│   └── tours/
├── navigation/
└── di/
```

The dependency direction is:

```text
Compose UI → ViewModel → use case or repository contract
                                      ↑
                         repository implementation
                             ↙                  ↘
                          Room                  API
```

Key rules:

- UI renders immutable state and reports user actions.
- ViewModels coordinate feature behavior and expose `StateFlow` for ordinary UI state. Paginated content is exposed as `Flow<PagingData<ArtworkSummary>>` with `cachedIn(viewModelScope)`; it does not need conversion to `StateFlow`. Paging load states drive pagination loading and error presentation.
- Use cases are introduced for meaningful domain behavior, not as wrappers around every repository method.
- Repository contracts shield features from network and database implementation details.
- API DTOs and Room entities never leak into UI code.
- Domain models stay independent of Android, serialization, persistence, and Koin.
- Failures are mapped into explicit app-level error types at data boundaries.

## Dependency injection with Koin

[Koin](https://insert-koin.io/) will provide dependency injection. The graph will be split by responsibility so it remains easy to navigate and override in tests:

```text
appModule
├── networkModule       HTTP client, API service, JSON configuration
├── databaseModule      Room database and DAOs
├── repositoryModule    repository implementations and contracts
└── featureModules      ViewModels and feature-specific coordinators
```

Application startup will load the production modules once. Compose destinations will obtain their ViewModels through Koin's Compose integration. Unit and UI tests can load replacement modules containing fakes or in-memory infrastructure.

Koin is intentionally kept at the composition boundary: domain models and business rules do not call service locators or depend on Koin types.

## Planned technology stack

- Kotlin and coroutines/Flow
- Jetpack Compose and Material 3
- Navigation Compose
- Koin
- Retrofit with kotlinx.serialization
- Paging 3
- Room
- Coil
- DataStore
- Media3 for audio tours
- JUnit, coroutine test utilities, Turbine, MockWebServer, and Compose UI testing
- Detekt or Android Lint plus a Kotlin formatter
- Macrobenchmark and Baseline Profiles

Retrofit is selected for the initial implementation because the API is conventional REST, it integrates naturally with OkHttp interceptors and MockWebServer, and it keeps the networking layer recognizable to Android reviewers. The decision can be revisited only if a concrete requirement makes another client materially better.

## Data and UI behavior

### Pagination

The data layer will translate API pagination into Paging 3 keys. Listing and search pagination differences will remain internal to the repository so the UI consumes the same `PagingData<ArtworkSummary>` abstraction in both cases.

### State restoration

Navigation passes only stable identifiers. Restoration has two distinct requirements in Milestone 1:

- **Configuration changes:** retain loaded pages and grid position across rotation and light/dark theme changes. The Discover ViewModel caches the paginated flow with `cachedIn(viewModelScope)`. Recreating the UI must not restart requests for already loaded pages. Loading a new page when a larger viewport triggers prefetch is acceptable.
- **System-initiated process death:** restore lightweight screen context using saved-state mechanisms, such as `SavedStateHandle` for ViewModel state and saveable Compose state for UI state. The ViewModel and its in-memory page cache do not survive process death, so reloading artwork from the API is allowed. Full artwork lists must not be stored in saved-state bundles.

Returning from details while the Discover ViewModel remains alive must restore the previous grid position and reuse loaded pages. Persistent artwork caching and offline recovery will be added in Milestone 2 with Room and an explicit refresh policy.

### Errors

The UI will distinguish between:

- an initial request failure with no cached content;
- a pagination failure after content is already visible;
- an empty collection or empty search result;
- cached content that may be stale;
- rate limiting or a temporarily unavailable service;
- missing or failed images;
- metadata that is legitimately absent.

### Accessibility

- Images use the API's thumbnail alt text where available, with a sensible artwork-title fallback.
- Controls have explicit semantics and sufficiently large touch targets.
- Layouts support system font scaling without truncating essential information.
- Color is decorative or paired with text; it never carries meaning alone.
- Audio experiences expose transcripts and usable playback semantics.
- Motion-heavy transitions respect the system's reduced-motion preference where possible.

## Testing strategy

The test suite will favor behavior at architectural boundaries:

- DTO and entity mapping, especially missing and nullable fields.
- Repository cache, refresh, and fallback behavior.
- Paging refresh, append, retry, and end-of-pagination behavior.
- ViewModel state transitions using deterministic coroutine dispatchers.
- Compose rendering and interaction for loading, content, empty, and error states.
- Navigation and state restoration for the discover-to-detail flow.
- API contract fixtures captured from representative responses.
- Performance benchmarks for startup and gallery scrolling.

Production dependencies will be replaced with fakes through Koin test modules where dependency substitution adds value. Pure classes will still be constructed directly in unit tests.

## API usage and licensing

The Art Institute API does not require authentication, but anonymous clients are rate-limited. The app will:

- use HTTPS;
- send a descriptive `AIC-User-Agent` header;
- select only required response fields;
- debounce and cancel obsolete searches;
- cache responses and avoid unnecessary refetches;
- use the response-provided IIIF configuration;
- prefer public-domain images for portfolio and promotional surfaces;
- display source, copyright, and licensing information where appropriate.

Artwork descriptions and images may have licensing restrictions distinct from other metadata. See the API's [copyright guidance](https://api.artic.edu/docs/#copyright) and the museum's linked terms before redistributing content.

## Local development

Prerequisites:

- Android Studio with support for the project's Android Gradle Plugin version
- JDK 17 or a compatible toolchain configured by Android Studio
- Android SDK 29 or newer device/emulator; the project currently targets SDK 36

Build the debug application:

```bash
./gradlew assembleDebug
```

Run local unit tests:

```bash
./gradlew test
```

Run lint checks:

```bash
./gradlew lint
```

## Architectural decisions to document

Short architecture decision records will be added as choices become real. The first expected records are:

1. Retrofit and kotlinx.serialization for the network client.
2. Page-keyed caching and the Room `RemoteMediator` schema.
3. Public-domain filtering and image licensing behavior.
4. Single-module feature organization and the threshold for modularization.
5. IIIF image sizing, caching, and deep-zoom strategy.

## Acknowledgements

Collection data and images are provided by the [Art Institute of Chicago](https://www.artic.edu/) through its public API. This project is an independent portfolio application and is not an official Art Institute of Chicago product.
