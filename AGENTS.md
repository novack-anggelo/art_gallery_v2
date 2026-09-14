# Working agreement

## Direction and ownership

- Use all available development tools while keeping product quality, maintainability, and architectural clarity central.
- Codex implements approved work, verifies it, and opens pull requests. The developer approves each feature and controls merging.
- Keep planning, implementation, review, QA, and PR coordination in one task by default. Use subagents only when the developer explicitly requests them and independent parallel work provides a clear time or quality benefit.
- The active milestone is adaptive Discover through prompting. Artwork Q&A follows later; the remaining original roadmap is paused. See README.md for scope and current implementation status.

## Plan and approval before implementation

- Read the relevant code, README, and project instructions before proposing a feature.
- Present the feature's scope, architectural approach, acceptance criteria, verification plan, and estimated PR size. Obtain explicit approval before coding that feature.
- Milestone approval establishes direction; it does not authorize every implementation feature or increment. Each requires its own approved plan.
- If a feature will not fit the PR limit, propose smaller coherent increments before implementation and obtain approval for each increment.
- When uncertain about requirements, behavior, or architectural decisions, stop the affected work and ask. Do not turn unanswered questions into requirements.
- If implementation reveals a material scope or architecture change, return to clarification and revised-plan approval. Continue only independent work already authorized.

## Token-efficient execution

- After a PR is merged, recommend starting the next feature in a fresh Codex task with a concise handoff containing only current state, target branch, and relevant decisions.
- Use medium reasoning for routine, bounded work when reasoning effort is configurable. Increase it only for difficult architecture, debugging, or failed attempts that justify the additional cost.
- Read relevant sections and targeted code paths. Prefer focused searches, ranges, and diffs over printing whole files or complete diffs when a narrower view is sufficient.
- Keep prompts, tool output, agent reports, and progress updates concise while preserving decisions, blockers, and verification evidence.
- Run the required QA gate once on the final unchanged HEAD. Repeat successful checks only after relevant changes, failures, or unresolved evidence.

## Coding and architecture

- Keep changes focused, readable, and consistent with existing Kotlin and Compose conventions. Avoid unrelated cleanup and speculative abstractions.
- Keep Compose responsible for rendering state and reporting actions; coordinate feature behavior in ViewModels and meaningful domain rules.
- Access network and persistence through repository or service contracts. Keep transport models and provider SDK details out of UI and domain logic.
- Keep domain models independent of Android, serialization, persistence, and Koin. Use Koin at the composition boundary.
- Preserve Paging behavior, accessibility, and browsing context when changing Discover layouts.
- Treat LLM output as untrusted input: validate supported actions and bounds before changing preferences. Application rules control rendering and state transitions.
- Keep credentials and private development tokens out of source control and PRs.

## Commits and pull requests

- Use a separate Git worktree with a dedicated `codex/` branch for each approved feature or PR, based on the appropriate current target branch. Preserve the developer's checkout and unrelated work.
- Create future feature worktrees as sibling directories under `/Users/anggelo.novack/StudioProjects`, never under `/private/tmp` (for example, `/Users/anggelo.novack/StudioProjects/art_gallery_v2_thumbnail_rows`).
- Run edits, builds, tests, and Git operations from that worktree. Independent approved features can proceed in parallel in separate worktrees; coordinate dependencies and overlapping changes before starting concurrent work.
- Keep every commit small, clear, focused on one purpose, and compilable. Do not leave intermediate commits with unresolved references or broken builds.
- Keep each PR below 400 total added plus deleted lines, including production code, tests, documentation, and configuration. Target a smaller diff to leave room for review fixes.
- Count the complete PR diff against its intended base, not only the latest commit. Splitting commits does not make an oversized PR acceptable.
- If the limit is at risk, pause and agree a smaller scope or approved split; do not compress code or omit necessary tests to fit.
- Open PRs only after the QA gate below. Describe the problem, resulting behavior, verification results, and any relevant limitations.
- Do not merge PRs without explicit developer authorization.

## QA before opening a PR

1. Review the complete diff for correctness, coding practices, architectural boundaries, scope, and accidental sensitive data.
2. Verify each commit remains buildable. Run the debug build and relevant automated tests and lint for the change; inspect failures before proceeding.
3. Exercise changed UI flows where applicable, including loading, failure recovery, accessibility, and restoration behavior affected by the change.
4. If developer QA is needed, provide concrete steps and expected results, then wait for the results before opening the PR.
5. Check whitespace and the complete PR size, including added files. Confirm the PR contains only the approved work.
6. Report checks accurately. If a required check is blocked or fails, explain the blocker and ask how to proceed before opening the PR; do not claim a passing gate.

Do not add application tests for documentation-only edits. Review documentation against the code and approved decisions, check links and formatting, and verify that application and build inputs are unchanged.

## Local verification commands

Use the repository's Gradle wrapper and a compatible JDK configured by Android Studio:

```sh
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew connectedDebugAndroidTest # when device QA applies and a device is available
git diff --check
```

Run commands appropriate to the approved change; distinguish tests actually executed from historical results recorded in the README.
