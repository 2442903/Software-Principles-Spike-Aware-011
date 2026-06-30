**SpikeAware — Design Document**

Goals
-----
- Describe architecture and responsibilities of major modules.
- Explain core data models and flows (authentication, resource lifecycle, team management).
- Clarify concurrency, persistence, and error-handling choices.
- Provide practical notes for testing, deployment, and extension.

Repository entry points
-----------------------
- Main application: [spike-aware-uk/src/main/java/com/spikeaware/Main.java](spike-aware-uk/src/main/java/com/spikeaware/Main.java)
- Configuration manager: [spike-aware-uk/src/main/java/com/spikeaware/config/ConfigurationManager.java](spike-aware-uk/src/main/java/com/spikeaware/config/ConfigurationManager.java)
- Database manager: [spike-aware-uk/src/main/java/com/spikeaware/db/DatabaseManager.java](spike-aware-uk/src/main/java/com/spikeaware/db/DatabaseManager.java)
- Core models: [spike-aware-uk/src/main/java/com/spikeaware/model/](spike-aware-uk/src/main/java/com/spikeaware/model/)
- Services: [spike-aware-uk/src/main/java/com/spikeaware/service/](spike-aware-uk/src/main/java/com/spikeaware/service/)
- Team management: [spike-aware-uk/src/main/java/com/spikeaware/team/TeamManager.java](spike-aware-uk/src/main/java/com/spikeaware/team/TeamManager.java)

High-level architecture
---------------------------------------
Think of the application as a small office with clear roles:
- Configuration: the office rules and settings (who works where, where files are stored).
- Database manager: the filing clerk who reads/writes records.
- Services: the staff members who perform business tasks (authentication, resource management, team operations).
- Models: the forms used to describe things (resources, status, team members).

Each piece has a single responsibility and talks to the others via well-defined method calls. The `Main` class starts the app, loads configuration, and wires components together.

Component breakdown
-------------------
- Configuration (`ConfigurationManager`): Reads settings (paths, DB config, feature toggles). Keep configuration simple and environment-driven.
- Persistence (`DatabaseManager`): Encapsulates all data access. Services should not directly access low-level database code — use `DatabaseManager` methods.
- Domain models (`model` package): `Resource`, `PublicResource`, `ResearchResource`, and `ResourceStatus` capture the key entities. Keep them small and focused on data.
- Services (`service` package): Implement business logic:
  - `AuthenticationService`: validates credentials and issues session info.
  - `ResourceService`: CRUD and business rules for resources.
  - `TeamService`: operations around teams and permissions.
- Team (`team` package): `TeamManager`, `TeamMember`, `UserRole` manage membership, roles, and team-level operations.

Key data flows (user-friendly)
-----------------------------
1. Authentication flow
   - User sends credentials to `AuthenticationService`.
   - Service checks credentials (via `DatabaseManager`) and returns a session/identity object.
   - Downstream services use that identity to authorize actions.

2. Resource lifecycle
   - Create: `ResourceService` validates input and asks `DatabaseManager` to persist a new `Resource`.
   - Read: `ResourceService` fetches records and maps them to `PublicResource` or `ResearchResource` as needed.
   - Update/Delete: `ResourceService` enforces rules (e.g., only owners or admins can delete) and calls `DatabaseManager`.

3. Team management
   - `TeamManager` adds/removes `TeamMember`s and updates `UserRole`s.
   - Permissions produced here are checked by services before sensitive operations.

Important implementation notes
------------------------------
- Keep business rules in `service` classes. Small helper methods are fine in models but avoid migrating logic into `DatabaseManager`.
- Prefer clear method names (e.g., `createResource`, `findResourceById`, `assignRole`) so callers don't need to inspect internals.
- Centralize logging and errors in services rather than scattering prints through the codebase.

Error handling and logging
-------------------------
- Fail fast: validate inputs early and return helpful error messages.
- Use structured logging (timestamp, class, method, correlation id) to trace requests.
- Map internal errors to user-friendly messages at service boundaries.

Deployment and runtime notes
----------------------------
- Runtime: the app starts at `Main` and loads configuration via `ConfigurationManager`.
- Persistence: `DatabaseManager` encapsulates DB connections; ensure credentials come from environment variables.
- Configuration: prefer environment-driven config for CI/CD and containerized deployments.

Glossary
-------------------------
- Resource: an item the app tracks (could be public or research-specific).
- Service: a code module that performs a business task.
- Model: a simple data container describing an entity.
- Transaction: a safe bundle of operations that succeed or fail together.

References (key files)
----------------------
- [Main](spike-aware-uk/src/main/java/com/spikeaware/Main.java)
- [ConfigurationManager](spike-aware-uk/src/main/java/com/spikeaware/config/ConfigurationManager.java)
- [DatabaseManager](spike-aware-uk/src/main/java/com/spikeaware/db/DatabaseManager.java)
- [ResourceService](spike-aware-uk/src/main/java/com/spikeaware/service/ResourceService.java)
- [TeamManager](spike-aware-uk/src/main/java/com/spikeaware/team/TeamManager.java)
