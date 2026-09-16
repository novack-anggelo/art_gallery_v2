# Art Gallery

Art Gallery is a portfolio Android app for discovering works from the Art Institute of Chicago. The project explores what can be achieved using all available development tools, including AI, while keeping product quality, clean architecture, accessibility, testing, and performance central.

The app uses the public [Art Institute of Chicago API](https://api.artic.edu/docs/#introduction) and its [IIIF image service](https://api.artic.edu/docs/#iiif-image-api).

> Status: The active milestone is LLM integration, starting with prompt-driven customization of Discover. The milestone plan is approved; each implementation feature still requires separate approval. Discover already supports adaptive columns and loading, error, empty, refresh, and pagination states. AI integration is not yet implemented. The remaining original roadmap is on hold.

## Development workflow

Codex implements approved features, performs QA, and opens PRs; the developer approves each feature and controls merging. Before implementation, every feature needs a concrete plan covering scope, architecture, acceptance criteria, verification, and estimated PR size. Milestone approval does not replace feature approval.

Commits must stay small, clear, and compilable. Each PR must contain fewer than 400 added plus deleted lines, counting code, tests, documentation, and configuration across the complete PR diff. Larger features must be planned as smaller, independently approved increments. QA happens before a PR is opened: review coding practices and architecture, run applicable checks, and obtain developer QA when needed. Uncertainty or a blocked required check must be raised rather than silently resolved through assumptions.

The repository's [working agreement](AGENTS.md) records the rules for ongoing implementation and review.

## Current progress

Implemented in the repository:

- Application startup and Koin network/data modules.
- Core dependencies for networking, pagination, image loading, navigation, and testing.
- Artwork listing API contract, response DTOs, and DTO-to-domain mapping.
- `ArtworkSummary` and the `ArtworkRepository` contract.
- `ArtworkPagingSource` and `NetworkArtworkRepository`.
- Tests for the dependency graph, network configuration, deserialization, mapping, paging, and repository output.
- `DiscoverViewModel`, its Koin registration, and tests using a main dispatcher rule.
- Application theme and navigation host, with `DiscoverRoute` collecting the cached paging flow.
- Artwork cards preserve the full image with `ContentScale.Fit`; titles are always visible, while artist, date, and medium can each be shown or hidden.
- A collapsible Discover header that keeps a compact version visible and expands when scrolling back up; search is deferred.
- Initial shimmer skeletons, an initial-error screen with Retry, and an empty collection message.
- Pull to refresh for populated and empty collections. Existing artworks remain visible during refresh; refresh failures with content show a themed snackbar without an action.
- Custom light/dark palettes and debug previews for cards, header, screen states, and errors.
- Full-width pagination footers for loading and errors, with Retry for failed loads.
- Content and skeletons support large (280 dp minimum) and compact (160 dp minimum) adaptive grids plus full-width thumbnail rows with 96 dp square images.
- Typed Discover preferences combine presentation and metadata visibility, with deterministic rules for direct selection, bounded size steps, and metadata changes.
- Discover preferences are stored locally with DataStore and exposed to the screen through `DiscoverViewModel`.
- Preference actions can be reduced and persisted together as one atomic multi-action transaction.
- `DiscoverViewModel` serializes preference operations and provides single-level undo plus an undoable reset to defaults.
- Compose instrumentation tests for header behavior and pull-to-refresh scenarios.

These entries describe code present in the repository, not a completed quality gate. Historically, validation passed the debug build, local unit tests, lint, and instrumentation-test compilation. Instrumentation execution previously encountered a device installation permission restriction; a passing device test run has not been confirmed. The developer has confirmed that the current UI and pull-to-refresh behavior work manually. These historical results do not establish validation of later changes; each PR reports its own checks.

The original roadmap reached implementation slice 3 (Discover). Adaptive columns are present; restoration verification remains pending. The detail data contract from slice 2 is still pending, and artwork clicks are not yet connected to a detail destination. Firebase AI Logic, prompt-driven customization UI, and user-facing preference results are not yet implemented.

The content grid now handles `loadState.append`: it shows a loading indicator at the end while another page loads, shows an inline error with Retry calling `artworks.retry()` if it fails, and removes the indicator when loading finishes or the collection ends. Existing artworks remain visible. Both footers span all grid columns.

Next, plan and approve a small implementation feature within the adaptive Discover milestone below. The milestone's verification includes browsing context and layout changes; the original details flow remains paused.

## Active milestone — Adaptive Discover through prompting

The first AI integration is for personal testing. Users will customize Discover with natural-language requests while the app retains control of rendering and supported behavior. This is an approved milestone plan, not a list of implemented capabilities or blanket authorization to code every feature.

### User experience

- A **Customize Discover** button opens a bottom sheet with a prompt field and example requests.
- Supported presentations are large image cards, compact grid cards, and thumbnail rows, with bounded image-size steps.
- Repeated requests such as "smaller" use the current preferences and follow a deterministic progression toward thumbnail rows. At the minimum size, explain that no further reduction is available.
- Artist, date, and medium can be shown or hidden independently. Artwork titles remain visible.
- Apply valid changes immediately, show a short result message, and provide **Undo** and **Reset to default**.
- Persist preferences locally across app restarts. Preserve browsing context as closely as possible during layout changes, anchored to the visible artwork.
- Unsupported requests, invalid model responses, network failures, and exhausted quotas leave preferences intact and show a useful explanation.

### Architectural approach

- Use Gemini through [Firebase AI Logic](https://firebase.google.com/docs/ai-logic), behind an interface supplied through Koin. No custom backend is planned for this milestone.
- Use a free-tier-eligible model on a Firebase Spark project without a linked billing account. Verify available models and actual project quotas during setup; do not assume unlimited free usage. See [Firebase AI Logic pricing](https://firebase.google.com/docs/ai-logic/pricing).
- Configure Firebase App Check for local development and keep private development tokens out of the repository.
- Send the request and current preferences for structured action interpretation. Validate the returned actions and bounds before applying application-controlled state transitions; the model does not generate executable UI code.
- Store typed Discover preferences locally using DataStore. Cloud preference synchronization is outside this milestone.
- Adapt cards and loading skeletons to the selected presentation while retaining existing Paging, refresh, and failure behavior.

### Delivery and acceptance

Plan the work in separately approved PRs below the size limit: preference and layout foundations, the customization interaction, Firebase integration, and the verification appropriate to each increment. These are architectural work areas, not a commitment to one PR per area; tests travel with the behavior they verify.

The milestone is complete when a user can repeatedly shrink images into thumbnail rows, toggle metadata, undo or reset changes, restart with preferences retained, and recover from AI failures without losing the selected presentation or loaded gallery. Verification covers application rules, persistence, rejected actions, Compose interaction using a fake AI service, and real prompts. Manual checks include scrolling, rotation, dark mode, and large system text. Each PR must pass its applicable build, test, lint, and QA checks before opening.

### Next integration and exclusions

Artwork Q&A follows later: conversations will concern a selected artwork, using museum data plus clearly labeled general knowledge. Its detailed feature plan and implementation still require approval.

This first milestone excludes artwork Q&A, app-wide UI customization, cloud preference synchronization, and the remaining original roadmap. Quality requirements for the work being delivered remain active even while broader roadmap features are paused.

## Product vision

Art Gallery should feel like a small museum companion that adapts to how a user wants to browse and helps them understand artworks. Prompt-driven Discover is the current focus; the longer-term vision below remains available for future planning:

- Discover artwork through an adaptive, paginated gallery.
- Open an artwork to inspect its image, artist, history, medium, dimensions, and location.
- Search and filter the collection without losing browsing context.
- Save works into personal collections and assemble a private exhibition.
- Explore artwork through color, related works, deep-zoom imagery, and audio tours.
- Continue browsing useful cached content when the network is unavailable.

## Original first release — On hold

The original first-release scope is preserved for future planning. Its remaining work is paused while adaptive Discover is developed:

1. A paginated artwork overview displayed as an adaptive grid.
2. Loading placeholders, empty states, initial-load errors, append errors, and retry actions.
3. Artwork details loaded by artwork ID.
4. Full-screen artwork imagery with basic pan and zoom.
5. Navigation that preserves the gallery's scroll position.
6. Unit tests for mapping and presentation logic, plus Compose UI tests for the main states.

For the overview, the client will request only the fields it renders, for example:

```text
id,title,artist_title,date_display,medium_display,image_id,thumbnail,is_public_domain,color
```

Image URLs will be derived from the API response's `config.iiif_url`; the host will not be hardcoded.

## Original roadmap — Remaining work on hold

The milestones below retain the original plan and completion criteria for reference. They do not describe the current implementation order or authorize new work. Applicable architecture, testing, and accessibility standards still govern the active AI milestone.

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

The original implementation sequence was organized into these vertical slices:

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
│   ├── data/
│   │   ├── di/
│   │   ├── mappers/
│   │   ├── paging/
│   │   ├── remote/
│   │   └── repository/
│   └── domain/
│       ├── model/
│       └── repository/
├── ui/
│   ├── common/
│   ├── feature/
│   │   └── discover/
│   └── theme/
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
- Firebase AI Logic for the active adaptive Discover milestone
- Media3 for audio tours
- JUnit, coroutine test utilities, Turbine, MockWebServer, and Compose UI testing
- Detekt or Android Lint plus a Kotlin formatter
- Macrobenchmark and Baseline Profiles

Retrofit is selected for the initial implementation because the API is conventional REST, it integrates naturally with OkHttp interceptors and MockWebServer, and it keeps the networking layer recognizable to Android reviewers. The decision can be revisited only if a concrete requirement makes another client materially better.

## Data and UI behavior

### Pagination

The data layer will translate API pagination into Paging 3 keys. Listing and search pagination differences will remain internal to the repository so the UI consumes the same `PagingData<ArtworkSummary>` abstraction in both cases.

Discover distinguishes initial loading, refresh, and append:

- Initial loading without content displays shimmer cards.
- An initial failure displays a full-screen error with Retry.
- A successful empty response displays a message and supports pull to refresh.
- Pull to refresh calls `artworks.refresh()` and preserves existing artworks while loading.
- A refresh failure with artworks displays a snackbar using `primaryContainer` / `onPrimaryContainer`, without an action; the user can pull again.
- Append loading and errors appear at the end of the grid. Append Retry calls `artworks.retry()` to retry failed loads without refreshing the collection. No footer is shown while append is not loading, including after reaching the end.

All states above are implemented; pagination footer behavior still needs device verification.

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
